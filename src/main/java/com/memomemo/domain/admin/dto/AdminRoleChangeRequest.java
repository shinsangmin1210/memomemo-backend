package com.memomemo.domain.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AdminRoleChangeRequest(
        @NotBlank @Pattern(regexp = "USER|ADMIN") String role
) {}
