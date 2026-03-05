package com.gotrip.transport_service.client;

import com.gotrip.common_library.dto.user.TravellerContactInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Slf4j
public class UserServiceClient {

    private final WebClient webClient;

    public UserServiceClient(WebClient.Builder loadBalancedWebClientBuilder) {
        this.webClient = loadBalancedWebClientBuilder
                .baseUrl("http://user-service") // Eureka service name
                .build();
    }

    public TravellerContactInfo getTravellerContact(Long travellerId) {
        try {
            return webClient.get()
                    .uri("/user/internal/traveller/{id}", travellerId)
                    .retrieve()
                    .bodyToMono(TravellerContactInfo.class)
                    .block();
        } catch (Exception e) {
            log.error("CRITICAL: WebClient failed for travellerId {}. Error: {}", travellerId, e.toString());
            return new TravellerContactInfo("Unknown", "User", "N/A", "N/A");
        }
    }

    public TravellerContactInfo getProviderContact(Long providerId) {
        try {
            return webClient.get()
                    .uri("/user/internal/provider/{id}", providerId)
                    .retrieve()
                    .bodyToMono(TravellerContactInfo.class)
                    .block();
        } catch (Exception e) {
            log.error("CRITICAL: WebClient failed for providerId {}. Error: {}", providerId, e.toString());
            return new TravellerContactInfo("Unknown", "Provider", "N/A", "N/A");
        }
    }
}