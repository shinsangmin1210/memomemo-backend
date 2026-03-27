package com.memomemo.domain.message.repository;

import com.memomemo.domain.message.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findAllByMessageId(Long messageId);
}
