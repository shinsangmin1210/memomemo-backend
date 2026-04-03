package com.memomemo.domain.workspace.dto;

import jakarta.validation.constraints.NotNull;

public record WorkspaceInviteRequest(
        @NotNull Long userId
) {}
