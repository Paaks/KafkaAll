package com.prads.kafka.learn.domain;

public record Book(
        Integer bookId,
        String bookName,
        String bookAuthor
) {
}
