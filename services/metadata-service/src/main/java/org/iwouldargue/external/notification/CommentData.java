package org.iwouldargue.external.notification;

import jakarta.validation.constraints.NotNull;

public record CommentData(
        @NotNull
        String body,
        @NotNull
        String sender
) {
}
