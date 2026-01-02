package com.together.workeezy.program.program.domain.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "tb_room")
//@NoArgsConstructor(access = AccessLevel.PROTECTED) 테스트 코드용
//@AllArgsConstructor(access = AccessLevel.PRIVATE)
//@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(name = "room_no")
    private Integer roomNo;

    @Column(name = "room_people")
    private Integer roomPeople;

    @Column(name = "room_service", length = 1000)
    private String roomService;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false)
    private RoomType roomType;

    protected Room() {
        // JPA
    }

    public Room(Place place, RoomType roomType, Integer roomNo, Integer roomPeople, String roomService) {
        this.place = place;
        this.roomType = roomType;
        this.roomNo = roomNo;
        this.roomPeople = roomPeople;
        this.roomService = roomService;
    }

    // 도메인 행위(의미 있는 변경만)
    public void changeCapacity(Integer roomPeople) {
        this.roomPeople = roomPeople;
    }

    public void changeService(String roomService) {
        this.roomService = roomService;
    }

    public void changeRoomType(RoomType roomType) {
        this.roomType = roomType;
    }
}
