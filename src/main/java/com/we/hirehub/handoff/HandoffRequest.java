package com.we.hirehub.handoff;

import lombok.Data;

/** 클라이언트가 보내는 handoff 요청 바디 */
@Data
public class HandoffRequest {
    private String roomId;
    private String userId;
    private String message;
}
