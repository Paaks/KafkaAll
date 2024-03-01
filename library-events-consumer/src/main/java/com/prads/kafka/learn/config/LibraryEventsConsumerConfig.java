package com.prads.kafka.learn.config;

import com.prads.kafka.learn.service.FailureService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.util.backoff.FixedBackOff;

import java.util.List;

@Configuration
@EnableKafka
@Slf4j
public class LibraryEventsConsumerConfig {

    public static final String RETRY = "RETRY";
    public static final String SUCCESS = "SUCCESS";
    public static final String DEAD = "DEAD";
    @Autowired
    KafkaTemplate kafkaTemplate;
    @Autowired
    FailureService failureService;

    @Value("${topics.retry}")
    private String retryTopic;
    @Value("${topics.dlt}")
    private String deadLetterTopic;

    @Bean
    ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> kafkaConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory);
        factory.setConcurrency(3);
        factory.setCommonErrorHandler(errorHandler());
        return factory;
    }

    public DefaultErrorHandler errorHandler() {
        var exceptiopnToIgnorelist = List.of(IllegalArgumentException.class);

        ExponentialBackOffWithMaxRetries expBackOff = new ExponentialBackOffWithMaxRetries(2);
        expBackOff.setInitialInterval(1_000L);
        expBackOff.setMultiplier(2.0);
        expBackOff.setMaxInterval(2_000L);

        var fixedBackoff = new FixedBackOff(1000L,2);
        var erroHandler = new DefaultErrorHandler(
                //publishingRecoverer(),
                consumerRecordRecoverer,
                fixedBackoff);
        erroHandler.setRetryListeners((consumerRecord, e, attempt) -> {
            log.info("Failed record in Retry Listener, Exception : {}, deliveryAttempt : {}",e.getMessage(),attempt);
        });
        return erroHandler;
    }

    public DeadLetterPublishingRecoverer publishingRecoverer(){
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate
                , (r, e) -> {
            log.error("Exception in publishingRecoverer : {} ", e.getMessage(), e);
            if (e.getCause() instanceof RecoverableDataAccessException) {
                return new TopicPartition(retryTopic, r.partition());
            } else {
                return new TopicPartition(deadLetterTopic, r.partition());
            }
        }
        );
        return recoverer;
    }

    ConsumerRecordRecoverer consumerRecordRecoverer = (consumerRecord, e) -> {
        log.error("Exception in publishingRecoverer : {} ", e.getMessage(), e);
        if (e.getCause() instanceof RecoverableDataAccessException) {
            log.info("Inside the recoverable logic");
            failureService.saveFailedRecord((ConsumerRecord<Integer, String>) consumerRecord, e, RETRY);
        } else {
            log.info("Inside the recoverable DEAD logic");
            failureService.saveFailedRecord((ConsumerRecord<Integer, String>) consumerRecord, e, DEAD);
        }
     };
}
