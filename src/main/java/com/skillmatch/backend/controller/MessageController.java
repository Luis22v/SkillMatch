package com.skillmatch.backend.controller;

import com.skillmatch.backend.dto.ChatMessageResponse;
import com.skillmatch.backend.dto.MessageRequest;
import com.skillmatch.backend.dto.MessageResponse;
import com.skillmatch.backend.model.User;
import com.skillmatch.backend.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MessageController {
    
    private final MessageService messageService;
    
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('EMPRESA')")
    public ResponseEntity<?> sendMessage(
            @Valid @RequestBody MessageRequest request,
            @AuthenticationPrincipal User currentUser) {
        try {
            Long userId = requireAuthenticatedUser(currentUser);
            ChatMessageResponse response = messageService.sendMessage(userId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/conversation/{otherUserId}")
    @PreAuthorize("hasRole('USER') or hasRole('EMPRESA')")
    public ResponseEntity<?> getConversation(
            @PathVariable(required = false) String otherUserId,
            @AuthenticationPrincipal User currentUser) {
        try {
            Long userId = requireAuthenticatedUser(currentUser);
            Long targetUserId = (otherUserId != null && !otherUserId.equals("undefined")) ? Long.parseLong(otherUserId) : null;
            if (targetUserId == null) {
                return ResponseEntity.badRequest().body(new MessageResponse("ID de usuario inválido"));
            }
            List<ChatMessageResponse> messages = messageService.getConversation(userId, targetUserId);
            return ResponseEntity.ok(messages);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("ID de usuario debe ser un número válido"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @PatchMapping("/{messageId}/read")
    @PreAuthorize("hasRole('USER') or hasRole('EMPRESA')")
    public ResponseEntity<?> markAsRead(
            @PathVariable(required = false) String messageId,
            @AuthenticationPrincipal User currentUser) {
        try {
            Long userId = requireAuthenticatedUser(currentUser);
            Long msgId = (messageId != null && !messageId.equals("undefined")) ? Long.parseLong(messageId) : null;
            if (msgId == null) {
                return ResponseEntity.badRequest().body(new MessageResponse("ID de mensaje inválido"));
            }
            ChatMessageResponse response = messageService.markAsRead(msgId, userId);
            return ResponseEntity.ok(response);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("ID de mensaje debe ser un número válido"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @PatchMapping("/conversation/{otherUserId}/read-all")
    @PreAuthorize("hasRole('USER') or hasRole('EMPRESA')")
    public ResponseEntity<?> markConversationAsRead(
            @PathVariable(required = false) String otherUserId,
            @AuthenticationPrincipal User currentUser) {
        try {
            Long userId = requireAuthenticatedUser(currentUser);
            Long targetUserId = (otherUserId != null && !otherUserId.equals("undefined")) ? Long.parseLong(otherUserId) : null;
            if (targetUserId == null) {
                return ResponseEntity.badRequest().body(new MessageResponse("ID de usuario inválido"));
            }
            messageService.markConversationAsRead(userId, targetUserId);
            return ResponseEntity.ok(new MessageResponse("Conversación marcada como leída"));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("ID de usuario debe ser un número válido"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{messageId}")
    @PreAuthorize("hasRole('USER') or hasRole('EMPRESA')")
    public ResponseEntity<?> deleteMessage(
            @PathVariable(required = false) String messageId,
            @AuthenticationPrincipal User currentUser) {
        try {
            Long userId = requireAuthenticatedUser(currentUser);
            Long msgId = (messageId != null && !messageId.equals("undefined")) ? Long.parseLong(messageId) : null;
            if (msgId == null) {
                return ResponseEntity.badRequest().body(new MessageResponse("ID de mensaje inválido"));
            }
            messageService.deleteMessage(msgId, userId);
            return ResponseEntity.ok(new MessageResponse("Mensaje eliminado exitosamente"));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("ID de mensaje debe ser un número válido"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('USER') or hasRole('EMPRESA')")
    public ResponseEntity<?> getUnreadCount(@AuthenticationPrincipal User currentUser) {
        try {
            Long userId = requireAuthenticatedUser(currentUser);
            Long count = messageService.getUnreadCount(userId);
            return ResponseEntity.ok(new CountResponse(count));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/unread-count/{fromUserId}")
    @PreAuthorize("hasRole('USER') or hasRole('EMPRESA')")
    public ResponseEntity<?> getUnreadCountFromUser(
            @PathVariable(required = false) String fromUserId,
            @AuthenticationPrincipal User currentUser) {
        try {
            Long userId = requireAuthenticatedUser(currentUser);
            Long targetUserId = (fromUserId != null && !fromUserId.equals("undefined")) ? Long.parseLong(fromUserId) : null;
            if (targetUserId == null) {
                return ResponseEntity.badRequest().body(new MessageResponse("ID de usuario inválido"));
            }
            Long count = messageService.getUnreadCountFromUser(userId, targetUserId);
            return ResponseEntity.ok(new CountResponse(count));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("ID de usuario debe ser un número válido"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/conversations")
    @PreAuthorize("hasRole('USER') or hasRole('EMPRESA')")
    public ResponseEntity<?> getLastMessages(@AuthenticationPrincipal User currentUser) {
        try {
            Long userId = requireAuthenticatedUser(currentUser);
            List<ChatMessageResponse> lastMessages = messageService.getLastMessages(userId);
            return ResponseEntity.ok(lastMessages);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/unread-counts-by-conversation")
    @PreAuthorize("hasRole('USER') or hasRole('EMPRESA')")
    public ResponseEntity<?> getUnreadCountsByConversation(@AuthenticationPrincipal User currentUser) {
        try {
            Long userId = requireAuthenticatedUser(currentUser);
            Map<Long, Long> counts = messageService.getUnreadCountsByConversation(userId);
            return ResponseEntity.ok(counts);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    private Long requireAuthenticatedUser(User currentUser) {
        if (currentUser == null) {
            throw new AccessDeniedException("Usuario no autenticado");
        }
        return currentUser.getId();
    }
    
    // Inner classes para respuestas simples
    private record CountResponse(Long count) {}
}
