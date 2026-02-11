package org.iwouldargue.discussion.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateDiscussionResponse(
        @NotNull
        String sfuUrl,
        @NotNull
        String sfuToken,
        @NotNull
        String userToken,
        @NotNull
        UUID discussionId
) {
}
