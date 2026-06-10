package com.fiap.hackgov.messages.internal.repositories;

import com.fiap.hackgov.messages.internal.entities.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<Chat, UUID> {

    @Query("""
                SELECT c
                FROM Chat c
                JOIN ChatParticipant cp1 ON cp1.chat = c
                JOIN ChatParticipant cp2 ON cp2.chat = c
                WHERE c.type = 'PRIVATE'
                AND cp1.employee.id = :employee1
                AND cp2.employee.id = :employee2
            """)
    Optional<Chat> findPrivateChatBetweenEmployees(UUID employee1, UUID employee2);

    @Query("""
                SELECT DISTINCT c
                FROM Chat c
                JOIN FETCH c.participants p
                JOIN FETCH p.employee
                WHERE EXISTS (
                    SELECT 1
                    FROM ChatParticipant cp
                    WHERE cp.chat.id = c.id
                    AND cp.employee.id = :employeeId
                )
                ORDER BY c.createdAt DESC
            """)
    List<Chat> findAllByParticipant(UUID employeeId);
}