# Changelog

## 2.0-SNAPSHOT

This release modernizes the toolchain and completes RSS 2.0 coverage. Most of the changes
below are breaking; nothing was ever published under `io.github.onurdemir55`, so there was
no compatibility to preserve.

### Toolchain

- Minimum Java version raised to 17.
- Lombok removed. `Value`, `SimpleValue` and `CDATAValue` became a sealed interface and
  records; `Rss`, `Channel` and `Item` gained hand-written builders. The consumer-facing API
  (`X.builder()....build()`) is unchanged.
- Gradle 7.1 → 9.6.1, JUnit 5.7.0 → 6.1.2.
- Jackson removed. The feed is now written element by element through StAX, so the model
  classes carry no serialization annotations and the document is produced by one class that
  can be read from top to bottom. The only remaining dependency is Woodstox, the StAX
  implementation, needed at runtime only.

  The output is the same, and that was established by comparison rather than by assertion.
  The previous implementation was built alongside this one and both were asked to produce the
  same 297 feeds — every optional element alone and in pairs, 34 text shapes including empty
  strings, newlines, `]]>` in several positions, emoji and non-Latin scripts, every element
  count from zero to three — and the results compared byte for byte, with no normalizing.
  294 were identical. The other three differ in one line each, all the same deliberate
  improvement: a channel-level extension element is now written before the items rather than
  after, which the annotation-driven writer had no way to do. All 297 parse, all 297 are read
  correctly by Rome, and the three that changed were checked against the W3C Feed Validation
  Service.

  This mattered because the first attempt at the comparison used one hand-written feed and
  compared the two outputs after `strip()`, which hid a real difference: the old
  implementation ended the document with a newline and the new one did not. It was noticed by
  accident, while measuring something else. The output now ends with a newline as before, and
  the corpus is no longer a one-off script: it lives in the test suite, with its expected
  output recorded in `src/test/resources/corpus/feeds.txt`, so any change to a single
  character of any feed fails the build. Removing that trailing newline again, or changing the
  indent width, now fails 297 assertions instead of passing quietly.

  The reason for the change was not dependency count. Four capabilities this library exists
  to provide had no direct support in Jackson XML and were reached by repurposing features
  meant for something else: runtime-chosen namespace declarations and element names, "this
  nested object is the element's text", and splitting a CDATA section on `]]>`, which needed
  the layer underneath Jackson to be configured directly. Each of those is a plain method
  call against StAX.

### Fixed

- `CDATAValue` content containing `]]>` no longer fails to serialize; it is split across two
  CDATA sections instead.
- `DateParser` no longer follows the platform locale, which could emit non-English day and
  month names and produce an unparseable feed.
- `RssOutput` no longer rebuilds its serializer on every call.
- `RssOutput.output(Rss, OutputStream)` and `output(Rss, Writer)` no longer close the
  caller's stream. The javadoc always said "flushed but not closed"; the serializer's default
  was to close the target after writing, so that was not true until now. A caller using
  try-with-resources around their own stream would have hit a double-close.
- **An extension or namespace name is checked instead of being written blindly.** Element
  text is escaped on the way out, so bad text was only ever ugly, but an element name is
  written as markup: `extension("<evil>", ...)` produced `<<evil>>...</<evil>>` and a
  document no parser would accept. A name assembled from someone else's input could have put
  arbitrary markup into the feed. Names are now checked when the feed is built, and `xml` and
  `xmlns` are refused as namespace prefixes because XML reserves them.

  The first version of that check asked `Character.isLetter`, which is a Unicode category test
  and not the production XML gives. Five characters were found that it accepted and that a
  strict parser then refused: `U+00AA` and `U+00B5`, which no edition of XML allows in a name,
  and `U+02B0`, `U+2113` and `U+FF10`, which the fifth edition added and the parser in the JDK
  still rejects. Names are therefore held to ASCII letters, digits, hyphens, dots and
  underscores. That is narrower than the specification on purpose: matching it exactly would
  mean emitting feeds that a very widely deployed reader will not parse, and every extension
  module in use spells its names in ASCII regardless. Element text is not restricted this way.
- **A carriage return survives a CDATA section.** XML normalizes line endings before anything
  else sees them, turning `\r\n` and a lone `\r` into `\n`. Escaped text was unaffected, because
  a carriage return is written `&#xd;` and a character reference is resolved after
  normalization, but a CDATA section cannot escape its way out: `a\r\nb` came back out of a
  parser as `a\nb`, silently, for the commonest input there is - markup written on Windows. The
  section is now broken around each carriage return, which is written as escaped text between
  two sections, the same technique the CDATA terminator already needed. Text with no carriage
  return is written exactly as before. Found by an audit; a round-trip test now pins it, which
  is what was missing - nothing had compared what a reader gets back against what was set.
- **A namespace URI is checked for characters XML cannot represent.** Every other value that
  reaches the document was, and this one was not, so a control character in it was discovered
  while writing - and because the document is streamed, 81 bytes of a feed were already on the
  caller's stream when it failed. Measured, then closed.
- **A negative `ttl` is refused.** The specification calls it a number of minutes. An hour of 25
  in `skipHours` and an image 200 pixels wide were already refused; this was the inconsistency.
- **A URI scheme must be spelled in ASCII.** `Uris` asked `Character.isLetter`, which is a
  Unicode category test rather than the ALPHA of RFC 3986, and accepted `ürl://` and `ℓink://`.
  The same confusion that let five characters into element names.
