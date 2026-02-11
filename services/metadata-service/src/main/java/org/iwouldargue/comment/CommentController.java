package org.iwouldargue.comment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.iwouldargue.auth.Session;
import org.iwouldargue.external.notification.CommentData;
import org.iwouldargue.external.notification.Event;
import org.iwouldargue.external.notification.NotificationService;
import org.springframework.data.domain.Limit;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentRepository repo;
    private final NotificationService notificationService;

    @PostMapping
    public void addComment(
            @Valid @RequestBody AddCommentRequest payload,
            @AuthenticationPrincipal Session session
    ) {
        var comment = Comment.builder()
                .id(UUID.randomUUID())
                .authorName(session.displayName())
                .discussionId(session.discussionId())
                .content(payload.content())
                .build();
        repo.save(comment);
        notificationService.notifyEvent(new Event<>(session.discussionId().toString(), new CommentData(payload.content(), session.displayName())));
    }

    @GetMapping
    public List<CommentDto> getCommentsFrame(
            @RequestParam(required = false) LocalDateTime after,
            @RequestParam(required = false) LocalDateTime before,
            @RequestParam(required = false) Integer frameSize,
            @AuthenticationPrincipal Session session
    ) {
        if (after != null && before != null) {
            throw new ErrorResponseException(HttpStatus.CONFLICT);
        }

        Limit limit;
        if (frameSize == null) {
            limit = Limit.unlimited();
        } else {
            limit = Limit.of(frameSize);
        }

        List<CommentDto> comments;
        if (after != null) {
            comments = repo.findByDiscussionIdAndPostedAtAfterOrderByPostedAtAsc(limit, session.discussionId(), after);
        } else if (before != null) {
            comments = repo.findByDiscussionIdAndPostedAtBeforeOrderByPostedAtDesc(limit, session.discussionId(), before);
        } else {
            comments = repo.findByDiscussionIdOrderByPostedAtAsc(limit, session.discussionId());
        }

        return comments;
    }

    @DeleteMapping(path = "/{commentId}")
    public void removeComment(@PathVariable UUID commentId) {
        repo.deleteById(commentId);
    }
}
