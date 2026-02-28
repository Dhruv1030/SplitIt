package com.splitwise.group.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupInvitationEmailRequest {
    private String inviteeEmail;
    private String inviteeName;
    private String inviterName;
    private String groupName;
    private Long groupId;
    private String groupDescription;
}
