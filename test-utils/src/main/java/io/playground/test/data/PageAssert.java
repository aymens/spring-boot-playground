package io.playground.test.data;

import org.assertj.core.api.AbstractAssert;
import org.springframework.data.domain.Page;

import static org.assertj.core.api.Assertions.assertThat;

public class PageAssert<ELEMENT> extends AbstractAssert<PageAssert<ELEMENT>, Page<ELEMENT>> {
    protected PageAssert(Page<ELEMENT> page) {
        super(page, PageAssert.class);
    }

    public static <T> PageAssert<T> assertThatPage(Page<T> actual) {
        return new PageAssert<>(actual);
    }

    public PageAssert<ELEMENT> hasPageSize(int expectedSize) {
        isNotNull();
        assertThat(actual.getSize())
                .as("page size")
                .isEqualTo(expectedSize);
        return this;
    }

    public PageAssert<ELEMENT> hasTotalPages(int expectedTotalPages) {
        isNotNull();
        assertThat(actual.getTotalPages())
                .as("total pages")
                .isEqualTo(expectedTotalPages);
        return this;
    }

    public PageAssert<ELEMENT> hasTotalElements(long expectedTotalElements) {
        isNotNull();
        assertThat(actual.getTotalElements())
                .as("total elements")
                .isEqualTo(expectedTotalElements);
        return this;
    }

    public PageAssert<ELEMENT> hasPageNumber(int expectedPageNumber) {
        isNotNull();
        assertThat(actual.getNumber())
                .as("page number")
                .isEqualTo(expectedPageNumber);
        return this;
    }

    public PageAssert<ELEMENT> hasPageStats(int size, long totalElements, int totalPages, int number) {
        return hasPageSize(size)
                .hasTotalElements(totalElements)
                .hasTotalPages(totalPages)
                .hasPageNumber(number);
    }

    public PageAssert<ELEMENT> hasEmptyContent() {
        assertThat(actual.hasContent()).isFalse();
        return this;
    }
}