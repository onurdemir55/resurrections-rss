# resurrections - rss

[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-brightgreen.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.onurdemir55/resurrections-rss.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.onurdemir55/resurrections-rss)
[![Java 17](https://img.shields.io/badge/Java-17%2B-blue.svg)](https://openjdk.org/projects/jdk/17/)

Exactly yes the project name refers [The Matrix Resurrections](https://www.youtube.com/watch?v=9ix7TUGVYIo). _Oldies but Goldies. Back to
the RSS._

######

RSS is an acronym for Really Simple Syndication, and resurrections-rss is an easy-peasy Java
library for creating RSS 2.0 feeds. It covers every element the specification describes — all
19 of `<channel>` and all 10 of `<item>` — and anything it does not describe can be added
through a namespace. Its biggest advantage is the one it was written for: every element can be
plain text or a CDATA section, and you choose which.

This is a producer, not a client: it builds and writes feeds, and does not parse an existing one. If you need to
consume RSS, [Rome](http://rometools.github.io/rome/) does that well.

##### Why this project was needed ?

Firstly I have to say that the best project about creating an RSS feed is [Rome](http://rometools.github.io/rome/).
They did a great job, and for almost everyone building a feed in Java, Rome is the right answer. Just one thing
was missing for what I needed: CDATA on the elements the specification actually points at for markup, like
`description`. Rome writes exactly one CDATA section — `content:encoded`, hardcoded in its content module — and
escapes everything else, `description` included. That is a fair scope for a library that size; it just was not
the scope I needed.

If your output content is HTML, a CDATA section is what a reader shows unescaped, tags and all, instead of
entity-encoded markup a person has to decode by eye. Some news editors check the RSS output manually before an
item goes out, so this is not only about what a parser accepts — it is about what a reader shows.

### Installation

Gradle:

```groovy
dependencies {
    implementation 'io.github.onurdemir55:resurrections-rss:2.0'
}
```

Maven:

```xml
<dependency>
    <groupId>io.github.onurdemir55</groupId>
    <artifactId>resurrections-rss</artifactId>
    <version>2.0</version>
</dependency>
```

That is the whole dependency list. [Woodstox](https://github.com/FasterXML/woodstox) comes with
it at runtime, because it is what writes the XML, but nothing extra reaches your compile
classpath — no JSON library, no annotation processor, nothing to configure.

### Features

* [x] Requires Java 17 or later.
* [x] Every element the specification describes: all 19 of `<channel>`, all 10 of `<item>`.
* [x] Elements support Plain Text and CDATA format. You can set your encoded data or you can set them as CDATA.
* [x] Anything the specification does not describe can be added through a namespace, plain or
  CDATA like everything else.
* [x] The rules the specification states are checked when you build the feed, not discovered
  later by a reader that refuses it. There is no lenient mode.
* [x] The feed is written element by element through [StAX](https://docs.oracle.com/javase/tutorial/jaxp/stax/index.html),
  so what XML comes out is decided in one readable place rather than inferred from annotations.
* [x] One dependency: [woodstox](https://github.com/FasterXML/woodstox), the StAX implementation.
  No annotation processor, no IDE plugin, no build-time magic, no reflection.
* [x] A real module descriptor, named `io.github.onurdemir55.resurrections.rss`. Four packages
  are exported; the validation helpers are not, so they stay out of the API you depend on.
* [x] Immutable feed model with builders, and a getter for every element so a feed you built
  can be inspected afterwards.
* [x] Easy and Peasy development
* [x] Apache 2.0 licensed

#### Overview Info
* [O'Reilly](https://www.oreilly.com/library/view/developing-feeds-with/0596008813/ch04s02.html)
* [w3](https://validator.w3.org/feed/docs/rss2.html)
* [RSS 2.0 Specification](https://www.rssboard.org/rss-specification)

## Examples

An element's text is either plain or a CDATA section, and that is the whole decision:

```java
        // plain text: XML special characters are escaped
        Value plainText = new PlainValue("Profit rose 18% & the dividend went up");

        // CDATA: markup stays readable
        Value cdata = new CDATAValue("<p>Profit rose <b>18%</b></p>");
```

In a feed those two come out like this:

```xml
    <title>Profit rose 18% &amp; the dividend went up</title>
    <description><![CDATA[<p>Profit rose <b>18%</b></p>]]></description>
```

Neither is more correct than the other. Plain text is right when the value *is* text, and a
reader that renders the escaped ampersand shows the character you meant. CDATA is right when the
value is markup you want a reader to render, which is the case this library was written for.

Either way the text arrives as you wrote it. Runs of spaces are not collapsed, tabs stay tabs,
leading and trailing whitespace is kept, and a carriage return survives even inside a CDATA
section, where nothing can be escaped — so an aligned table, a `<pre>` block or an indented
listing comes out of a reader with its columns still lined up. The indentation you see in the
XML below is between elements, never inside one.

---

Create a feed. The title is plain text and the description is markup, which is the choice this
library exists to let you make:

```java
        String published = DateParser.formatRfc822(Instant.parse("2026-02-19T08:30:00Z"));

        Item item = Item.builder()
                        .title(new PlainValue("Onur Demir Holding beats forecasts & lifts its dividend"))
                        .link(new PlainValue("https://example.com/2026/02/onur-demir-holding-dividend"))
                        .description(new CDATAValue(
                                "<p>Full-year profit rose <b>18%</b> and the board raised the "
                                + "dividend to <b>$1.24</b>. Read the "
                                + "<a href=\"/2026/02/onur-demir-holding-dividend\">full report</a>.</p>"))
                        .categories(Category.of("Earnings"), Category.of("Equities"))
                        .guid(Guid.of("https://example.com/2026/02/onur-demir-holding-dividend", true))
                        .pubDate(new PlainValue(published))
                        .build();

        Channel channel = Channel.builder()
                                 .title(new PlainValue("Markets & Mornings"))
                                 .link(new PlainValue("https://example.com/"))
                                 .description(new CDATAValue(
                                         "<p>Good news from the markets, before your "
                                         + "<i>first coffee</i>.</p>"))
                                 .language(new PlainValue("en-us"))
                                 .pubDate(new PlainValue(published))
                                 .items(item)
                                 .build();

        // version defaults to "2.0"
        Rss rss = Rss.builder().channel(channel).build();

        String xmlOutput = RssOutput.outputString(rss);
```

```xml
<?xml version='1.0' encoding='UTF-8'?>
<rss version="2.0">
  <channel>
    <title>Markets &amp; Mornings</title>
    <link>https://example.com/</link>
    <description><![CDATA[<p>Good news from the markets, before your <i>first coffee</i>.</p>]]></description>
    <language>en-us</language>
    <pubDate>Thu, 19 Feb 2026 08:30:00 GMT</pubDate>
    <item>
      <title>Onur Demir Holding beats forecasts &amp; lifts its dividend</title>
      <link>https://example.com/2026/02/onur-demir-holding-dividend</link>
      <description><![CDATA[<p>Full-year profit rose <b>18%</b> and the board raised the dividend to <b>$1.24</b>. Read the <a href="/2026/02/onur-demir-holding-dividend">full report</a>.</p>]]></description>
      <category>Earnings</category>
      <category>Equities</category>
      <guid isPermaLink="true">https://example.com/2026/02/onur-demir-holding-dividend</guid>
      <pubDate>Thu, 19 Feb 2026 08:30:00 GMT</pubDate>
    </item>
  </channel>
</rss>
```

Both forms are visible in one document. The ampersand in the plain title came out as `&amp;`,
because that is the only way a title can carry one. The markup in the description came out as
you wrote it, tags and all, because it is inside a CDATA section — and the `&amp;` a reader
would otherwise have to decode is not there to decode. A feed reader shows the first as text
and renders the second as HTML.

`ReadmeExampleTest` compares this block against what the code produces, so the example cannot
drift from the library. `ExactOutputTest` goes further and pins two whole documents byte for
byte, indentation included, because the indentation is written by hand here rather than supplied
by a library.

---

Write somewhere other than a `String`:

```java
        RssOutput.output(rss, Path.of("feed.xml"));   // UTF-8
        RssOutput.output(rss, outputStream);          // UTF-8
        RssOutput.output(rss, writer);                // encoding is the writer's business
```

---

### Extending the feed

The specification permits elements it does not describe, on one condition: they must be
defined in a namespace. So a declaration goes on the root element and the prefix goes on the
element name.

```java
        Item item = Item.builder()
                        .title(new PlainValue("An item"))
                        .extension("content:encoded", new CDATAValue("<p>rich <b>html</b></p>"))
                        .extension("dc:creator", new PlainValue("Onur Demir"))
                        .build();

        Channel channel = Channel.builder()
                                 .title(new PlainValue("Sample"))
                                 .link(new PlainValue("https://example.com/"))
                                 .description(new PlainValue("Sample feed"))
                                 // the RSS Best Practices Profile asks for this one
                                 .atomLink(AtomLink.self("https://example.com/feed.xml"))
                                 .items(item)
                                 .build();

        Rss rss = Rss.builder()
                     .namespace("content", "http://purl.org/rss/1.0/modules/content/")
                     .namespace("dc", "http://purl.org/dc/elements/1.1/")
                     .channel(channel)
                     .build();
```

```xml
<rss version="2.0" xmlns:content="http://purl.org/rss/1.0/modules/content/"
     xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:atom="http://www.w3.org/2005/Atom">
  <channel>
    <title>Sample</title>
    <link>https://example.com/</link>
    <description>Sample feed</description>
    <atom:link href="https://example.com/feed.xml" rel="self" type="application/rss+xml"/>
    <item>
      <title>An item</title>
      <content:encoded><![CDATA[<p>rich <b>html</b></p>]]></content:encoded>
      <dc:creator>Onur Demir</dc:creator>
    </item>
  </channel>
</rss>
```

`xmlns:atom` is declared without being asked for, because `atomLink` was used and a prefixed
element without its declaration is not well-formed XML. Extension elements take a `Value`,
so they choose plain text or CDATA like everything else.

The root element above is wrapped across two lines so it fits on the page; the library writes
it on one. Everything else is exactly what comes out, and `ReadmeExtendingExampleTest` compares
this block against the real output to keep it that way. `ExactOutputTest` pins two whole
documents byte for byte, indentation included, since that is written by hand here rather than
supplied by a library.

`Rss`, `Channel` and `Item` expose a getter for every element, so a feed you built can be
inspected afterwards, and a `toString` that fits on one line for when it is not what you
expected. Reading an existing feed, however, is out of scope; see
[Why this project was needed](#why-this-project-was-needed-) above.

---

### Serving it over HTTP

Almost every feed ends up behind an HTTP endpoint, and two headers decide whether a reader
handles it or a browser downloads it as a file:

| Header | Value | Why |
|---|---|---|
| `Content-Type` | `application/rss+xml; charset=UTF-8` | Without it a framework may send `text/plain`, and a browser then shows the markup instead of subscribing. The media type is also available as `AtomLink.RSS_MEDIA_TYPE`, since `atom:link` needs the same string. |
| `Last-Modified` | when the feed last changed | Readers poll in minutes. Answering `304 Not Modified` to a conditional request costs nothing to send and saves almost all of the bandwidth. |

The charset is not optional. The document declares `UTF-8` in its own prolog, so a header that
says something else, or says nothing and lets the framework guess, leaves a reader with two
answers and no way to choose. That is where non-ASCII titles turn into question marks.

For a large feed prefer `output(rss, outputStream)` over `outputString`: it writes straight to
the response and never holds the whole document in memory. The stream is flushed and left open,
which is what a servlet container expects.

---

### Validation

The rules the specification states are enforced when you build, not discovered later by a
reader that refuses your feed. There is no lenient mode.

```java
        Channel.builder().title(new PlainValue("t")).build();
        // IllegalStateException: channel requires link; the specification lists
        // title, link and description as required elements

        Item.builder().link(new PlainValue("https://example.com/a")).build();
        // IllegalStateException: an item requires a title or a description

        Channel.builder()
               .title(new PlainValue("t"))
               .link(new PlainValue("www.example.com"))
               .description(new PlainValue("d"))
               .build();
        // IllegalArgumentException: channel link must begin with a URI scheme
```

What is checked:

* `channel` requires `title`, `link` and `description`
* an `item` requires a `title` or a `description`
* `rss` must declare `version="2.0"`, which is also the default
* `link` and `url` values must carry a URI scheme; any scheme is accepted, not just `http`
* an `enclosure` url must be `http` or `https`, as the specification says explicitly
* an `image` is at most 144 wide and 400 tall
* a `skipHours` hour is between 0 and 23, and neither `skipHours` nor `skipDays` may repeat
  a value or be empty
* elements the specification marks required cannot be `null`, and neither can element text.
  To leave an element out of the feed, do not set it.
* an extension or namespace name has to be spelled in ASCII letters, digits, hyphens, dots and
  underscores, and `xml` and `xmlns` cannot be declared as prefixes because XML reserves them.
  This is narrower than XML allows, on purpose: the parser that ships with the JDK refuses some
  names the current edition of XML permits, so matching the specification exactly would mean
  emitting feeds a common reader will not parse. Every extension module in use — `dc`,
  `content`, `itunes`, `media`, `slash`, `sy`, `georss`, `wfw`, `admin` — is ASCII anyway.
  Element *text* is unrestricted; only names are. And a name matters more than text does:
  text is escaped on the way out, but a name is written as markup, so an unchecked name would
  not produce an escaped oddity but a broken document
* an extension element has to carry a namespace prefix that was declared on the document. This
  is checked by `Rss.builder().build()`, the one place that knows both the prefixes in use and
  the declarations, and it is worth checking because the mistake is otherwise silent: the feed
  is produced, looks correct, and is rejected by the first reader that receives it
* element text and attribute values cannot contain a character XML has no spelling for, such as
  a NUL byte. No amount of escaping and no CDATA section can encode one, so this is caught when
  the value is created rather than part-way through writing the feed, which on a stream would
  leave half a document already sent



