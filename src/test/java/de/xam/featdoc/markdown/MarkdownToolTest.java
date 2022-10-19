package de.xam.featdoc.markdown;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MarkdownToolTest {

    @Test
    void testHtmlId() {
        assertEquals(MarkdownTool.fragmentid("Feature: TIS Case-Event-Prozessor"), "feature%3A-tis-case-event-prozessor");
    }


}