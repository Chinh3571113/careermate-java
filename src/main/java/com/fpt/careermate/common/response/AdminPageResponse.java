package com.fpt.careermate.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

/**
 * Custom Page Response for Admin API caching
 * This class is serializable and deserializable by Jackson (unlike PageImpl)
 */
@Data
@NoArgsConstructor  // Required for Jackson deserialization
@AllArgsConstructor
public class AdminPageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private int numberOfElements;

    /**
     * Create AdminPageResponse from Spring Data Page
     */
    public static <T> AdminPageResponse<T> from(Page<T> page) {
        return new AdminPageResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast(),
            page.getNumberOfElements()
        );
    }

    /**
     * Convert AdminPageResponse back to Spring Data Page
     */
    public Page<T> toPage() {
        return new PageImpl<>(
            content,
            PageRequest.of(page, size),
            totalElements
        );
    }
}

