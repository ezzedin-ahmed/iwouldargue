package org.iwouldargue.auth;

import java.util.UUID;

public record Session(
        String displayName,
        UUID discussionId,
        Role role,
        UUID userId
) {
}
