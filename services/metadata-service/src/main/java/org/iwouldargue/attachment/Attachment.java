package org.iwouldargue.attachment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.iwouldargue.auth.Role;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.http.MediaType;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table
public class Attachment {
    @PrimaryKey
    private UUID id;
    private UUID discussionId;

    private String senderName;
    @CassandraType(type = CassandraType.Name.INT)
    private Role senderRole;

    private String filename;
    @CassandraType(type = CassandraType.Name.INT)
    private MediaType mediaType;
    private Long size;

}
