package io.github.onurdemir55.resurrections.rss.io;

import io.github.onurdemir55.resurrections.rss.feed.Channel;
import io.github.onurdemir55.resurrections.rss.feed.Item;
import io.github.onurdemir55.resurrections.rss.feed.Rss;
import io.github.onurdemir55.resurrections.rss.feed.holder.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Fails when the model gains an element that {@link RssWriter} does not write.
 * <p>
 * This is the one real cost of writing XML by hand instead of deriving it from annotations:
 * the element list lives in the writer, so a field added to {@link Channel} or {@link Item}
 * and forgotten there is simply absent from every feed, quietly, with the build still green.
 * It was verified that nothing else catches this — a field was added to {@code Channel},
 * left out of the writer, and all other tests passed.
 * <p>
 * So it is caught here instead, by asking the model what it has rather than by remembering.
 * Every public getter on {@code Channel} and {@code Item} has to be set in the sample feed
 * below, and every value set has to leave a trace in the output. Adding an element to the
 * model therefore fails this test twice: once for not being in the sample, and once for not
 * being written.
 */
class WriterCoversModelTest {

    /**
     * Getters whose element is not simply the property name. A repeatable element is plural in
     * Java and singular in the feed - a {@code List<Category>} is written as several
     * {@code <category>} elements, which the specification names in the singular - and
     * {@code atom:link} is named differently from the accessor that returns it.
     */
    private static final Map<String, String> ELEMENT_NAMES = Map.of(
            "getItems", "<item",
            "getCategories", "<category",
            "getAtomLink", "<atom:link");

    /** Not an element in its own right; checked through the names it was registered under. */
    private static final String EXTENSIONS = "getExtensions";

    @Test
    @DisplayName("every element the model can hold is written by RssWriter")
    void writerCoversChannel() throws ReflectiveOperationException {
        Rss rss = ExactOutputSample.feed();
        String xml = RssOutput.outputString(rss);

        assertCovered(Channel.class, rss.getChannel(), xml, "Channel");
        assertCovered(Item.class, rss.getChannel().getItems().get(0), xml, "Item");
    }

    private static void assertCovered(final Class<?> type, final Object instance,
                                      final String xml, final String label)
            throws ReflectiveOperationException {
        List<String> unset = new ArrayList<>();
        List<String> unwritten = new ArrayList<>();

        for (Method getter : new TreeMap<>(gettersOf(type)).values()) {
            Object value = getter.invoke(instance);

            if (value == null) {
                unset.add(getter.getName());
                continue;
            }
            if (EXTENSIONS.equals(getter.getName())) {
                @SuppressWarnings("unchecked")
                Map<String, Value> extensions = (Map<String, Value>) value;
                if (extensions.isEmpty()) {
                    unset.add(getter.getName());
                }
                extensions.keySet().stream()
                        .filter(name -> !xml.contains("<" + name))
                        .forEach(unwritten::add);
                continue;
            }
            String element = ELEMENT_NAMES.getOrDefault(getter.getName(), elementFor(getter));
            if (!xml.contains(element)) {
                unwritten.add(getter.getName() + " (expected " + element + ")");
            }
        }

        assertTrue(unset.isEmpty(), () -> label + " has elements the sample feed never sets, so "
                + "this test cannot tell whether they are written: " + unset
                + ". Set them in ExactOutputSample.feed().");
        assertTrue(unwritten.isEmpty(), () -> label + " has elements missing from the output: "
                + unwritten + ". Add them to RssWriter, or they will be absent from every feed.");
    }

    private static Map<String, Method> gettersOf(final Class<?> type) {
        Map<String, Method> getters = new TreeMap<>();
        for (Method method : type.getMethods()) {
            boolean isGetter = method.getName().startsWith("get")
                    && method.getParameterCount() == 0
                    && method.getDeclaringClass().equals(type);
            if (isGetter) {
                getters.put(method.getName(), method);
            }
        }
        return getters;
    }

    /** {@code getLastBuildDate} describes an element called {@code lastBuildDate}. */
    private static String elementFor(final Method getter) {
        String name = getter.getName().substring("get".length());
        return "<" + Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
}
