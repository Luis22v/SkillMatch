package com.skillmatch.backend.service;

import com.skillmatch.backend.dto.ChatMessageResponse;
import com.skillmatch.backend.dto.MessageRequest;
import com.skillmatch.backend.model.Message;
import com.skillmatch.backend.model.User;
import com.skillmatch.backend.repository.MessageRepository;
import com.skillmatch.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {
    
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    
    @Transactional
    public ChatMessageResponse sendMessage(Long senderId, MessageRequest request) {
        if (senderId.equals(request.getReceiverId())) {
            throw new RuntimeException("No puedes enviarte un mensaje a ti mismo");
        }
        
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Usuario remitente no encontrado"));
        
        Long receiverId = request.getReceiverId();
        if (receiverId == null) {
            throw new RuntimeException("El ID del destinatario no puede ser nulo");
        }
        
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Usuario destinatario no encontrado"));
        
        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(request.getContent());
        message.setAttachmentUrl(request.getAttachmentUrl());
        message.setIsRead(false);
        message.setDeletedBySender(false);
        message.setDeletedByReceiver(false);
        
        message = messageRepository.save(message);
        
        // Crear notificación para el receptor
        notificationService.createMessageNotification(receiver.getId(), sender.getId(), message.getId());
        
        return mapToResponse(message);
    }
    
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getConversation(Long userId, Long otherUserId) {
        List<Message> messages = messageRepository.findConversationBetweenUsers(userId, otherUserId);
        return messages.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public ChatMessageResponse markAsRead(Long messageId, Long userId) {
        if (messageId == null) {
            throw new RuntimeException("El ID del mensaje no puede ser nulo");
        }
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Mensaje no encontrado"));
        
        // Verificar que el usuario es el receptor
        if (!message.getReceiver().getId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para marcar este mensaje como leído");
        }
        
        if (!message.getIsRead()) {
            message.setIsRead(true);
            message.setReadAt(LocalDateTime.now());
            message = messageRepository.save(message);
        }
        
        return mapToResponse(message);
    }
    
    @Transactional
    public void markConversationAsRead(Long userId, Long otherUserId) {
        List<Message> unreadMessages = messageRepository.findUnreadMessagesByUserId(userId).stream()
                .filter(msg -> msg.getSender().getId().equals(otherUserId))
                .collect(Collectors.toList());
        
        if (!unreadMessages.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            for (Message message : unreadMessages) {
                message.setIsRead(true);
                message.setReadAt(now);
            }
            
            messageRepository.saveAll(unreadMessages);
        }
    }
    
    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        if (messageId == null) {
            throw new RuntimeException("El ID del mensaje no puede ser nulo");
        }
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Mensaje no encontrado"));
        
        // Marcar como eliminado según quién sea el usuario
        if (message.getSender().getId().equals(userId)) {
            message.setDeletedBySender(true);
        } else if (message.getReceiver().getId().equals(userId)) {
            message.setDeletedByReceiver(true);
        } else {
            throw new RuntimeException("No tienes permiso para eliminar este mensaje");
        }
        
        // Si ambos han eliminado el mensaje, eliminarlo físicamente de la BD
        if (message.getDeletedBySender() && message.getDeletedByReceiver()) {
            messageRepository.delete(message);
        } else {
            messageRepository.save(message);
        }
    }
    
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId) {
        return messageRepository.countUnreadMessagesByUserId(userId);
    }
    
    @Transactional(readOnly = true)
    public Long getUnreadCountFromUser(Long userId, Long fromUserId) {
        return messageRepository.countUnreadMessagesFromUser(userId, fromUserId);
    }
    
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getLastMessages(Long userId) {
        List<Message> lastMessages = messageRepository.findLastMessagesByUserId(userId);
        return lastMessages.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Map<Long, Long> getUnreadCountsByConversation(Long userId) {
        List<Message> unreadMessages = messageRepository.findUnreadMessagesByUserId(userId);
        
        return unreadMessages.stream()
                .collect(Collectors.groupingBy(
                        msg -> msg.getSender().getId(),
                        Collectors.counting()
                ));
    }
    
    private ChatMessageResponse mapToResponse(Message message) {
        User sender = message.getSender();
        User receiver = message.getReceiver();
        
        ChatMessageResponse response = new ChatMessageResponse();
        response.setId(message.getId());
        response.setSenderId(sender.getId());
        response.setSenderName(sender.getFirstName() + " " + sender.getLastName());
        response.setSenderProfileImage(sender.getProfileImageUrl());
        response.setReceiverId(receiver.getId());
        response.setReceiverName(receiver.getFirstName() + " " + receiver.getLastName());
        response.setReceiverProfileImage(receiver.getProfileImageUrl());
        response.setContent(message.getContent());
        response.setIsRead(message.getIsRead());
        response.setReadAt(message.getReadAt());
        response.setSentAt(message.getSentAt());
        response.setAttachmentUrl(message.getAttachmentUrl());
        
        return response;
    }
}
