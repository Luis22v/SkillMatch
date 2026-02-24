package com.skillmatch.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequest {
    @NotNull(message = "Receiver ID es requerido")
    private Long receiverId;
    
    @NotBlank(message = "El contenido del mensaje es requerido")
    private String content;
    
    private String attachmentUrl;
}
