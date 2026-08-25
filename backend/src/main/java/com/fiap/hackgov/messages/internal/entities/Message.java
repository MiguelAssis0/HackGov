package com.fiap.hackgov.messages.internal.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fiap.hackgov.cityhall_management.internal.entities.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "messages")
@Filter(name = "cityHallFilter", condition = "chat_id in (select c.id from chats c where c.city_hall_id = :cityHallId)")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private Employee sender;

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime sentAt;

    @OneToOne(mappedBy="message",cascade=CascadeType.ALL,orphanRemoval=true)
    private MessageAttachment attachment;
}
