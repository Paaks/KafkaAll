package com.prads.kafka.learn.scheduler;

import com.prads.kafka.learn.config.LibraryEventsConsumerConfig;
import com.prads.kafka.learn.entity.FailureRecord;
import com.prads.kafka.learn.jpa.FailureRecordRepository;
import com.prads.kafka.learn.service.LibraryEventService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RetryScheduler {

    private FailureRecordRepository failureRecordRepository;
    private LibraryEventService libraryEventService;

    public RetryScheduler(FailureRecordRepository failureRecordRepository, LibraryEventService libraryEventService) {
        this.failureRecordRepository = failureRecordRepository;
        this.libraryEventService = libraryEventService;
    }
    @Scheduled(fixedRate = 10000)
    public void retryRecords(){
        log.info("Retrying Failed Records Started!");
        failureRecordRepository.findAllByStatus(LibraryEventsConsumerConfig.RETRY)
                .forEach(failureRecord -> {
                    try {
                        //libraryEventsService.processLibraryEvent();
                        var consumerRecord = buildConsumerRecord(failureRecord);
                        libraryEventService.processLibraryEvent(consumerRecord);
                        // libraryEventsConsumer.onMessage(consumerRecord); // This does not involve the recovery code for in the consumerConfig
                        failureRecord.setStatus(LibraryEventsConsumerConfig.SUCCESS);
                    } catch (Exception e){
                        log.error("Exception in retryFailedRecords : ", e);
                    }

                });

    }

    private ConsumerRecord<Integer, String> buildConsumerRecord(FailureRecord failureRecord) {
        return new ConsumerRecord<>(failureRecord.getTopic(),
                failureRecord.getPartition(), failureRecord.getOffset_value(), failureRecord.getKey_value(),
                failureRecord.getErrorRecord());

    }
}
