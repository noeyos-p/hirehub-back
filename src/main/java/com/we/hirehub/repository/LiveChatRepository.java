package com.we.hirehub.repository;

import com.we.hirehub.entity.LiveChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LiveChatRepository extends JpaRepository<LiveChat, Long> {
    List<LiveChat> findTop30ByOrderByCreateAtDesc();
}
