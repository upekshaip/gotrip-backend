package com.gotrip.backend;

import com.gotrip.backend.config.AppConfig;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;


@RestController
public class HomeController {

    private final AppConfig appConfig;

    private HomeController(AppConfig appConfig) {
        this.appConfig = appConfig;
    };


    @RequestMapping("/hello")
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


    @RequestMapping("/env")
    public Map<String, String> getEnv() {
        return Map.of(
        "db", appConfig.dbUrl()
        );
    }


}
