package org.iwouldargue.discussion.dto;

import jakarta.validation.constraints.NotNull;

public record DiscussionJoin(
        @NotNull
        String sfuUrl,
        @NotNull
        String sfuToken,
        @NotNull
        String userToken
) {
}
