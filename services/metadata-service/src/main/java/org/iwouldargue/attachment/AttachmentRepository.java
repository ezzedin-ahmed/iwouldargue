package org.iwouldargue.attachment;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AttachmentRepository extends CassandraRepository<Attachment, UUID> {
    List<Attachment> findByDiscussionId(UUID discussionId);
}
