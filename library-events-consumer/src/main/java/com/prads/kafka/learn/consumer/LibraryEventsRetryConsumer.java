package com.prads.kafka.learn.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prads.kafka.learn.service.LibraryEventService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LibraryEventsRetryConsumer {
    @Autowired
    private LibraryEventService libraryEventService;

    @KafkaListener(topics = {"${topics.retry}"},
            autoStartup = "${retryListener.startup:true}",
            groupId = "retry-listner-group")
    public void onMessage(ConsumerRecord<Integer,String> consumerRecord) throws JsonProcessingException {

        log.info("Consumer record Retry : {}",consumerRecord);
        libraryEventService.processLibraryEvent(consumerRecord);
    }
}
