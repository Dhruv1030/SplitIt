package com.splitwise.user.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendFriendRequestDTO {

    @Email(message = "Invalid email format")
    private String receiverEmail;

    private String receiverId;
}
