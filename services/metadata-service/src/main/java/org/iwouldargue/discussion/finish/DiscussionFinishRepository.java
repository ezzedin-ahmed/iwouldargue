package org.iwouldargue.discussion.finish;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DiscussionFinishRepository extends CassandraRepository<DiscussionFinish, UUID> {
}
