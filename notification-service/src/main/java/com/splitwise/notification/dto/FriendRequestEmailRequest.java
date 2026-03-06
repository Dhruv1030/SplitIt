package com.splitwise.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestEmailRequest {

    @NotBlank(message = "Receiver email is required")
    @Email(message = "Invalid receiver email format")
    private String receiverEmail;

    @NotBlank(message = "Receiver name is required")
    private String receiverName;

    @NotBlank(message = "Sender name is required")
    private String senderName;

    private String senderEmail;

    @NotBlank(message = "Accept URL is required")
    private String acceptUrl;

    @NotBlank(message = "Decline URL is required")
    private String declineUrl;
}
