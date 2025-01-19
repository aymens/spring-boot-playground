/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.playground._sandbox;

import lombok.val;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.jayway.jsonpath.JsonPath.read;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GreetingControllerITests {

    @Autowired
    private TestRestTemplate template;

    @MockBean
    SomeService someService;

    @Test
    public void getGreeting_WithoutParam_ReturnsDefaultMessage() {
        val response = template.getForEntity("/api/v1/greeting", String.class);

        // Print the response body
        System.out.println(response.getBody());

        // Validate the response status and body
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat((((String) read(response.getBody(), "$.content"))))
                .isEqualTo("Hello, World!");
    }

    @Test
    public void getGreeting_WithNameParam_ReturnsTailoredMessage() {
        val response = template.getForEntity(
                UriComponentsBuilder.fromPath("/api/v1/greeting")
                        .queryParam("name", "{name}")
                        .buildAndExpand("Spring Community")
                        .toUriString(),
                String.class);

        // Print the response body
        System.out.println(response.getBody());

        // Validate the response status and body
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat((((String) read(response.getBody(), "$.content"))))
                .isEqualTo("Hello, Spring Community!");
    }

    @Test @Disabled
    public void getDebug_WithEncodedUri_ValidatesEncoding() {
        // Direct approach
        String directUri = UriComponentsBuilder.fromPath("/api/v1/greeting")
                .queryParam("name", "Spring Community")
                .toUriString();
        System.out.println("Direct URI: " + directUri);

        // Template approach
        String templateUri = UriComponentsBuilder.fromPath("/api/v1/greeting")
                .queryParam("name", "{name}")
                .buildAndExpand("Spring Community")
                .toUriString();
        System.out.println("Template URI: " + templateUri);

        // Let's also print what the endpoint receives
        ResponseEntity<String> response1 = template.getForEntity(directUri, String.class);
        ResponseEntity<String> response2 = template.getForEntity(templateUri, String.class);

        System.out.println("Direct approach response: " + response1.getBody());
        System.out.println("Template approach response: " + response2.getBody());
    }

    @Test @Disabled
    public void getDebug_WithDifferentEncodings_ComparesResults() {
        // Original string
        String value = "Spring Community";

        // UriComponentsBuilder encoding
        String uriBuilderEncoded = UriComponentsBuilder.fromPath("/api/v1/greeting")
                .queryParam("name", value)
                .toUriString();
        System.out.println("UriBuilder encoded: " + uriBuilderEncoded);

        // TestRestTemplate will use RestTemplate's UriTemplateHandler
        // We can get it directly:
        DefaultUriBuilderFactory uriFactory = new DefaultUriBuilderFactory();
        String templateEncoded = uriFactory.expand("/api/v1/greeting?name={name}", value)
                .toString();
        System.out.println("TestRestTemplate would encode to: " + templateEncoded);
    }

    @Test @Disabled
    public void getDebug_WithQueryParams_ValidatesResolution() {
        // Direct approach
        String directUri = UriComponentsBuilder.fromPath("/api/v1/debug")
                .queryParam("name", "Spring Community")
                .toUriString();
        System.out.println("directUri: "+directUri);

        // Template approach
        String templateUri = UriComponentsBuilder.fromPath("/api/v1/debug")
                .queryParam("name", "{name}")
                .buildAndExpand("Spring Community")
                .toUriString();
        System.out.println("templateUri: "+templateUri);

        // Call debug endpoint with both approaches
        var directResponse = template.getForEntity(directUri, Map.class);
        System.out.println("Direct approach debug info:");
        System.out.println(directResponse.getBody());

        var templateResponse = template.getForEntity(templateUri, Map.class);
        System.out.println("\nTemplate approach debug info:");
        System.out.println(templateResponse.getBody());
    }

    @Test @Disabled
    public void investigateDoubleEncoding() {
        // Direct approach
        String directUri = UriComponentsBuilder.fromPath("/api/v1/debug")
                .queryParam("name", "Spring Community")
                .toUriString();
        System.out.println("Direct URI initial: " + directUri);

        // Let's see what happens when TestRestTemplate processes it
        URI processedUri = new DefaultUriBuilderFactory()
                .expand(directUri);
        System.out.println("After TestRestTemplate processing: " + processedUri);

        // Decode both ways to see the difference
        System.out.println("Single decoded: " +
                URLDecoder.decode("Spring%20Community", StandardCharsets.UTF_8));
        System.out.println("Double decoded: " +
                URLDecoder.decode("Spring%2520Community", StandardCharsets.UTF_8));
    }

}
