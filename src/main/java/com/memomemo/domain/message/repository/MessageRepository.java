package com.memomemo.domain.message.repository;

import com.memomemo.domain.message.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // 채널 메시지 페이지네이션 (최신순, 커서 기반)
    @Query("""
            SELECT m FROM Message m
            WHERE m.channel.id = :channelId
              AND m.parent IS NULL
              AND (:cursor IS NULL OR m.id < :cursor)
            ORDER BY m.id DESC
            """)
    Slice<Message> findByChannelIdWithCursor(@Param("channelId") Long channelId,
                                              @Param("cursor") Long cursor,
                                              Pageable pageable);

    // 스레드 답글 조회
    List<Message> findAllByParentIdOrderByCreatedAtAsc(Long parentId);

    // 풀텍스트 검색 (PostgreSQL tsvector)
    @Query(value = """
            SELECT * FROM messages
            WHERE channel_id = :channelId
              AND search_vector @@ plainto_tsquery('simple', :keyword)
            ORDER BY created_at DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Message> searchByKeyword(@Param("channelId") Long channelId,
                                   @Param("keyword") String keyword,
                                   @Param("limit") int limit);
}
