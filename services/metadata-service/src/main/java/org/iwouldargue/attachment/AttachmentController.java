package org.iwouldargue.attachment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iwouldargue.FileService;
import org.iwouldargue.auth.Role;
import org.iwouldargue.auth.Session;
import org.iwouldargue.external.notification.AttachmentData;
import org.iwouldargue.external.notification.Event;
import org.iwouldargue.external.notification.NotificationService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/attachments")
@RequiredArgsConstructor
@Slf4j
public class AttachmentController {
    private final AttachmentRepository repo;
    private final NotificationService notificationService;
    private final FileService fileService;

    @PostMapping
    public String addAttachment(
            @RequestParam MultipartFile file,
            @AuthenticationPrincipal Session session
    ) {
        if (session.role() == Role.ATTENDEE) {
            throw new ErrorResponseException(HttpStatus.UNAUTHORIZED);
        }
        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(Objects.requireNonNull(file.getContentType()));
        } catch (NullPointerException | InvalidMediaTypeException e) {
            log.error(e.toString());
            throw new ErrorResponseException(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        UUID fileId;
        try {
            fileId = fileService.write(file);
        } catch (IOException e) {
            log.error(e.toString());
            throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        var attachment = new Attachment(
                fileId,
                session.discussionId(),
                session.displayName(),
                session.role(),
                file.getOriginalFilename(),
                mediaType,
                file.getSize()
        );
        repo.save(attachment);
        notificationService.notifyEvent(new Event<>(session.discussionId().toString(), new AttachmentData(fileId.toString(), session.displayName())));
        return attachment.getId().toString();
    }

    @GetMapping(path = "/{attachmentId}")
    public ResponseEntity<Resource> getAttachment(
            @PathVariable UUID attachmentId,
            @AuthenticationPrincipal Session session
    ) {
        var attachment = repo.findById(attachmentId).orElseThrow(() -> new ErrorResponseException(HttpStatus.NOT_FOUND));

        if (attachment.getDiscussionId() != session.discussionId()) {
            throw new ErrorResponseException(HttpStatus.UNAUTHORIZED);
        }

        var filepath = fileService.getFilePath(attachmentId);
        if (filepath.isEmpty()) {
            throw new ErrorResponseException(HttpStatus.NOT_FOUND);
        }
        Resource resource;
        try {
            resource = new UrlResource(filepath.get().toUri());
        } catch (IOException e) {
            log.error(e.toString());
            throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFilename() + "\"")
                .contentType(attachment.getMediaType())
                .contentLength(attachment.getSize())
                .body(resource);
    }

    @GetMapping
    public List<Attachment> getDiscussionAttachments(@RequestParam UUID discussionId) {
        var attachments = repo.findByDiscussionId(discussionId);
        if (attachments.isEmpty()) {
            throw new ErrorResponseException(HttpStatus.NOT_FOUND);
        }
        return attachments;
    }
}
