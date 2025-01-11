package io.playground._playground;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@RequestMapping("api")
@RestController
public class GreetingController {

    private final SomeService someService;

    public GreetingController(SomeService someService) {
        this.someService = someService;
    }

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @GetMapping("v1/debug")
    public Map<String, Object> debug(HttpServletRequest request) {
        Map<String, Object> debug = new HashMap<>();
        debug.put("queryString", request.getQueryString());
        debug.put("parameter", request.getParameter("name"));
        debug.put("requestUri", request.getRequestURI());
        debug.put("characterEncoding", request.getCharacterEncoding());
        return debug;
    }

    @GetMapping("v1/greeting")
    public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        someService.someMethod();
        return new Greeting(counter.incrementAndGet(), String.format(template, name));
    }

    //TODO PROBLEM

    @GetMapping("v2/greeting")
    public ResponseEntity<Greeting> greetingV2(
            @RequestParam(value = "name", required = false) Optional<String> nameOptional) {
        return nameOptional
                .map(s -> ResponseEntity.ok(
                        new Greeting(counter.incrementAndGet(), String.format(template, s))))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Optional with Mono<ResponseEntity> example
    @GetMapping("v3/greeting")
    public Mono<ResponseEntity<Greeting>> greetingV3(
            @RequestParam(value = "name", defaultValue = "World") String name) {
        return Mono.just(
                ResponseEntity.ok(
                        new Greeting(
                                counter.incrementAndGet(),
                                String.format(template, name)
                        )
                )
        );
    }

    // Flux<ResponseEntity> for a stream of responses
    @GetMapping("v1/greetings")
    public Mono<ResponseEntity<Flux<Greeting>>> greetingsStream(@RequestParam(value = "count", defaultValue = "5") int count) {
        try {
            System.out.println("Flux definition start");
            Flux<Greeting> greetings = Flux.range(1, count)
                    .doOnSubscribe(subscription -> System.out.println(subscription))
                    .map(i -> {
                                System.out.println("Creating greeting " + i);
                                return new Greeting(
                                        counter.incrementAndGet(),
                                        String.format(template, "User" + i));
                            }
                    )
                    .delayElements(Duration.ofMillis(1000))
                    .doOnNext(g -> System.out.println("Emitted greeting " + g.id()));
            ; // Simulating a delay for streaming
            System.out.println("Flux definition done");

            return Mono.just(ResponseEntity.ok(greetings));
        } finally {
            System.out.println("greetingsStream returned.");
        }
    }

    @GetMapping(value = "v2/greetings", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Greeting> greetingsStreamV2(@RequestParam(value = "count", defaultValue = "5") int count) {
        return Flux.range(1, count)
                .map(i -> new Greeting(
                        counter.incrementAndGet(),
                        String.format(template, "User" + i)
                ))
                .delayElements(Duration.ofMillis(1000));
    }

    @GetMapping(value = "v3/greetings", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Greeting> greetingsStreamV3SSE(@RequestParam(value = "count", defaultValue = "5") int count) {
        return Flux.range(1, count)
                .map(i -> new Greeting(
                        counter.incrementAndGet(),
                        String.format(template, "User" + i)
                ))
                .delayElements(Duration.ofMillis(1000));
    }

    @GetMapping(value = "v4/greetings", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<Flux<Greeting>>> greetingsStreamV4SSE(
            @RequestParam(value = "count", defaultValue = "5") int count) {
        Flux<Greeting> greetingFlux = Flux.range(1, count)
                .map(i -> new Greeting(
                        counter.incrementAndGet(),
                        String.format(template, "User" + i)
                ))
                .delayElements(Duration.ofMillis(1000));

        return Mono.just(
                ResponseEntity
                        .ok()
//                        .contentType(MediaType.TEXT_EVENT_STREAM)
                        .body(greetingFlux));
    }
}

