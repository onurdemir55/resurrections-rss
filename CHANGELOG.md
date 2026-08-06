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
- `Channel.getExtensions()` and `Item.getExtensions()` are public. They were hidden only
  because a serialization framework used to find them by reflection.

### Not added

- Reading or parsing an existing feed. This library produces feeds; it does not consume
  them. A prototype was measured: a CDATA section and escaped text are indistinguishable
  once parsed, so reading and re-writing a feed would silently turn every CDATA element into
  plain text, which defeats the reason this library exists. [Rome](http://rometools.github.io/rome/)
  reads RSS well; this library is not trying to replace it there.

## 1.1 and earlier

See the git history. The short version: Lombok-based builders, Jackson XML serialization,
`title`/`link`/`description`/`language`/`pubDate` on `<channel>`, `title`/`link`/`description`/
`category`/`pubDate` on `<item>`.
