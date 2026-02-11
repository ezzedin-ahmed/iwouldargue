package org.iwouldargue.attendee;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AttendeeRepository extends CassandraRepository<Attendee, UUID> {
}
