package org.iwouldargue.external.notification;

import jakarta.validation.constraints.NotNull;

public record Event<T>(
        @NotNull
        String discussionId,
        @NotNull
        T data
) {
}
