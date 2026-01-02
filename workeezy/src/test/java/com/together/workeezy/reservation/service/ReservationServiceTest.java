package com.together.workeezy.reservation.service;

import com.together.workeezy.reservation.dto.ReservationCreateDto;
import com.together.workeezy.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    // 체크인 - 아웃 공통 유틸메서드
    private LocalDateTime normalizeCheckIn(LocalDateTime date) {
        return date.toLocalDate().atTime(15, 0);
    }

    private LocalDateTime normalizeCheckOut(LocalDateTime date) {
        return date.toLocalDate().plusDays(2).atTime(11, 0);
    }

//    private Long createdReservationId; // 테스트로 생성될 예약 id 저장용

    @Test
    void 동시_예약_요청시_1건만_성공해야한다() throws Exception {
        int threadCount = 300; // 예약 요청수
        ExecutorService executorService = Executors.newFixedThreadPool(32); // 스레드풀 32개
        CountDownLatch latch = new CountDownLatch(threadCount); // 스레드가 끝날때 latch는 0으로

        // 성공 - 실패 카운터
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();


        // 1. 테스트에서 사용할 원본 날짜와 "실제 DB에 저장될 기대 시간"을 명시적으로 선언
        LocalDateTime requestDate = LocalDateTime.of(2026, 5, 1, 14, 0);
        LocalDateTime expectedStoredDate = LocalDateTime.of(2026, 5, 1, 15, 0); // 서비스가 15시로 바꿀 것을 알고 있음


        /* 막아두기 builder
        ReservationCreateDto dto = ReservationCreateDto.builder()
                .roomId(1L)
                .programId(1L)
                .officeId(1L)
                .startDate(LocalDateTime.of(2026, 5, 1, 11, 0))
                .peopleCount(2)
                .build();*/

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
//                    reservationService.createNewReservation(dto, "hong@company.com");
                    System.out.println("예약 성공@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // 여기에 로그를 찍어보세요! "해당 이메일의 유저가 존재하지 않습니다"라고 뜰 거예요.
                    System.out.println("❌ 예약 실패 원인: " + e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        // 테스트 스레드 기다림

        latch.await();

        // ✅ 검증
        assertThat(successCount.get()).isEqualTo(1); // 1개

        assertThat(failCount.get()).isEqualTo(threadCount - 1); // 29개 여야함

        // DB 조회 시 서비스 메서드를 빌려 쓰는 게 아니라,
        // "이 시간으로 저장되어야 해"라는 확정된 값을 사용
        long count = reservationRepository.countByRoomIdAndStartDate(
                1L,
                expectedStoredDate
        );

        assertThat(count).isEqualTo(1);
    }


    @AfterEach
    void tearDown() {
        reservationRepository.deleteAllByRoomIdAndStartDate(1L, LocalDateTime.of(2026,5,1,15,0));
    }

    }




