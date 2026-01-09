package com.together.workeezy.reservation.repository;

import com.together.workeezy.reservation.domain.Reservation;
import com.together.workeezy.reservation.enums.ReservationStatus;
import com.together.workeezy.reservation.dto.AdminReservationDetailDto;
import com.together.workeezy.reservation.dto.AdminReservationListDto;
import com.together.workeezy.reservation.dto.ReservationResponseDto;
import com.together.workeezy.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // ***** 오늘 날짜(yyyyMMdd)로 시작하는 예약번호 중 가장 큰 값 1개 조회 ***** //
    @Query("""
    SELECT r.reservationNo
    FROM Reservation r
    WHERE r.reservationNo LIKE CONCAT(:datePrefix, '%')
    ORDER BY r.reservationNo DESC
""")
    List<String> findLatestReservationNoByDate(@Param("datePrefix") String datePrefix, Pageable pageable);
    List<Reservation> findByUserId(Long userId);

    // ***** 해당 사용자 예약 전체 조회 ***** //
    @Query("""
select new com.together.workeezy.reservation.dto.ReservationResponseDto(
    r.id,
    r.reservationNo,
    r.status,
    u.userName,
    u.company,
    u.phone,
    r.startDate,
    r.endDate,
    p.title,
    p.id,
    s.name,
    o.name,
    r.room.id,
    r.room.roomType,
    r.totalPrice,
    r.peopleCount,
    r.rejectReason,
    r.createdDate,
    s.placePhoto1,
    s.placePhoto2,
    s.placePhoto3
)
from Reservation r
join r.user u
join r.program p
join r.stay s
join r.office o
join r.room r2
where r.user.id = :userId
and (
    :keyword is null
    or p.title like concat('%', :keyword, '%')
)

and (
    :status is null
    or r.status = :status
)

  and (
        :cursorDate is null
        or r.createdDate < :cursorDate
        or (r.createdDate = :cursorDate and r.id < :cursorId)
      )
order by r.createdDate desc, r.id desc
""")
    List<ReservationResponseDto> findMyReservationsWithCursor(
            Long userId,
            LocalDateTime cursorDate,
            Long cursorId,
            String keyword,
            ReservationStatus status,
            Pageable pageable
    );

    // ***** 사용자 단건 조회 ***** //
    @Query("""
select new com.together.workeezy.reservation.dto.ReservationResponseDto(
    r.id,
    r.reservationNo,
    r.status,
    u.userName,
    u.company,
    u.phone,
    r.startDate,
    r.endDate,
    p.title,
    p.id,
    s.name,
    o.name,
    rm.id,
    rm.roomType,
    r.totalPrice,
    r.peopleCount,
    r.rejectReason,
    r.createdDate,
    s.placePhoto1,
    s.placePhoto2,
    s.placePhoto3
)
from Reservation r
join r.user u
join r.program p
join r.stay s
join r.office o
join r.room rm
where r.id = :reservationId
  and u.email = :email
""")
    Optional<ReservationResponseDto> findMyReservationDto(
            @Param("reservationId") Long reservationId,
            @Param("email") String email
    );

    // 관리자 - 전체 유저 예약 리스트 조회
    @Query("""
select new com.together.workeezy.reservation.dto.AdminReservationListDto(
    r.id,
    r.reservationNo,
    p.title,
    u.userName,
    r.status,
    r.createdDate
)
from Reservation r
join r.user u
join r.program p
where (:status is null or r.status = :status)
  and (
       :keyword is null
       or :keyword = ''
       or u.userName like concat('%', :keyword, '%')
       or p.title like concat('%', :keyword, '%')
  )
order by r.id desc
""")
    Page<AdminReservationListDto> findAdminReservationListDtos(
            ReservationStatus status,
            String keyword,
            Pageable pageable
    );

    // 커서 기반 페이지네이션으로 변경
    @Query("""
    select new com.together.workeezy.reservation.dto.AdminReservationListDto(
        r.id,
        r.reservationNo,
        p.title,
        u.userName,
        r.status,
        r.createdDate
    )
    from Reservation r
    join r.user u
    join r.program p
    where (:status is null or r.status = :status)
    and (
           :keyword is null
           or :keyword = ''
           or u.userName like concat('%', :keyword, '%')
           or p.title like concat('%', :keyword, '%')
           or r.reservationNo like concat('%', :keyword, '%')
      )
      and (:cursor is null or r.id < :cursor)
      and (:checkInFrom is null or r.startDate >= :checkInFrom)
      and (:checkInTo   is null or r.startDate <  :checkInTo)
    order by r.id desc
""")
    List<AdminReservationListDto> findAdminReservationsByCursor(
            @Param("status") ReservationStatus status,
            @Param("keyword") String keyword,
            @Param("cursor") Long cursor,
            @Param("checkInFrom") LocalDate checkInFrom,
            @Param("checkInTo") LocalDate checkInTo,
            Pageable pageable   // size만 사용 (PageRequest.of(0, size))
    );



    @Query("""
select new com.together.workeezy.reservation.dto.AdminReservationDetailDto(
    r.id,
    r.reservationNo,
    r.status,
    p.title,
    u.userName,
    u.company,
    u.phone,
    u.email,
    r.startDate,
    r.endDate,
    r.peopleCount,
    s.name,
    rm.roomType,
    (
        select pl.name
        from Place pl
        where pl.program = p
          and pl.placeType = com.together.workeezy.program.program.domain.model.entity.PlaceType.office
                                                                                  
    )
)
from Reservation r
join r.program p
join r.user u
left join r.stay s
left join r.room rm
where r.id = :reservationId
""")
    Optional<AdminReservationDetailDto> findAdminReservationDetailDto(
            @Param("reservationId") Long reservationId
    );

    // 결제 시 예약 검증
    Optional<Reservation> findByIdAndUserId(Long reservationId, Long userId);

    Optional<Reservation> findByReservationNo(String s);

    int countByUserAndStatus(User user, ReservationStatus status);

    // 관리자 - 예약 상세 조회
