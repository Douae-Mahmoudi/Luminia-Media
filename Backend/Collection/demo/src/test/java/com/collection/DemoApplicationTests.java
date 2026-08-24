package com.collection;

import com.collection.event.CommentAddedEvent;
import com.collection.event.MediaLikedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class DemoApplicationTests {

    @MockitoBean
    private KafkaTemplate<String, CommentAddedEvent> kafkaTemplateComment;

    @MockitoBean
    private KafkaTemplate<String, MediaLikedEvent> kafkaTemplateMediaLiked;

    @Test
    void contextLoads() {
    }
}
