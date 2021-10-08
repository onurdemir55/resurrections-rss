# resurrections - rss

[![License: MIT](https://img.shields.io/badge/License-MIT-brightgreen.svg)](https://opensource.org/licenses/MIT)

Exactly yes the project name refers [The Matrix Resurrections](https://www.youtube.com/watch?v=9ix7TUGVYIo). _Oldies but Goldies. Back to
the RSS._

######

Rss is an acronym for Really Simple Syndication. Resurrections-rss is a very easy-peasy Java project for creating
RSS-2.0 feeds. This project code contains required mandatory and some optional rss elements. You are free to extend
this. One more thing and biggest advantages is that Support CDATA and Plain Text.

##### Why this project was needed ?

Firstly I have to say that the best project about creating Rss feed is [Rome](http://rometools.github.io/rome/).
Literally they did a great job. Congratulations to the whole contributors. Just one more improvement that we needed.
Just CDATA. Sorry but Rome not support that :(

If your output content is HTML, CDATA output more readable. (Encoded format is not preferable). Some News Editors,
checks the Rss News output manually, selects and decides _item_. So some customers can expect to see CDATA output.

### Features

* [x] Works on Java version 8 or later.
* [x] Elements support Plain Text and CDATA format. You can set your encoded data or you can set them as CDATA.
* [x] [jackson](https://github.com/FasterXML/jackson-dataformat-xml)  XML data format
* [x] [Lombok](https://github.com/projectlombok/lombok) keeps code clean and easy to use Builder Pattern
* [x] Easy and Peasy development
* [x] Simple and easy to extend for optional elements if needed. Just add fields to entity classes.
* [x] MIT licensed

#### Basic Setting Before Coding

* [x] Work with lombok, make [this](https://projectlombok.org/setup/intellij) basic setting.
* [x] Possible errors,
  check [this](https://stackoverflow.com/questions/9424364/cant-compile-project-when-im-using-lombok-under-intellij-idea)

#### Overview Info
* [orelly](https://www.oreilly.com/library/view/developing-feeds-with/0596008813/ch04s02.html)
* [w3](https://validator.w3.org/feed/docs/rss2.html)

## Examples

```java
        // creates plaint text holder
        Value plainText=new SimpleValue("simple value")

        // creates cdata holder
        Value cdata=new CDATAValue("cdata value")

```

---

Create a feed:

```java
        Set<Value> set = new HashSet<>(Arrays.asList(
                                            new SimpleValue("category-1"),
                                            new SimpleValue("category-2"),
                                            new SimpleValue("category-3"),
                                            new SimpleValue("category-3")));

        Item item = Item.builder()
                        .title(new SimpleValue("sample title"))
                        .category(set)
                        .description(new CDATAValue("sample description"))
                        .link(new CDATAValue("https://www.google.com/"))
                        .pubDate(new SimpleValue(DateParser.format_RFC1123_RFC822(new Date())))
                        .build();

        // items
        List<Item> items = Arrays.asList(item);


        Channel channel = Channel.builder()
                                .items(items)
                                .language(new SimpleValue("en"))
                                .pubDate(new SimpleValue(DateParser.format_RFC1123_RFC822(Instant.now())))
                                .title(new CDATAValue("sample title"))
                                .description(new CDATAValue("sample description"))
                                .link(new SimpleValue("https://www.google.com/"))
                                .build();


        Rss rss = Rss.builder()
                      .version("2.0")
                      .channel(channel)
                      .build();

        String xmlOutput = RssOutput.outputString(rss);
```

---



