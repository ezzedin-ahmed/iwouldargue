package org.iwouldargue.external.media;

import jakarta.validation.constraints.NotNull;

public record SetLkParticipantRequest(
        @NotNull
        String discussionId,
        @NotNull
        String attendeeId
) {
}
