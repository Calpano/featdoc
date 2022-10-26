package de.xam.featdoc.markdown;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MarkdownToolTest {

    @Test
    void testFilename() {
        assertEquals("Foo%3A-Bär%2FBaz%2B-Boo,Boing", MarkdownTool.filename("Foo: Bär/Baz+-Boo,Boing"));
    }

    @Test
    void testHtmlId() {
        assertEquals("feature%3A-tis-case-event-prozessor", MarkdownTool.fragmentId("Feature: TIS Case-Event-Prozessor"));
    }


}