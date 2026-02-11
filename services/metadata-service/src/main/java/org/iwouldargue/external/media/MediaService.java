package org.iwouldargue.external.media;

import jakarta.validation.Valid;
import org.iwouldargue.config.ExternalConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MediaService {

    private final RestTemplate restTemplate;
    private final ExternalConfig config;

    public MediaService(@Qualifier("mediaRestTemplate") RestTemplate restTemplate, ExternalConfig externalConfig) {
        this.restTemplate = restTemplate;
        this.config = externalConfig;
    }

    public HttpStatusCode createDiscussion(@Valid CreateLkDiscussionRequest request) {
        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/v1/discussions",
                request,
                Void.class
        );
        return response.getStatusCode();
    }

    public ResponseEntity<String> createHostToken(@Valid CreateLkTokenRequest request) {
        return restTemplate.postForEntity(
                "/api/v1/tokens/host",
                request,
                String.class
        );
    }

    public ResponseEntity<String> createAttendeeToken(@Valid CreateLkTokenRequest request) {
        return restTemplate.postForEntity(
                "/api/v1/tokens/attendee",
                request,
                String.class
        );
    }

    public HttpStatusCode setParticipant(@Valid SetLkParticipantRequest request) {
        HttpEntity<SetLkParticipantRequest> entity = new HttpEntity<>(request);
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/participants/set",
                HttpMethod.PUT,
                entity,
                Void.class
        );
        return response.getStatusCode();
    }

    public HttpStatusCode unsetParticipant(@Valid SetLkParticipantRequest request) {
        HttpEntity<SetLkParticipantRequest> entity = new HttpEntity<>(request);
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/participants/unset",
                HttpMethod.PUT,
                entity,
                Void.class
        );
        return response.getStatusCode();
    }
}