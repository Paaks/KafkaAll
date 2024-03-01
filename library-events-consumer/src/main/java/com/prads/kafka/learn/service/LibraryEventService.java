package com.prads.kafka.learn.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prads.kafka.learn.entity.LibraryEvent;
import com.prads.kafka.learn.jpa.LibraryEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class LibraryEventService {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private LibraryEventRepository libraryEventRepository;
    public void processLibraryEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        LibraryEvent libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);
        log.info("libraryevent : {}",libraryEvent);

        if(libraryEvent.getLibraryEventId()!=null && ( libraryEvent.getLibraryEventId()==999 )){
            throw new RecoverableDataAccessException("Temporary Network Issue");
        }

        switch (libraryEvent.getLibraryEventType()){
            case NEW : save(libraryEvent);
                break;
            case UPDATE: update(libraryEvent);
                break;
            default:
                log.info("Wrong choice !");
        }
    }

    private void update(LibraryEvent libraryEvent) {
        if(libraryEvent.getLibraryEventId() == null)
            throw new IllegalArgumentException("Library id is missing !");

        Optional<LibraryEvent> libraryEventByid = libraryEventRepository.findById(libraryEvent.getLibraryEventId());
        if(!libraryEventByid.isPresent())
            throw new IllegalArgumentException("Library id is missing in db!");
        log.info("Library event id from db : {}",libraryEventByid.get().getLibraryEventId());
        save(libraryEvent);
    }

    private void save(LibraryEvent libraryEvent) {
        libraryEvent.getBook().setLibraryEvent(libraryEvent);
        libraryEventRepository.save(libraryEvent);
        log.info("Libraryevent is persisted, {}",libraryEvent);
    }
}
