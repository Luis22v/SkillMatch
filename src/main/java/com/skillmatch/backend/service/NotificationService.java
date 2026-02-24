package com.skillmatch.backend.service;

import com.skillmatch.backend.dto.NotificationResponse;
import com.skillmatch.backend.model.Notification;
import com.skillmatch.backend.model.User;
import com.skillmatch.backend.repository.NotificationRepository;
import com.skillmatch.backend.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    @Transactional
    public NotificationResponse createNotification(@NonNull Long userId, String type, String content, Long relatedId, String actionUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setActionUrl(actionUrl);
        notification.setIsRead(false);
        
        notification = notificationRepository.save(notification);
        
        return mapToResponse(notification);
    }
    
    @Transactional
    public void createConnectionRequestNotification(Long userId, @NonNull Long requesterId, Long connectionId) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        String content = requester.getFirstName() + " " + requester.getLastName() + " te ha enviado una solicitud de conexión";
        String actionUrl = "/pages/conexiones.html?tab=pending";
        
        createNotification(userId, "connection_request", content, connectionId, actionUrl);
    }
    
    @Transactional
    public void createConnectionAcceptedNotification(Long userId, @NonNull Long accepterId, Long connectionId) {
        User accepter = userRepository.findById(accepterId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        String content = accepter.getFirstName() + " " + accepter.getLastName() + " ha aceptado tu solicitud de conexión";
        String actionUrl = "/pages/conexiones.html";
        
        createNotification(userId, "connection_accepted", content, connectionId, actionUrl);
    }
    
    @Transactional
    public void createMessageNotification(Long userId, @NonNull Long senderId, Long messageId) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        String content = sender.getFirstName() + " " + sender.getLastName() + " te ha enviado un mensaje";
        String actionUrl = "/pages/mensajes.html?userId=" + senderId;
        
        createNotification(userId, "message", content, messageId, actionUrl);
    }
    
    @Transactional
    public void createApplicationUpdateNotification(Long userId, String status, Long applicationId, String jobTitle) {
        String content = "Tu aplicación para " + jobTitle + " ha sido " + 
                (status.equals("accepted") ? "aceptada" : 
                 status.equals("rejected") ? "rechazada" : "actualizada");
        String actionUrl = "/pages/perfil-usuario.html?tab=applications";
        
        createNotification(userId, "application_update", content, applicationId, actionUrl);
    }
    
    @Transactional
    public void createNewJobNotification(Long userId, String companyName, Long jobId) {
        String content = companyName + " ha publicado una nueva oferta que podría interesarte";
        String actionUrl = "/pages/oportunidades.html?jobId=" + jobId;
        
        createNotification(userId, "new_job", content, jobId, actionUrl);
    }
    
    @Transactional
    public void createApplicationReceivedNotification(Long companyUserId, String userName, Long applicationId, String jobTitle) {
        String content = userName + " ha aplicado a tu oferta: " + jobTitle;
        String actionUrl = "/pages/perfil-empresa.html?tab=applicants";
        
        createNotification(companyUserId, "application_received", content, applicationId, actionUrl);
    }
    
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }
    
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByType(Long userId, String type) {
        List<Notification> notifications = notificationRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type);
        return notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public NotificationResponse markAsRead(@NonNull Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));
        
        // Verificar que la notificación pertenece al usuario
        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para marcar esta notificación");
        }
        
        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
        }
        
        return mapToResponse(notification);
    }
    
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        
        if (unreadNotifications != null && !unreadNotifications.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            for (Notification notification : unreadNotifications) {
                notification.setIsRead(true);
                notification.setReadAt(now);
            }
            
            notificationRepository.saveAll(unreadNotifications);
        }
    }
    
    @Transactional
    public void deleteOldNotifications(Long userId, int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        notificationRepository.deleteByUserIdAndCreatedAtBefore(userId, cutoffDate);
    }
    
    @Transactional
    public void deleteNotification(@NonNull Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));
        
        // Verificar que la notificación pertenece al usuario
        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para eliminar esta notificación");
        }
        
        notificationRepository.delete(notification);
    }
    
    private NotificationResponse mapToResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setUserId(notification.getUser().getId());
        response.setType(notification.getType());
        response.setContent(notification.getContent());
        response.setRelatedId(notification.getRelatedId());
        response.setActionUrl(notification.getActionUrl());
        response.setIsRead(notification.getIsRead());
        response.setReadAt(notification.getReadAt());
        response.setCreatedAt(notification.getCreatedAt());
        return response;
    }
}
