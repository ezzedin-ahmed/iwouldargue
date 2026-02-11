package org.iwouldargue.external.notification;

import jakarta.validation.constraints.NotNull;

public record AttachmentData(
        @NotNull
        String id,
        @NotNull
        String sender
) {
}
