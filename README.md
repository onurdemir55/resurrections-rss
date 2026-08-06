# resurrections - rss

[![License: MIT](https://img.shields.io/badge/License-MIT-brightgreen.svg)](https://opensource.org/licenses/MIT)

Exactly yes the project name refers [The Matrix Resurrections](https://www.youtube.com/watch?v=9ix7TUGVYIo). _Oldies but Goldies. Back to
the RSS._

######

Rss is an acronym for Really Simple Syndication. Resurrections-rss is a very easy-peasy Java project for creating
RSS-2.0 feeds. This project code contains required mandatory and some optional rss elements. You are free to extend
this. One more thing and biggest advantages is that Support CDATA and Plain Text.

This is a producer, not a client: it builds and writes feeds, and does not parse an existing one. If you need to
consume RSS, [Rome](http://rometools.github.io/rome/) does that well.

##### Why this project was needed ?

Firstly I have to say that the best project about creating Rss feed is [Rome](http://rometools.github.io/rome/).
Literally they did a great job. Congratulations to the whole contributors. Just one more improvement that we needed.
Just CDATA. Sorry but Rome not support that :(

If your output content is HTML, CDATA output more readable. (Encoded format is not preferable). Some News Editors,
checks the Rss News output manually, selects and decides _item_. So some customers can expect to see CDATA output.

### Features

* [x] Requires Java 17 or later.
* [x] Elements support Plain Text and CDATA format. You can set your encoded data or you can set them as CDATA.
* [x] The feed is written element by element through [StAX](https://docs.oracle.com/javase/tutorial/jaxp/stax/index.html),
  so what XML comes out is decided in one readable place rather than inferred from annotations.
* [x] One dependency: [woodstox](https://github.com/FasterXML/woodstox), the StAX implementation.
  No annotation processor, no IDE plugin, no build-time magic, no reflection.
* [x] Immutable feed model with builders
* [x] Easy and Peasy development
* [x] Simple and easy to extend for optional elements if needed. Just add fields to entity classes.
* [x] MIT licensed

#### Overview Info
* [orelly](https://www.oreilly.com/library/view/developing-feeds-with/0596008813/ch04s02.html)
* [w3](https://validator.w3.org/feed/docs/rss2.html)
* [RSS 2.0 Specification](https://www.rssboard.org/rss-specification)

## Examples

```java
        // plain text: XML special characters are escaped
        Value plainText = new SimpleValue("simple value");

        // CDATA: markup stays readable
        Value cdata = new CDATAValue("<b>cdata value</b>");
```

---

Create a feed:

```java
        String published = DateParser.formatRfc822(Instant.now());

        Item item = Item.builder()
                        .title(new SimpleValue("sample title"))
                        .link(new CDATAValue("https://www.google.com/"))
                        .description(new CDATAValue("sample description"))
                        .category(Category.of("category-1"), Category.of("category-2"))
                        .pubDate(new SimpleValue(published))
                        .build();

        Channel channel = Channel.builder()
                                 .title(new CDATAValue("sample title"))
                                 .link(new SimpleValue("https://www.google.com/"))
                                 .description(new CDATAValue("sample description"))
                                 .language(new SimpleValue("en"))
                                 .pubDate(new SimpleValue(published))
                                 .items(List.of(item))
                                 .build();

        // version defaults to "2.0"
        Rss rss = Rss.builder().channel(channel).build();

        String xmlOutput = RssOutput.outputString(rss);
```

```xml
<?xml version='1.0' encoding='UTF-8'?>
<rss version="2.0">
  <channel>
    <title><![CDATA[sample title]]></title>
    <link>https://www.google.com/</link>
    <description><![CDATA[sample description]]></description>
    <language>en</language>
    <pubDate>Sat, 07 Sep 2002 00:00:01 GMT</pubDate>
    <item>
      <title>sample title</title>
      <link><![CDATA[https://www.google.com/]]></link>
      <description><![CDATA[sample description]]></description>
      <category>category-1</category>
      <category>category-2</category>
      <pubDate>Sat, 07 Sep 2002 00:00:01 GMT</pubDate>
    </item>
  </channel>
</rss>
```

This exact output is asserted by `ReadmeExampleTest`, so the example cannot drift from
what the library actually produces.

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
                        .title(new SimpleValue("An item"))
                        .extension("content:encoded", new CDATAValue("<p>rich <b>html</b></p>"))
                        .extension("dc:creator", new SimpleValue("Onur Demir"))
                        .build();

        Channel channel = Channel.builder()
                                 .title(new SimpleValue("Sample"))
                                 .link(new SimpleValue("https://example.com/"))
                                 .description(new SimpleValue("Sample feed"))
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

`Rss`, `Channel` and `Item` also expose a getter for every element, so a feed you built can
be inspected afterwards. Reading an existing feed, however, is out of scope; see
[Why this project was needed](#why-this-project-was-needed-) above.

---

### Validation

The rules the specification states are enforced when you build, not discovered later by a
reader that refuses your feed. There is no lenient mode.

```java
        Channel.builder().title(new SimpleValue("t")).build();
        // IllegalStateException: channel requires link; the specification lists
        // title, link and description as required elements

        Item.builder().link(new SimpleValue("https://example.com/a")).build();
        // IllegalStateException: an item requires a title or a description

        Channel.builder()
               .title(new SimpleValue("t"))
               .link(new SimpleValue("www.example.com"))
               .description(new SimpleValue("d"))
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



