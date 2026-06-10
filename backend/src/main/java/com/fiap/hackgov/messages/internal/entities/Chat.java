package com.fiap.hackgov.messages.internal.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fiap.hackgov.cityhall_management.internal.entities.CityHall;
import com.fiap.hackgov.messages.internal.entities.enums.ChatType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "chats")
@Getter
@Setter
@Filter(name = "cityHallFilter", condition = "city_hall_id = :cityHallId")
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;

    @Enumerated(EnumType.STRING)
    private ChatType type;

    @ManyToOne
    @JoinColumn(name = "city_hall_id")
    private CityHall cityHall;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL)
    private final List<ChatParticipant> participants = new ArrayList<>();
}
