package org.iwouldargue.discussion.dto;

import jakarta.validation.constraints.NotNull;

public record CreateDiscussionRequest(
        @NotNull
        String displayName,
        @NotNull
        String title,
        @NotNull
        boolean isRecorded
) {
}
