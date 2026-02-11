package org.iwouldargue.external.media;

import jakarta.validation.constraints.NotNull;

public record CreateLkDiscussionRequest(
        @NotNull
        String discussionId,
        @NotNull
        String title,
        @NotNull
        boolean isRecorded
) {
}
