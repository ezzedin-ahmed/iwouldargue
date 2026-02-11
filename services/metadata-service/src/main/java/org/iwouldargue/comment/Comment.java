package org.iwouldargue.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table
public class Comment {
    @PrimaryKey
    private UUID id;
    private String authorName;
    private UUID discussionId;
    @CreatedDate
    private LocalDateTime postedAt;
    private String content;
}
