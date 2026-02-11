package org.iwouldargue.comment;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends CassandraRepository<Comment, UUID> {
    List<CommentDto> findByDiscussionIdAndPostedAtAfterOrderByPostedAtAsc(Limit limit, UUID discussionId, LocalDateTime postedAt);

    List<CommentDto> findByDiscussionIdAndPostedAtBeforeOrderByPostedAtDesc(Limit limit, UUID discussionId, LocalDateTime postedAt);

    List<CommentDto> findByDiscussionIdOrderByPostedAtAsc(Limit limit, UUID discussionId);
}
