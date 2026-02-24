package com.skillmatch.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
    
    @Column(name = "read_at", columnDefinition = "DATETIME")
    private LocalDateTime readAt;
    
    @Column(name = "sent_at", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime sentAt;
    
    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl; // URL de archivo adjunto (opcional)
    
    @Column(name = "deleted_by_sender", nullable = false)
    private Boolean deletedBySender = false;
    
    @Column(name = "deleted_by_receiver", nullable = false)
    private Boolean deletedByReceiver = false;
    
    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
    }
}
