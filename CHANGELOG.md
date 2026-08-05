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
- Gradle 7.1 → 9.6.1, Jackson 2.13.0 → 2.22.1, JUnit 5.7.0 → 6.1.2.

### Fixed

- `CDATAValue` content containing `]]>` no longer fails to serialize; it is split across two
  CDATA sections instead.
- `DateParser` no longer follows the platform locale, which could emit non-English day and
  month names and produce an unparseable feed.
- `RssOutput` no longer rebuilds its `XmlMapper` on every call.
- `RssOutput.output(Rss, OutputStream)` and `output(Rss, Writer)` no longer close the
  caller's stream. The javadoc always said "flushed but not closed"; Jackson's default is to
  close the target after writing, so that was not true until now. A caller using
  try-with-resources around their own stream would have hit a double-close.

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
