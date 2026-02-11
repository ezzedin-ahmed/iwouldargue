package org.iwouldargue.attendee;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttendeeService {
    private final AttendeeRepository repo;

    public UUID recordAttendee(String displayName, UUID discussionId) {
        var attendee = Attendee.builder().id(UUID.randomUUID()).discussionId(discussionId).displayName(displayName).build();
        repo.save(attendee);
        return attendee.getId();
    }
}
