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
public class GroupInvitationRequest {

    @NotBlank(message = "Invitee email is required")
    @Email(message = "Invalid invitee email format")
    private String inviteeEmail;

    @NotBlank(message = "Invitee name is required")
    private String inviteeName;

    @NotBlank(message = "Inviter name is required")
    private String inviterName;

    @NotBlank(message = "Group name is required")
    private String groupName;

    private Long groupId;
    private String groupDescription;
    private String invitationToken; // For accept/decline links
}
