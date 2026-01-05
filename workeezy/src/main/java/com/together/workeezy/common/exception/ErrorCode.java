package com.together.workeezy.common.exception;

import co.elastic.clients.elasticsearch.nodes.Http;
import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // BAD_REQUEST      400     잘못된 요청
    // UNAUTHORIZED     401     인증 실패
    // FORBIDDEN        403     권한 없음
    // NOT_FOUND        404     리소스 없음
    // CONFLICT         409     상태 충돌

    // 예약/결제
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "예약을 찾을 수 없습니다."),
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "해당 예약에 접근 권한이 없습니다."),

    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "결제 금액이 일치하지 않습니다."),
    PAYMENT_KEY_MISSING(HttpStatus.BAD_REQUEST, "paymentKey가 없습니다."),
    ORDER_ID_MISSING(HttpStatus.BAD_REQUEST, "orderId가 없습니다."),
    AMOUNT_INVALID(HttpStatus.BAD_REQUEST, "금액이 올바르지 않습니다."),
    PAYMENT_ALREADY_COMPLETED(HttpStatus.CONFLICT, "이미 결제된 예약입니다."),
    ORDER_ID_MISMATCH(HttpStatus.BAD_REQUEST, "주문 번호가 일치하지 않습니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 정보가 존재하지 않습니다."),
    PAYMENT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "승인 후 결제 가능합니다."),
    PAYMENT_NOT_REFUNDABLE(HttpStatus.BAD_REQUEST, "결제를 취소할 수 없는 상태입니다."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
    RESERVATION_CANCEL_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "해당 예약은 취소할 수 없습니다.", "관리자에게 문의 바랍니다."),
    REFUND_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 환불 정보가 존재합니다."),
    REFUND_ALREADY_COMPLETED(HttpStatus.CONFLICT, "이미 환불이 완료된 상태입니다."),

    // 예약 제한 관련
    RESERVATION_WAITING_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "예약 대기 건수가 너무 많습니다.", "예약 대기는 최대 5건까지 가능합니다."),
    RESERVATION_APPROVED_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "결제 대기 중인 예약이 너무 많습니다.", "결제 대기 상태는 최대 3건까지 가능합니다."),
    RESERVATION_CONFIRMED_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "확정된 예약이 너무 많습니다.", "확정된 예약은 최대 3건까지 가능합니다."),
    RESERVATION_TOTAL_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "예약 가능 개수를 초과했습니다.", "예약 가능 건수를 초과했습니다."),
    RESERVATION_NOT_AVAILABLE(HttpStatus.BAD_REQUEST,"예약이 불가능 합니다." ),
    INVALID_PEOPLE_COUNT(HttpStatus.BAD_REQUEST,"예약 인원은 1명 이상이어야 합니다."),
    EXCEED_MAX_PEOPLE_COUNT(HttpStatus.BAD_REQUEST,"프로그램 최대 인원을 초과합니다.","인원수를 다시 확인해 주세요."),
    RESERVATION_CANCEL_IMMEDIATE_ALLOWED(HttpStatus.BAD_REQUEST,"해당 예약은 즉시 취소가 가능합니다." ),
    RESERVATION_EXPIRED(HttpStatus.BAD_REQUEST,"만료된 예약입니다."),
    RESERVATION_START_DATE_IN_PAST(HttpStatus.BAD_REQUEST,"예약 날짜가 이미 지났습니다." ),

    // 인증/인가
    AUTH_REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "Refresh token 찾을 수 없음"),
    AUTH_REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Refresh token 만료 또는 위조"),
    AUTH_REFRESH_TOKEN_NOT_SAVED(HttpStatus.UNAUTHORIZED, "서버에 Refresh Token 없음(로그아웃된 사용자)"),
    AUTH_REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "Refresh Token 불일치(탈취/중복 로그인 가능)"),

    // 사용자
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    INVALID_PHONE_NUMBER(HttpStatus.BAD_REQUEST, "휴대폰 번호 형식이 올바르지 않습니다. (010-1234-5678)"),
    PHONE_NOT_CHANGED(HttpStatus.BAD_REQUEST, "기존 연락처와 동일합니다."),
    DUPLICATE_PHONE_NUMBER(HttpStatus.CONFLICT, "이미 사용 중인 휴대폰 번호입니다."),

    PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "현재 비밀번호가 일치하지 않습니다."),
    PASSWORD_CONFIRM_NOT_MATCH(HttpStatus.BAD_REQUEST, "새 비밀번호가 서로 일치하지 않습니다."),
    DUPLICATE_PASSWORD(HttpStatus.CONFLICT, "기존 비밀번호와 동일한 비밀번호는 사용할 수 없습니다."),
    INVALID_PASSWORD_LENGTH(HttpStatus.BAD_REQUEST, "비밀번호는 8~16자여야 합니다."),
    INVALID_PASSWORD_NUMBER(HttpStatus.BAD_REQUEST, "숫자가 포함되어야 합니다."),
    INVALID_PASSWORD_UPPER(HttpStatus.BAD_REQUEST, "대문자가 포함되어야 합니다."),
    INVALID_PASSWORD_LOWER(HttpStatus.BAD_REQUEST, "소문자가 포함되어야 합니다."),
    INVALID_PASSWORD_SPECIAL(HttpStatus.BAD_REQUEST, "특수문자가 포함되어야 합니다.");

    // 검색


    private final HttpStatus status;
    private final String message;
    private final String detail;

    ErrorCode(HttpStatus status, String message) {
        this(status, message, null);
    }

    ErrorCode(HttpStatus status, String message, String detail) {
        this.status = status;
        this.message = message;
        this.detail = detail;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getDetail() {
        return detail;
    }

}
