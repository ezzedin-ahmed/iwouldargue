package org.iwouldargue.comment;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CommentDto(
        @NotNull
        String authorName,
        @NotNull
        LocalDateTime postedAt,
        @NotNull
        String content
) {
}
