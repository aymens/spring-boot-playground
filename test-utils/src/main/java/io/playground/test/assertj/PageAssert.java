package io.playground.test.assertj;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractListAssert;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.api.iterable.ThrowingExtractor;
import org.assertj.core.groups.Tuple;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

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

    public PageAssert<ELEMENT> hasNoContent() {
        assertThat(actual.hasContent())
                .as("page content")
                .isFalse();
        return this;
    }

    public PageAssert<ELEMENT> hasContent() {
        assertThat(actual.hasContent())
                .as("page content")
                .isTrue();
        return this;
    }

    /**
     * Delegates to {@link org.assertj.core.api.AbstractIterableAssert#extracting(org.assertj.core.api.iterable.ThrowingExtractor)}
     * to extract values from the elements of the page using the provided extractor function.
     *
     * @param <V>         the type of the value to extract
     * @param <EXCEPTION> the type of exception that may be thrown by the extractor
     * @param extractor   a function to extract a value from each element in the page
     * @return an assertion object for chaining further assertions on the extracted values
     */
    public <V, EXCEPTION extends Exception> AbstractListAssert<?, List<? extends V>, V, ObjectAssert<V>> extracting(
            ThrowingExtractor<? super ELEMENT, V, EXCEPTION> extractor) {
        return assertThat(actual).extracting(extractor);
    }

    /**
     * Extracts values from the elements of the page using the provided extractor functions.
     *
     * @param extractors one or more functions to extract values from elements in the page
     * @return an assertion object for chaining further assertions on the extracted values
     */
    @SafeVarargs
    public final AbstractListAssert<?, List<? extends Tuple>, Tuple, ObjectAssert<Tuple>> extracting(
            Function<? super ELEMENT, ?>... extractors) {
        return assertThat(actual).extracting(extractors);
    }
}