- **A blank namespace URI is refused.** `namespace("dc", "")` was accepted and produced
  `xmlns:dc=""`, which a parser rejects outright: XML lets the default namespace be undeclared
  that way and gives a prefixed binding no equivalent. Found by auditing the builders against a
  strict parser rather than against the tests.
- **An extension element must now have a declared namespace prefix.** Calling
  `.extension("dc:creator", ...)` without `.namespace("dc", ...)` used to produce a feed with
  no `xmlns:dc` on it: not well-formed XML, built and written without complaint, and refused
  by whatever finally read it. `Rss.builder().build()` now rejects it, that being the only
  place where the prefixes in use and the declarations are both known. An extension with no
  prefix at all is rejected too, which is what the specification requires.
- **`Rss.Builder.build()` no longer changes the builder.** The Atom namespace is declared
  automatically when the channel carries an `atom:link`, and that declaration used to be
  written into the builder's own list, so a second feed built from the same builder inherited
  it and declared a namespace it had no element for. The declarations are now worked out into
  a copy, which also makes building the same builder twice give the same document.
- **Text and attribute values that XML cannot represent are rejected when they are created**,
  rather than failing during writing. A NUL or other control character has no spelling in XML
  1.0 — neither escaping nor a CDATA section can encode it — and such characters do turn up in
  text taken from a database column. Because the document is streamed, discovering this at
  write time meant a half-written feed already on the caller's stream.

### Added

- **Full element coverage.** `<channel>` now supports all 19 elements the specification
  describes (previously 5): `copyright`, `managingEditor`, `webMaster`, `lastBuildDate`,
  `category`, `generator`, `docs`, `cloud`, `ttl`, `image`, `rating`, `textInput`,
  `skipHours`, `skipDays`. `<item>` now supports all 10 (previously 4): `author`, `category`
  with `@domain`, `comments`, `enclosure`, `guid` with `@isPermaLink`, `source`.
- **Namespace support.** `Rss.builder().namespace(prefix, uri)` declares an `xmlns:*`
  attribute; `Channel`/`Item` `.extension(prefixedName, Value)` adds an element from another
  namespace, plain or CDATA. `AtomLink.self(href)` emits the `atom:link rel="self"` the RSS
  Best Practices Profile recommends and declares its namespace automatically.
- **Specification validation, enforced at build time, with no lenient mode.** A channel
  without title, link or description, an item without a title or a description, a `link` or
  `url` without a URI scheme, an `enclosure` that is not `http(s)`, or a document declaring a
  version other than `2.0` are all rejected before they become an invalid feed.
- Getters on `Rss`, `Channel` and `Item` for every element, including `Rss.getNamespaces()`.
- `RssOutput.output(Rss, OutputStream | Writer | Path)`, alongside `outputString`.
- Validated against the specification's own sample feed and against the W3C Feed Validation
  Service (the software behind the RSS Validator): 0 errors on every generated sample.

### Changed

- `Item.category` is `List<Category>`, not `Set<Value>`. A set left the serialized order
  undefined and dropped categories that share text under different domains, which the
  specification allows.
- `DateParser.format_RFC1123_RFC822` renamed to `formatRfc822` and pads the day to two
  digits, matching every example in the specification. The old name is not kept: nothing was
  ever published under this namespace, so there is no caller to keep compiling.
- `Rss.version` defaults to `"2.0"`.
- **No exception type from another library appears in the API any more.**
  `RssOutput.outputString` used to declare `JsonProcessingException`; it now declares no
  checked exception at all, because building a string in memory has no target that can fail.
  Where it genuinely cannot produce a document it throws `RssOutputException`, which is
  unchecked. The overloads that write to a stream, a writer or a file throw `IOException`.
  They do not try to separate "the disk was full" from "the document could not be produced",
  because the underlying writer reports both identically, and a guess presented as a
  diagnosis is worse than neither.
- The published jar declares `Automatic-Module-Name: io.github.onurdemir55.resurrections.rss`.
  Without it the module name is derived from the jar's file name, which gave
  `resurrections.rss` - not the package root, and not stable if the artifact is ever renamed,
  which would break anyone using it on the module path.
- `Channel.getExtensions()` and `Item.getExtensions()` are public. They were hidden only
  because a serialization framework used to find them by reflection.

### Not added

- Reading or parsing an existing feed. This library produces feeds; it does not consume them.

  The reason recorded here previously was that a CDATA section and escaped text cannot be told
  apart once parsed, so reading and re-writing a feed would silently turn every CDATA element
  into plain text. That reason no longer holds and the correction belongs on the record: it was
  true of the annotation-driven implementation, whose databind layer sat on top of StAX and
  collapsed the two into one kind of text. Reading through StAX directly, with coalescing
  turned off, Woodstox reports a CDATA section as a distinct event from character data — the
  same text, a different event type — which was measured. The JDK's own StAX implementation
  does not, so the capability comes from Woodstox rather than from StAX itself.

  So the obstacle is gone and the decision now rests on scope alone. A reader is not the mirror
  image of a writer: this one is strict on purpose and refuses anything the specification does
  not allow, while a reader has no control over its input and must be forgiving of feeds with a
  misdeclared encoding, an undeclared entity, a date in one of a dozen shapes, or elements from
  an older version of RSS. Those are opposite dispositions to hold in one library.
  [Rome](http://rometools.github.io/rome/) reads RSS well and this release is not trying to
  replace it there.

## 1.1 and earlier

See the git history. The short version: Lombok-based builders, Jackson XML serialization,
`title`/`link`/`description`/`language`/`pubDate` on `<channel>`, `title`/`link`/`description`/
`category`/`pubDate` on `<item>`.
