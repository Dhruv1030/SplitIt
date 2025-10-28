package com.splitwise.group.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddMemberRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    private String role; // ADMIN or MEMBER (default: MEMBER)
}
