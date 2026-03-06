package com.splitwise.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestDTO {
    private String id;
    private String senderId;
    private String senderName;
    private String senderEmail;
    private String receiverId;
    private String receiverName;
    private String receiverEmail;
    private String status;
    private LocalDateTime createdAt;
}
