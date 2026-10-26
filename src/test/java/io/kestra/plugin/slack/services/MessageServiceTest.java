package io.kestra.plugin.slack.services;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.core.serializers.JacksonMapper;

import jakarta.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@KestraTest
class MessageServiceTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    void shouldWrapNonObjectJsonAsText() {
        assertAll(Stream.of("123", "true", "null", "[1, 2]", "\"quoted\"").map(messageText -> () ->
        {
            String json = MessageService.prepareMessageAsJson(runContextFactory.of(), null, Property.ofValue(messageText));

            assertThat(json).as("messageText: %s", messageText).isEqualTo(
                JacksonMapper.ofJson().writeValueAsString(
                    JacksonMapper.ofJson().createObjectNode().put("text", messageText)
                )
            );
        }));
    }

    @Test
    void shouldPreserveJsonObject() throws Exception {
        String messageText = "{\"text\":\"**bold**\",\"blocks\":[]}";

        String json = MessageService.prepareMessageAsJson(runContextFactory.of(), null, Property.ofValue(messageText));

        assertThat(json).isEqualTo(messageText);
    }

    @Test
    void shouldWrapMarkdownAsSlackMrkdwn() throws Exception {
        String messageText = "**bold** __italic__ [link](https://example.com)";

        String json = MessageService.prepareMessageAsJson(runContextFactory.of(), null, Property.ofValue(messageText));

        assertThat(json).isEqualTo("{\"text\":\"*bold* _italic_ <https://example.com|link>\"}");
    }

    @Test
    void shouldPreferPayloadOverMessageText() throws Exception {
        String payload = "{\"text\":\"**bold**\",\"blocks\":[]}";

        String json = MessageService.prepareMessageAsJson(runContextFactory.of(), Property.ofValue(payload), Property.ofValue("123"));

        assertThat(json).isEqualTo(payload);
    }
}
