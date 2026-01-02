package com.together.workeezy.reservation.enums;

public enum ReservationStatus {

    waiting_payment,  // 결제대기 - 예약 신청 직후(관리자 승인전)
    approved,          // 승인 완료 - 관리자 승인 (아직 결제 안 함) -> 취소 바로 가능
    rejected,          // 관리자 반송
    confirmed,		  // 결제 완료(예약 확정)
    cancel_requested, // 취소 신청(취소 3일전까지는 바로 취소, 당일은 불가, 전날/전전날은 승인)
    cancelled;		  // 취소 완료

    // 수정 가능 여부
    public boolean canUpdate() {
        return this == waiting_payment;
    }

    // 즉시 취소 가능 여부(상태 변경 X)
    public boolean canCancelImmediately(int diffDays) {
        if (this == waiting_payment) return true;
        if (this == approved) return true;
        return this == confirmed && diffDays >= 3;
    }

    // 취소 요청 가능 (관리자 승인 필요)(상태 변경 X)
    public boolean canRequestCancel(int diffDays) {
        return this == confirmed && diffDays >= 1 && diffDays < 3;
    }

    // 예약 변경 요청 가능 여부
    public boolean canRequestResubmit( ){
        return this == rejected;
    }

}
