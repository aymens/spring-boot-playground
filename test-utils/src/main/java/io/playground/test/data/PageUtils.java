package io.playground.test.data;

import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for creating Spring Data {@link Page} instances in test scenarios.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class PageUtils {

    /**
     * Creates a Page from varargs containing all elements.
     */
    @SafeVarargs
    public static <T> Page<T> pageOf(T... elements) {
        return new PageImpl<>(Arrays.asList(elements));
    }

    /**
     * Creates a Page from varargs with custom pagination parameters.
     */
    @SafeVarargs
    public static <T> Page<T> pageOf(Pageable pageable, T... elements) {
        return pageOf(Arrays.asList(elements), pageable);
    }

    /**
     * Creates a Page from varargs with specified page number and size.
     */
    @SafeVarargs
    public static <T> Page<T> pageOf(int pageNumber, int pageSize, T... elements) {
        return pageOf(Arrays.asList(elements), PageRequest.of(pageNumber, pageSize));
    }

    /**
     * Creates a Page from a List with custom pagination parameters.
     */
    public static <T> Page<T> pageOf(List<T> elements, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), elements.size());

        if (start > elements.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, elements.size());
        }

        return new PageImpl<>(
                elements.subList(start, end),
                pageable,
                elements.size()
        );
    }

    /**
     * Creates an empty Page with given page parameters.
     */
    public static <T> Page<T> emptyPage(Pageable pageable) {
        return new PageImpl<>(Collections.emptyList(), pageable, 0);
    }
}