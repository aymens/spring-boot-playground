package io.playground.web;

import io.playground.service.CompanyService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/data-gen")
@ConditionalOnProperty(
        prefix = "playground.api.rest",
        name = "data-generator.enabled",
        havingValue = "true",
        matchIfMissing = false)
//TODO mv to another api module, dep on app-core, app disabled by props, later
public class DataGeneratorController {

    private final CompanyService companyService;

    @Value("${playground.api.rest.data-generator.enabled}")
//    @Value("${playground.api.rest.data-generator.enabled:false}")
    private boolean dataGenEnabled;

    @PostConstruct
    public void init() {//TODO DEL
        log.info("DataGeneratorController initialized. playground.api.rest.data-generator.enabled: {}", dataGenEnabled);
    }

    public DataGeneratorController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping("v1/debug")
    public Map<String, Object> debug(HttpServletRequest request) {
        Map<String, Object> debug = new HashMap<>();
        debug.put("queryString", request.getQueryString());
        debug.put("parameter", request.getParameter("name"));
        debug.put("requestUri", request.getRequestURI());
        debug.put("characterEncoding", request.getCharacterEncoding());
        return debug;
    }

}