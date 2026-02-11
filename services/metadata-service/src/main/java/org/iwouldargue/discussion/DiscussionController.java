package org.iwouldargue.discussion;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iwouldargue.attendee.AttendeeService;
import org.iwouldargue.auth.Role;
import org.iwouldargue.auth.Session;
import org.iwouldargue.auth.TokenIssuer;
import org.iwouldargue.config.ExternalConfig;
import org.iwouldargue.discussion.dto.CreateDiscussionRequest;
import org.iwouldargue.discussion.dto.CreateDiscussionResponse;
import org.iwouldargue.discussion.dto.DiscussionJoin;
import org.iwouldargue.discussion.finish.DiscussionFinish;
import org.iwouldargue.discussion.finish.DiscussionFinishRepository;
import org.iwouldargue.external.media.CreateLkDiscussionRequest;
import org.iwouldargue.external.media.CreateLkTokenRequest;
import org.iwouldargue.external.media.MediaService;
import org.iwouldargue.external.media.SetLkParticipantRequest;
import org.iwouldargue.external.notification.NotificationService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/discussions")
@RequiredArgsConstructor
@Slf4j
public class DiscussionController {
    private final DiscussionRepository discussionRepo;
    private final DiscussionFinishRepository discussionFinishRepo;
    private final TokenIssuer tokenIssuer;
    private final MediaService mediaService;
    private final NotificationService notificationService;
    private final AttendeeService attendeeService;
    private final StringRedisTemplate redisTemplate;

    private final ExternalConfig externalConfig;

    @PostMapping
    public CreateDiscussionResponse createDiscussion(@Valid @RequestBody CreateDiscussionRequest payload) {
        var discussion = Discussion.builder()
                .id(UUID.randomUUID())
                .title(payload.title())
                .isRecorded(payload.isRecorded())
                .build();
        var userId = attendeeService.recordAttendee(payload.displayName(), discussion.getId());
        var session = new Session(payload.displayName(), discussion.getId(), Role.HOST, userId);
        var userToken = tokenIssuer.issue(session);

        var createLkDiscussionStatus = mediaService.createDiscussion(new CreateLkDiscussionRequest(
                discussion.getId().toString(),
                discussion.getTitle(),
                discussion.isRecorded()
        ));
        if (createLkDiscussionStatus != HttpStatus.OK) {
            throw new ResponseStatusException(createLkDiscussionStatus);
        }
        var sfuToken = mediaService.createHostToken(new CreateLkTokenRequest(
                discussion.getId().toString(),
                userId.toString(),
                payload.displayName()
        ));

        if (sfuToken.getStatusCode() != HttpStatus.OK) {
            throw new ResponseStatusException(sfuToken.getStatusCode());
        }

        var manageResponse = notificationService.manageDiscussion(discussion.getId());
        if (manageResponse != HttpStatus.OK) {
            throw new ResponseStatusException(sfuToken.getStatusCode());
        }

        discussionRepo.save(discussion);

        return new CreateDiscussionResponse(
                externalConfig.getLivekitUrl(),
                sfuToken.getBody(),
                userToken,
                discussion.getId()
        );
    }

    @GetMapping(path = "/{discussionId}/{displayName}")
    public DiscussionJoin joinDiscussion(@PathVariable UUID discussionId, @PathVariable String displayName) {

        var finish = discussionFinishRepo.existsById(discussionId);
        if (finish) {
            throw new ResponseStatusException(HttpStatus.GONE);
        }

        var discussion = discussionRepo.findById(discussionId);
        if (discussion.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        var userId = attendeeService.recordAttendee(displayName, discussionId);

        var sfuToken = mediaService.createAttendeeToken(new CreateLkTokenRequest(
                discussionId.toString(),
                userId.toString(),
                displayName
        ));
        if (sfuToken.getStatusCode() != HttpStatus.OK) {
            throw new ResponseStatusException(sfuToken.getStatusCode());
        }

        var userToken = tokenIssuer.issue(new Session(displayName, discussionId, Role.ATTENDEE, userId));
        return new DiscussionJoin(externalConfig.getLivekitUrl(), sfuToken.getBody(), userToken);
    }

    @PutMapping(path = "/{discussionId}/raise-hand")
    public void raiseHand(
            @PathVariable UUID discussionId,
            @AuthenticationPrincipal Session session
    ) {
        if (session.discussionId() != discussionId || session.role() != Role.ATTENDEE) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        redisTemplate.opsForValue().set("attendee:" + session.userId(), discussionId.toString());
    }

    @PutMapping(path = "/{discussionId}/participant/{attendeeId}")
    public void setParticipant(
            @PathVariable UUID discussionId,
            @PathVariable UUID attendeeId,
            @AuthenticationPrincipal Session session
    ) {
        checkHostAuthority(discussionId, session);
        var attendeeHand = redisTemplate.opsForValue().get("attendee:" + attendeeId);
        if (attendeeHand == null || !attendeeHand.equals(discussionId.toString())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        var status = mediaService.setParticipant(new SetLkParticipantRequest(discussionId.toString(), attendeeId.toString()));
        if (status != HttpStatus.OK) {
            throw new ResponseStatusException(status);
        }
    }

    @DeleteMapping(path = "/{discussionId}/participant/{attendeeId}")
    public void unsetParticipant(
            @PathVariable UUID discussionId,
            @PathVariable UUID attendeeId,
            @AuthenticationPrincipal Session session
    ) {
        checkHostAuthority(discussionId, session);
        var status = mediaService.unsetParticipant(new SetLkParticipantRequest(discussionId.toString(), attendeeId.toString()));
        if (status != HttpStatus.OK) {
            throw new ResponseStatusException(status);
        }
    }

    @PutMapping(path = "/{discussionId}/finish")
    public void finishDiscussion(
            @PathVariable UUID discussionId,
            @AuthenticationPrincipal Session session
    ) {
        checkHostAuthority(discussionId, session);
        discussionFinishRepo.save(DiscussionFinish.builder().discussionId(discussionId).build());
    }

    private void checkHostAuthority(UUID discussionId, Session session) {
        if (session.discussionId() != discussionId) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

}
