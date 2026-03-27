package com.memomemo.domain.workspace.service;

import com.memomemo.domain.workspace.dto.WorkspaceResponse;
import com.memomemo.domain.workspace.repository.WorkspaceRepository;
import com.memomemo.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;

    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspace(Long workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .map(WorkspaceResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("워크스페이스", workspaceId));
    }
}
