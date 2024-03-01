package com.prads.kafka.learn.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prads.kafka.learn.domain.LibraryEvent;
import com.prads.kafka.learn.domain.LibraryEventType;
import com.prads.kafka.learn.producer.LibraryEventProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class LibraryEventController {

    private final LibraryEventProducer libraryEventProducer;

    public LibraryEventController(LibraryEventProducer libraryEventProducer) {
        this.libraryEventProducer = libraryEventProducer;
    }

    @PostMapping("/v1/libraryevent")
    public ResponseEntity<LibraryEvent> postLibraryEvent(@RequestBody LibraryEvent libraryEvent) throws JsonProcessingException {

        log.info("libraryEvent - {}",libraryEvent);
        libraryEventProducer.sendLibraryEvent(libraryEvent);
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }

    @PutMapping("/v1/libraryevent")
    public ResponseEntity<?> updateLibraryEvent(@RequestBody LibraryEvent libraryEvent) throws JsonProcessingException {

        log.info("libraryEvent - {}",libraryEvent);
        if(null == libraryEvent.libraryEventId())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Library event id is mandatory.");
        if(LibraryEventType.UPDATE != libraryEvent.libraryEventType())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Only UPDATE request is allowed.");
        libraryEventProducer.sendLibraryEvent(libraryEvent);
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }
}
