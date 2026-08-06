package io.github.onurdemir55.resurrections.rss.io;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Compares every feed in {@link FeedCorpus} against a recorded copy of the output, character
 * for character, with nothing normalized.
 * <p>
 * The recorded copy was produced by the previous, annotation-driven implementation: 294 of the
 * 297 feeds came out of both byte for byte, and the three that differed did so by one line
 * each, all the same intended change — a channel-level extension element moving ahead of the
 * items. So this file is not merely a snapshot of what the code happens to do; it is what the
 * code did before the rewrite, which is the only baseline worth having.
 * <p>
 * To change the output deliberately, run this class with {@code -Dcorpus.record=true}, read
 * the resulting diff line by line, and commit it as part of the change that caused it. Doing
 * that without reading the diff would throw away the only thing here worth anything.
 */
class FeedCorpusRegressionTest {

    private static final String RESOURCE = "/corpus/feeds.txt";
    private static final Path SOURCE = Path.of("src/test/resources/corpus/feeds.txt");
    private static final String SEPARATOR = "=== ";

    @Test
    @DisplayName("every feed still renders exactly as recorded")
    void matchesRecordedOutput() throws IOException {
        Map<String, String> actual = FeedCorpus.feeds();

        if (Boolean.getBoolean("corpus.record")) {
            record(actual);
            fail("corpus re-recorded; review the diff and run again without -Dcorpus.record");
        }

        Map<String, String> expected = readRecorded();

        assertEquals(expected.keySet(), actual.keySet(),
                "the corpus gained or lost feeds; re-record it with -Dcorpus.record=true");

        List<String> changed = new ArrayList<>();
        for (Map.Entry<String, String> entry : expected.entrySet()) {
            if (!entry.getValue().equals(actual.get(entry.getKey()))) {
                changed.add(entry.getKey());
            }
        }
        if (!changed.isEmpty()) {
            String name = changed.get(0);
            assertEquals(expected.get(name), actual.get(name),
                    changed.size() + " feed(s) changed: " + changed + ". First one shown.");
        }
    }

    @Test
    @DisplayName("and every feed is well-formed XML")
    void allWellFormed() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        List<String> broken = new ArrayList<>();
        FeedCorpus.feeds().forEach((name, xml) -> {
            try {
                factory.newDocumentBuilder().parse(
                        new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            } catch (Exception e) {
                broken.add(name + " :: " + e.getMessage());
            }
        });

        assertTrue(broken.isEmpty(), () -> "feeds a parser rejects: " + broken);
    }

    @Test
    @DisplayName("the corpus is large enough to be worth having")
    void corpusIsNotEmpty() {
        assertTrue(FeedCorpus.feeds().size() >= 297,
                () -> "the corpus shrank to " + FeedCorpus.feeds().size() + " feeds");
    }

    private static Map<String, String> readRecorded() throws IOException {
        try (InputStream in = FeedCorpusRegressionTest.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException(RESOURCE + " is missing; record it with "
                        + "-Dcorpus.record=true");
            }
            return parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    /**
     * One file rather than 297, so the whole corpus shows up as a single reviewable diff.
     * Feeds are separated by a line naming the next one; a feed's own text can never start a
     * line with the separator, because every line of a feed is either XML or indentation.
     */
    private static Map<String, String> parse(final String recorded) {
        Map<String, String> feeds = new LinkedHashMap<>();
        String name = null;
        StringBuilder body = new StringBuilder();

        String[] lines = recorded.split("\n", -1);
        // Every feed ends with a newline, so the file does too, and splitting on the newline
        // leaves one empty segment after it. Keeping it would give the last feed an extra
        // blank line that no feed actually has.
        int count = lines.length > 0 && lines[lines.length - 1].isEmpty()
                ? lines.length - 1
                : lines.length;

        for (int i = 0; i < count; i++) {
            String line = lines[i];
            if (line.startsWith(SEPARATOR)) {
                if (name != null) {
                    feeds.put(name, body.toString());
                }
                name = line.substring(SEPARATOR.length());
                body.setLength(0);
            } else if (name != null) {
                body.append(line).append('\n');
            }
        }
        if (name != null) {
            feeds.put(name, body.toString());
        }
        return feeds;
    }

    private static void record(final Map<String, String> feeds) throws IOException {
        StringBuilder out = new StringBuilder();
        feeds.forEach((name, xml) -> out.append(SEPARATOR).append(name).append('\n').append(xml));
        Path parent = SOURCE.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(SOURCE, out.toString(), StandardCharsets.UTF_8);
    }
}
