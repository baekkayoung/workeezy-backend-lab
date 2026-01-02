package com.together.workeezy.reservation.domain;

import com.together.workeezy.common.exception.CustomException;
import com.together.workeezy.common.exception.ErrorCode;
import com.together.workeezy.payment.entity.Payment;
import com.together.workeezy.program.program.domain.model.entity.Place;
import com.together.workeezy.program.program.domain.model.entity.Program;
import com.together.workeezy.program.program.domain.model.entity.Room;
import com.together.workeezy.reservation.enums.ReservationStatus;
import com.together.workeezy.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static com.together.workeezy.common.exception.ErrorCode.*;
import static jakarta.persistence.FetchType.LAZY;


@Entity
@Table(name = "tb_reservation")
@Getter
@Setter
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @NotNull
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "stay_id")
    private Place stay;

    @NotNull
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "office_id")
    private Place office;

    @OneToOne(mappedBy = "reservation", fetch = LAZY, cascade = CascadeType.PERSIST)
    private Payment payment;

    @NotNull
    @Column(name = "reservation_no", nullable = false, length = 20)
    private String reservationNo;

    @NotNull
    @Column(name = "start_date", nullable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime startDate;

    @NotNull
    @Column(name = "end_date", nullable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime endDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status = ReservationStatus.waiting_payment; // waiting_payment, confirmed, cancel_requested, cancelled

    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @NotNull
    @Column(name = "total_price", nullable = false)
    private Long totalPrice;

    @NotNull
    @Column(name = "people_count", nullable = false)
    private int peopleCount;

    @Column(name = "confirm_pdf_key")
    private String confirmPdfKey;

    @OneToMany(mappedBy = "reservation")
    private List<ReservationModify> reservationModifys = new ArrayList<>();

    @OneToMany(mappedBy = "reservation")
    private List<ReservationPdf> reservationPdfs = new ArrayList<>();

    // 사용자 예약이 맞는지
    public boolean isOwnedBy(User user) {
        return this.user.getId().equals(user.getId());
    }

    public int daysUntilStart() {
        return (int) ChronoUnit.DAYS.between(
                LocalDate.now(),
                this.startDate.toLocalDate()
        );
    }

    // 수정 가능 상태 검증
    public void validateUpdatable() {
        if (!this.status.canUpdate()) {
            throw new IllegalStateException("해당 상태에서는 수정 불가");
        }
    }

    // 수정 요청 가능 상태 검증
    public void validateRequestResubmit() {
        if (!this.status.canRequestResubmit()) {
            throw new IllegalStateException("반려된 예약만 재신청이 가능합니다.");
        }
    }

    // 예약 생성 + 수정 시 공통 규칙
    // 시작일 - 종료일
    public static void validateDate(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new IllegalStateException("시작일은 종료일보다 늦을 수 없습니다.");
        }
    }

    // 수정 - 날짜 (상태검증, 날짜 검증까지)
    public void changePeriod(LocalDateTime start, LocalDateTime end) {
        validateUpdatable(); // 수정 가능한 상태인지
        validateDate(start, end); // 시작일 > 종료일 규칙
        this.startDate = start;
        this.endDate = end;
    }

    // 수정 - 인원수
    public void changePeopleCount(int count) {
        validateUpdatable();
        this.peopleCount = count;
    }

    // 룸 변경
    public void changeRoom(Room room) {
        validateUpdatable();
        this.room = room;
        this.stay = room.getPlace(); // stay 자동 동기화
    }

    // 프로그램 총 가격
    public void recalculateTotalPrice() {
        this.totalPrice = (long) this.program.getProgramPrice() * this.peopleCount;
    }

    // ================================ 예약 CRUD ========================= //


    // ***** 예약 생성 *****
    public static Reservation create(
            User user,
            Program program,
            Room room,
            Place office,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int peopleCount,
            String reservationNo
    ) {
        validateDate(startDate, endDate);

        Reservation r = new Reservation();
        r.user = user;
        r.program = program;
        r.room = room;
        r.stay = room.getPlace();
        r.office = office;
        r.startDate = startDate;
        r.endDate = endDate;
        r.peopleCount = peopleCount;
        r.reservationNo = reservationNo;
        r.status = ReservationStatus.waiting_payment;

        // 해당 예약 가격 계산
        r.recalculateTotalPrice();

        return r;
    }

    // ***** 예약 수정 *****
    public void update(
            LocalDateTime startDate,
            LocalDateTime endDate,
            int peopleCount,
            Room room
    ) {
        validateUpdatable(); // 수정 가능한지
        validateDate(startDate, endDate); // 날짜 규칙

        this.startDate = startDate;
        this.endDate = endDate;
        this.peopleCount = peopleCount;
        this.room = room;
        this.stay = room.getPlace();

        recalculateTotalPrice(); // 파생 값 계산
    }

    // 예약 재신청
    public void resubmit(LocalDateTime startDate,
                         LocalDateTime endDate,
                         int peopleCount,
                         Room room) {
        validateRequestResubmit();
        validateDate(startDate, endDate);

        this.startDate = startDate;
        this.endDate = endDate;
        this.peopleCount = peopleCount;
        this.room = room;
        this.stay = room.getPlace();

        recalculateTotalPrice();

        this.status = ReservationStatus.waiting_payment;
    }

    // todo: 추후 업데이트 공통 메서드로 추출

    // ***** 예약 취소 *****
    public boolean cancelByUser() {
        int diffDays = daysUntilStart();

        // 즉시 취소(예약 3일전)
        if (status.canCancelImmediately(diffDays)) {
            this.status = ReservationStatus.cancelled;
            return true;
        }

        // 취소 요청(예약2일전~당일)
        if (status.canRequestCancel(diffDays)) {
            this.status = ReservationStatus.cancel_requested;
            return false;
        }

        throw new CustomException(RESERVATION_CANCEL_NOT_ALLOWED);
    }

    // 결제 - 예약 연결
    public void linkPayment(Payment payment) {
        if (this.payment == null) {
            this.payment = payment;
        }
    }

    // ***** 예약 규칙 *****
    public void assertPayable() {
        if (!isPayable()) {
            throw new CustomException(PAYMENT_NOT_ALLOWED);
        }
    }

    public void assertOrderId(String orderId) {
        if (!reservationNo.equals(orderId)) {
            throw new CustomException(ORDER_ID_MISMATCH);
        }
    }

    public void assertTotalAmount(Long amount) {
        if (!totalPrice.equals(amount)) {
            throw new CustomException(PAYMENT_AMOUNT_MISMATCH);
        }
    }

    public void markConfirmed() {

        if (status == ReservationStatus.confirmed)
            throw new CustomException(ErrorCode.PAYMENT_ALREADY_COMPLETED);

        status = ReservationStatus.confirmed;
    }

    // 관리자 승인 후에만 결제 가능
    public boolean isPayable() {
        return this.status == ReservationStatus.approved;
    }

    // ==================================== 관리자 행위 =================================


    // 예약 승인 (waiting_payment → approved)
    public void approve() {
        if (this.status != ReservationStatus.waiting_payment) {
            throw new IllegalStateException("결제 대기 상태에서만 승인할 수 있습니다.");
        }
        this.status = ReservationStatus.approved;
    }

    // 예약 반려 (waiting_payment → rejected)
    public void reject(String reason) {
        if (this.status != ReservationStatus.waiting_payment) {
            throw new IllegalStateException("결제 대기 상태에서만 반려할 수 있습니다.");
        }
        this.status = ReservationStatus.rejected;
        this.rejectReason = reason;
    }

    // 취소 승인 (cancel_requested → cancelled)
    public void approveCancel() {
        if (this.status != ReservationStatus.cancel_requested) {
            throw new IllegalStateException("취소 요청 상태에서만 취소 승인할 수 있습니다.");
        }
        this.status = ReservationStatus.cancelled;
    }

    // 취소 반려 (cancel_requested → confirmed)
    public void rejectCancel(String reason) {
        if (this.status != ReservationStatus.cancel_requested) {
            throw new IllegalStateException("취소 요청 상태에서만 취소 반려할 수 있습니다.");
        }
        this.status = ReservationStatus.confirmed;
        this.rejectReason = reason;
    }

    // 예약 확정서 생성 기준
    public void ensureConfirmed() {
        if (this.status != ReservationStatus.confirmed) {
            throw new IllegalStateException("확정된 예약만 확정서를 생성/조회할 수 있습니다.");
        }
    }

    // 예약 확정서 존재 여부
    public void ensureHasConfirmPdf() {
        if (this.confirmPdfKey == null || this.confirmPdfKey.isBlank()) {
            throw new IllegalStateException("확정서 PDF가 아직 생성되지 않았습니다.");
        }
    }

    // 예약 확정서 key 업데이트
    public void updateConfirmPdfKey(String key) {
        this.confirmPdfKey = key;
    }

}