package com.prads.kafka.learn.jpa;

import com.prads.kafka.learn.entity.LibraryEvent;
import org.springframework.data.repository.CrudRepository;

public interface LibraryEventRepository extends CrudRepository<LibraryEvent, Integer> {
}
