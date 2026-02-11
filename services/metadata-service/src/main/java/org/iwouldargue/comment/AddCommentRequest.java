package org.iwouldargue.comment;

import jakarta.validation.constraints.NotNull;

public record AddCommentRequest(
        @NotNull
        String content
) {
}
