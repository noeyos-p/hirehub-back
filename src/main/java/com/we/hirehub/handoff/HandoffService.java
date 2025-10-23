package com.we.hirehub.handoff;

import com.we.hirehub.dto.HandoffDto; // ✅ dto 폴더의 DTO 사용
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class HandoffService {

    public List<HandoffDto> findPendingByRoomId(String roomId){
        return Collections.emptyList();
    }

    public HandoffDto create(HandoffRequest req){
        // dto 폴더의 HandoffDto는 아래 5개 String 필드를 가짐:
        // id, roomId, userId, status, lastMessage
        // id/lastMessage는 생성 시점에 없으니 null 처리.
        // userId는 우선 agentId를 문자열로 넣어 둠(요구에 맞게 나중에 교체 가능).
        return HandoffDto.builder()
                .id(null)
                .roomId(req.roomId())
                .userId(req.agentId() == null ? null : String.valueOf(req.agentId()))
                .status("PENDING")
                .lastMessage(null)
                .build();
    }

    public record HandoffRequest(String roomId, Long agentId){}
}
