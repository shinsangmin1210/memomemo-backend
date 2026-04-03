package com.memomemo.global.init;

import com.memomemo.domain.user.entity.User;
import com.memomemo.domain.user.repository.UserRepository;
import com.memomemo.domain.workspace.entity.Workspace;
import com.memomemo.domain.workspace.entity.WorkspaceMember;
import com.memomemo.domain.workspace.repository.WorkspaceMemberRepository;
import com.memomemo.domain.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByUsername("admin")) {
            return;
        }

        User admin = User.localBuilder()
                .username("admin")
                .email("admin@admin.com")
                .displayName("Admin")
                .passwordHash(passwordEncoder.encode("1"))
                .role("ADMIN")
                .build();
        userRepository.save(admin);
        log.info("Admin account created.");

        Workspace workspace = Workspace.builder()
                .name("기본 워크스페이스")
                .slug("default")
                .owner(admin)
                .build();
        workspaceRepository.save(workspace);

        WorkspaceMember member = WorkspaceMember.builder()
                .workspace(workspace)
                .user(admin)
                .role(WorkspaceMember.WorkspaceRole.OWNER)
                .build();
        workspaceMemberRepository.save(member);

        log.info("Default workspace created.");

        if (!userRepository.existsByUsername("user1")) {
            User testUser = User.localBuilder()
                    .username("user1")
                    .email("user1@test.com")
                    .displayName("테스트유저1")
                    .passwordHash(passwordEncoder.encode("1"))
                    .role("USER")
                    .build();
            userRepository.save(testUser);

            workspaceMemberRepository.save(WorkspaceMember.builder()
                    .workspace(workspace)
                    .user(testUser)
                    .role(WorkspaceMember.WorkspaceRole.MEMBER)
                    .build());

            log.info("Test user1 created.");
        }
    }
}
