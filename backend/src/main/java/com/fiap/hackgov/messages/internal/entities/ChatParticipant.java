package com.fiap.hackgov.messages.internal.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import com.fiap.hackgov.messages.internal.entities.enums.ChatRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(
        name = "chat_participants",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"chat_id", "employee_id"})
        }
)
@Filter(name = "cityHallFilter", condition = "chat_id in (select c.id from chats c where c.city_hall_id = :cityHallId)")
public class ChatParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "chat_id")
    private Chat chat;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime joinedAt;

    @Enumerated(EnumType.STRING)
    private ChatRole role;
}
