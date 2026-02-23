package com.gotrip.backend;

import com.gotrip.backend.config.AppConfig;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;


@RestController
public class ExampleController {

    private ExampleController(AppConfig appConfig) {
    };

    @RequestMapping("/")
    public Map<String, Object> hello() {
        System.out.println("Hello World");
        return Map.of(
                "greet", "Hello World",
                "things", Map.of(
                        "one", 1,
                        "two", 2
                )
        );
    }
}
