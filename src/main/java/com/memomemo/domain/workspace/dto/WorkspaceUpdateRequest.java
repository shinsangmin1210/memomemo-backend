package com.memomemo.domain.workspace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkspaceUpdateRequest(
        @NotBlank @Size(max = 100) String name
) {}
