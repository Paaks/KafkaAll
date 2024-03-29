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
public class LibraryEventsConsumer {
    @Autowired
    private LibraryEventService libraryEventService;
    @KafkaListener(topics = {"library-events"}, groupId = "library-events-listener-group")
    public void onMessage(ConsumerRecord<Integer,String> consumerRecord) throws JsonProcessingException {

        log.info("Consumer record : {}",consumerRecord);
        libraryEventService.processLibraryEvent(consumerRecord);
    }
}
