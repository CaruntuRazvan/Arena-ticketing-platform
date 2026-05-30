package com.arena.catalog.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
// Numele clasei este SerializablePage, <T> este parametrul generic
public class SerializablePage<T> implements Serializable {

    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;

    public SerializablePage(Page<T> page) {
        this.content = page.getContent();
        this.pageNumber = page.getNumber();
        this.pageSize = page.getSize();
        this.totalElements = page.getTotalElements();
    }

    public Page<T> toPage() {
        return new PageImpl<>(content, PageRequest.of(pageNumber, pageSize), totalElements);
    }
}