//    @Query("""
//        select r
//        from Reservation r
//          join fetch r.program p
//          join fetch r.user u
//          left join fetch r.stay s
//          left join fetch r.room rm
//          left join fetch p.places pl
//        where r.id = :reservationId
//    """)
//    Optional<Reservation> findAdminReservationDetail(
//            @Param("reservationId") Long reservationId
//    );

    // 신규 예약용
    @Query("""
SELECT COUNT(r) > 0 FROM Reservation r
WHERE r.room.id = :roomId
  AND r.status IN (
    com.together.workeezy.reservation.enums.ReservationStatus.waiting_payment,
    com.together.workeezy.reservation.enums.ReservationStatus.approved,
    com.together.workeezy.reservation.enums.ReservationStatus.confirmed
  )
  AND r.startDate < :endDate
  AND r.endDate > :startDate
""")
    boolean existsOverlap(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // 수정용 (자기 자신 제외)
    @Query("""
SELECT COUNT(r) > 0 FROM Reservation r
WHERE r.room.id = :roomId
  AND r.status IN (
    com.together.workeezy.reservation.enums.ReservationStatus.waiting_payment,
    com.together.workeezy.reservation.enums.ReservationStatus.approved,
    com.together.workeezy.reservation.enums.ReservationStatus.confirmed
  )
  AND r.startDate < :endDate
  AND r.endDate > :startDate
  AND r.id <> :excludeId
""")
    boolean existsOverlapExcept(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("excludeId") Long excludeId
    );


    // 비관적락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT r
    FROM Reservation r
    WHERE r.room.id = :roomId
      AND r.status IN (
        com.together.workeezy.reservation.enums.ReservationStatus.waiting_payment,
        com.together.workeezy.reservation.enums.ReservationStatus.approved,
        com.together.workeezy.reservation.enums.ReservationStatus.confirmed
      )
      AND r.startDate < :endDate
      AND r.endDate > :startDate
""")
    List<Reservation> findConflictingReservationsForUpdate(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // 방법 1: Spring Data JPA 쿼리 메서드 (가장 추천)
    long countByRoomIdAndStartDate(Long roomId, LocalDateTime startDate);

    @Modifying
    @Transactional
    @Query("DELETE FROM Reservation r WHERE r.room.id = :roomId AND r.startDate = :startDate")
    void deleteAllByRoomIdAndStartDate(Long roomId, LocalDateTime startDate);
}

