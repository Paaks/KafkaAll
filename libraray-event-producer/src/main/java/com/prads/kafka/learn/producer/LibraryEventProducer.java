package com.prads.kafka.learn.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prads.kafka.learn.domain.LibraryEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
public class LibraryEventProducer {

    @Value("${spring.kafka.topic}")
    private String topic;
    private final KafkaTemplate<Integer,String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public LibraryEventProducer(KafkaTemplate<Integer, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendLibraryEvent(LibraryEvent libraryEvent) throws JsonProcessingException {
        var key = libraryEvent.libraryEventId().intValue();
        var value = objectMapper.writeValueAsString(libraryEvent);
        
        var completableFuture = kafkaTemplate.send(topic, key, value);
        
        completableFuture
                .whenComplete((sendResult, throwable) -> {
                    if(null != throwable){
                        handleFailure(key,value,throwable);
                    }else {
                        handleSuccess(key,value,sendResult);
                    }
        });
    }

    public void sendLibraryEvent_approach2(LibraryEvent libraryEvent) throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
        var key = libraryEvent.libraryEventId().intValue();
        var value = objectMapper.writeValueAsString(libraryEvent);

        SendResult<Integer, String> sendResult = kafkaTemplate.send(topic, key, value).get(3, TimeUnit.SECONDS);

        log.info("Message send synchronously {}", sendResult.getProducerRecord().value());
    }
    public void sendLibraryEvent_approach3(LibraryEvent libraryEvent) throws JsonProcessingException {
        var key = libraryEvent.libraryEventId().intValue();
        var value = objectMapper.writeValueAsString(libraryEvent);

        var completableFuture = kafkaTemplate.send(new ProducerRecord<>(topic,key,value));

        completableFuture
                .whenComplete((sendResult, throwable) -> {
                    if(null != throwable){
                        handleFailure(key,value,throwable);
                    }else {
                        handleSuccess(key,value,sendResult);
                    }
                });
    }
    public void sendLibraryEvent_approach4(LibraryEvent libraryEvent) throws JsonProcessingException {
        var key = libraryEvent.libraryEventId().intValue();
        var value = objectMapper.writeValueAsString(libraryEvent);

        var completableFuture = kafkaTemplate.send(buildProducerRecord(key,value));

        completableFuture
                .whenComplete((sendResult, throwable) -> {
                    if(null != throwable){
                        handleFailure(key,value,throwable);
                    }else {
                        handleSuccess(key,value,sendResult);
                    }
                });
    }

    private ProducerRecord<Integer, String> buildProducerRecord(int key, String value) {
        List<Header> headers = List.of(new RecordHeader("event-source", "scanner".getBytes()));
        return new ProducerRecord<>(topic,null,key,value,headers);

    }

    private void handleFailure(Integer key, String value, Throwable throwable) {
        log.info("Message failed : {}", throwable.getMessage(), throwable);
    }

    private void handleSuccess(Integer key, String value, SendResult<Integer, String> sendResult) {
        log.info("Message is success : Key {} and value {}, partition is {}",key,value,sendResult.getRecordMetadata().partition());
    }
}


