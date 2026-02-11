package org.iwouldargue.discussion;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DiscussionRepository extends CassandraRepository<Discussion, UUID> {
}
