package com.we.hirehub.service;

import com.we.hirehub.dto.LiveChatDto;
import com.we.hirehub.entity.LiveChat;
import com.we.hirehub.entity.Session;
import com.we.hirehub.entity.Users;
import com.we.hirehub.repository.LiveChatRepository;
import com.we.hirehub.repository.SessionRepository;
import com.we.hirehub.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiveChatService {

    private final LiveChatRepository liveChatRepository;
    private final SessionRepository sessionRepository;
    private final UsersRepository usersRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional(readOnly = true)
    public List<LiveChatDto> getRecentMessages(String sessionId, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createAt"));
        List<LiveChat> chats = liveChatRepository.findBySessionId(sessionId, pageRequest);

        return chats.stream()
                .sorted((a, b) -> a.getCreateAt().compareTo(b.getCreateAt()))
                .map(chat -> toLiveChatDto(chat))
                .collect(Collectors.toList());
    }

    @Transactional
    public void send(String sessionId, String content, String nickname) {
        // Session 가져오기 또는 생성
        Session session = sessionRepository.findById(sessionId)
                .orElseGet(() -> {
                    Session newSession = new Session();
                    newSession.setId(sessionId);
                    newSession.setCtx(new HashMap<>());
                    return sessionRepository.save(newSession);
                });

        // 닉네임 가져오기
        String finalNickname = getNickname(nickname);
        log.info("채팅 메시지 전송 - 닉네임: {}, 내용: {}", finalNickname, content);

        // Session의 ctx에 현재 사용자 닉네임 저장 (임시 저장소로 활용)
        if (session.getCtx() == null) {
            session.setCtx(new HashMap<>());
        }
        Map<String, Object> ctx = session.getCtx();

        // 메시지별 닉네임을 ctx에 저장 (메시지 ID를 키로 사용)
        // 또는 간단하게 마지막 사용자 닉네임만 저장
        ctx.put("lastUserNickname", finalNickname);
        sessionRepository.save(session);

        // LiveChat 저장
        LiveChat chat = new LiveChat();
        chat.setSession(session);
        chat.setContent(content);
        chat.setCreateAt(LocalDateTime.now());
        LiveChat saved = liveChatRepository.save(chat);

        // DTO로 변환 시 닉네임 포함
        LiveChatDto dto = LiveChatDto.builder()
                .id(saved.getId())
                .content(saved.getContent())
                .createAt(saved.getCreateAt())
                .sessionId(saved.getSession().getId())
                .nickname(finalNickname)  // 방금 가져온 닉네임 사용
                .build();

        // WebSocket으로 전송
        messagingTemplate.convertAndSend("/topic/rooms/" + sessionId, dto);
    }

    private String getNickname(String requestNickname) {
        // 1. 요청에서 닉네임이 있으면 사용
        if (requestNickname != null && !requestNickname.isEmpty()) {
            return requestNickname;
        }

        // 2. 현재 로그인한 사용자 정보 가져오기
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            log.info("인증 정보: {}", authentication);

            if (authentication != null && authentication.isAuthenticated()) {
                String principal = authentication.getName();
                log.info("인증된 사용자 Principal: {}", principal);

                if (principal != null && !principal.equals("anonymousUser")) {
                    // 이메일로 사용자 조회
                    Users user = usersRepository.findByEmail(principal).orElse(null);

                    if (user != null) {
                        log.info("사용자 찾음 - ID: {}, 닉네임: {}, 이름: {}",
                                user.getId(), user.getNickname(), user.getName());

                        // 닉네임 우선, 없으면 이름 사용
                        if (user.getNickname() != null && !user.getNickname().isEmpty()) {
                            return user.getNickname();
                        }
                        if (user.getName() != null && !user.getName().isEmpty()) {
                            return user.getName();
                        }
                    } else {
                        log.warn("사용자를 찾을 수 없음: {}", principal);
                    }
                }
            } else {
                log.warn("인증 정보가 없거나 인증되지 않음");
            }
        } catch (Exception e) {
            log.error("닉네임 가져오기 실패", e);
        }

        // 3. 모두 실패하면 익명
        return "익명";
    }

    private LiveChatDto toLiveChatDto(LiveChat chat) {
        // 기존 메시지의 경우 Session ctx에서 닉네임 가져오기 시도
        String nickname = "익명";

        if (chat.getSession() != null && chat.getSession().getCtx() != null) {
            Map<String, Object> ctx = chat.getSession().getCtx();
            if (ctx.containsKey("lastUserNickname")) {
                nickname = (String) ctx.get("lastUserNickname");
            }
        }

        return LiveChatDto.builder()
                .id(chat.getId())
                .content(chat.getContent())
                .createAt(chat.getCreateAt())
                .sessionId(chat.getSession().getId())
                .nickname(nickname)
                .build();
    }
}
