package com.skillmatch.backend.repository;

import com.skillmatch.backend.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    // Conversación entre dos usuarios (ordenada por fecha)
    @Query("SELECT m FROM Message m WHERE " +
           "((m.sender.id = ?1 AND m.receiver.id = ?2 AND m.deletedBySender = false) OR " +
           "(m.sender.id = ?2 AND m.receiver.id = ?1 AND m.deletedByReceiver = false)) " +
           "ORDER BY m.sentAt DESC")
    List<Message> findConversationBetweenUsers(Long userId1, Long userId2);
    
    // Últimos mensajes de cada conversación
    @Query("SELECT m FROM Message m WHERE m.id IN (" +
           "SELECT MAX(m2.id) FROM Message m2 WHERE " +
           "(m2.sender.id = ?1 OR m2.receiver.id = ?1) " +
           "GROUP BY CASE WHEN m2.sender.id = ?1 THEN m2.receiver.id ELSE m2.sender.id END) " +
           "ORDER BY m.sentAt DESC")
    List<Message> findLastMessagesByUserId(Long userId);
    
    // Mensajes no leídos
    @Query("SELECT m FROM Message m WHERE m.receiver.id = ?1 AND m.isRead = false AND m.deletedByReceiver = false")
    List<Message> findUnreadMessagesByUserId(Long userId);
    
    // Contar mensajes no leídos
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver.id = ?1 AND m.isRead = false AND m.deletedByReceiver = false")
    long countUnreadMessagesByUserId(Long userId);
    
    // Contar mensajes no leídos de un usuario específico
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver.id = ?1 AND m.sender.id = ?2 AND m.isRead = false AND m.deletedByReceiver = false")
    long countUnreadMessagesFromUser(Long receiverId, Long senderId);
}
