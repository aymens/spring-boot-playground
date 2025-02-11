package io.playground.test.data;

import lombok.Data;
import org.springframework.data.web.PagedModel;

import java.util.List;

/**
 * A copy in structure of {@link PagedModel} to be used in tests as type for deserialization to pass.
 * @param <T>
 */
@Data
public class TestPageModel<T> {
    private List<T> content;
    private PagedModel.PageMetadata page;
}
