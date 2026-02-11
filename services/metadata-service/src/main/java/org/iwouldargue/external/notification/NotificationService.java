package org.iwouldargue.external.notification;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationService {

    private final RestTemplate restTemplate;
    private final StringRedisTemplate redisTemplate;
    private final ConcurrentHashMap<String, String> discussionNotifiers = new ConcurrentHashMap<>();

    public NotificationService(
            @Qualifier("notificationRestTemplate") RestTemplate restTemplate,
            StringRedisTemplate redisTemplate
    ) {
        this.restTemplate = restTemplate;
        this.redisTemplate = redisTemplate;
    }

    public HttpStatusCode manageDiscussion(UUID discussionId) {
        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/v1/manage/" + discussionId.toString(),
                null,
                Void.class

        );
        var notifierId = response.getHeaders().getFirst("X-Worker-Id");
        if (notifierId == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        redisTemplate.opsForValue().set(discussionId.toString(), notifierId);
        return response.getStatusCode();
    }

    public <T> void notifyEvent(@Valid Event<@Valid T> event) {
        var notifierId = discussionNotifiers.get(event.discussionId());
        if (notifierId == null) {
            notifierId = redisTemplate.opsForValue().get(event.discussionId());
            if (notifierId == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            discussionNotifiers.put(event.discussionId(), notifierId);
        }
        redisTemplate.convertAndSend(notifierId, event);
    }

}
