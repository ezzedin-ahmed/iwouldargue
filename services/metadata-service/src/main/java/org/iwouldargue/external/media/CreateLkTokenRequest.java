package org.iwouldargue.external.media;

import jakarta.validation.constraints.NotNull;

public record CreateLkTokenRequest(
        @NotNull
        String discussionId,
        @NotNull
        String userId,
        @NotNull
        String userName
) {
}
