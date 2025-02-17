package io.playground.test.data;

import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedModel;

import java.util.List;

/**
 * A mirror of {@link PagedModel} to be used in tests as type for deserialization of responses of type {@link Page}.
 * <p>
 * Example:
 * <pre><code class='java'>
 * TestPageModel<DepartmentOut> departments = objectMapper.readValue(
 *      listResult.getResponse().getContentAsString(),
 *      objectMapper.getTypeFactory().constructParametricType(TestPageModel.class, DepartmentOut.class)
 * );</code></pre>
 * 
 * @param <T>
 */
@Data
public class TestPageModel<T> {
    private List<T> content;
    private PagedModel.PageMetadata page;
}
