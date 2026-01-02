package com.together.workeezy.program.program.domain.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Entity
@Table(name = "tb_place")
//@Builder 테스트 코드 용
//@NoArgsConstructor(access = AccessLevel.PROTECTED) // 테용
//@AllArgsConstructor 테용
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @Enumerated(EnumType.STRING)
    @Column(name = "place_type")
    private PlaceType placeType;

    @Size(max = 100)
    @NotNull
    @Column(name = "place_name", nullable = false, length = 100)
    private String name;

    @Size(max = 100)
    @Column(name = "place_code", length = 100)
    private String placeCode;

    @Size(max = 1000)
    @Column(name = "place_address", length = 1000)
    private String placeAddress;

    @Size(max = 50)
    @Column(name = "place_Region", length = 50)
    private String placeRegion;

    @Size(max = 15)
    @Column(name = "place_phone", length = 15)
    private String placePhone;

    @Size(max = 100)
    @Column(name = "place_equipment", length = 100)
    private String placeEquipment;

    @Size(max = 100)
    @Column(name = "place_photo1", length = 100)
    private String placePhoto1;

    @Size(max = 100)
    @Column(name = "place_photo2", length = 100)
    private String placePhoto2;

    @Size(max = 100)
    @Column(name = "place_photo3", length = 100)
    private String placePhoto3;

    @Size(max = 100)
    @Column(name = "attraction_url", length = 100)
    private String attractionUrl;

    protected Place() {
        // JPA용 기본 생성자
    }

    // ✅ "올바른 상태"로만 생성되게 강제
    public Place(Program program, PlaceType placeType, String name) {
        this.program = program;
        this.placeType = placeType;
        this.name = name;
    }

    // ✅ 의미 있는 변경만 허용 (Setter 대신)
    public void changeBasicInfo(String name, String placeCode) {
        this.name = name;
        this.placeCode = placeCode;
    }

    public void changeLocation(String placeAddress, String placeRegion) {
        this.placeAddress = placeAddress;
        this.placeRegion = placeRegion;
    }

    public void changeContact(String placePhone) {
        this.placePhone = placePhone;
    }

    public void changeEquipment(String placeEquipment) {
        this.placeEquipment = placeEquipment;
    }

    public void changePhotos(String photo1, String photo2, String photo3) {
        this.placePhoto1 = photo1;
        this.placePhoto2 = photo2;
        this.placePhoto3 = photo3;
    }

    public void changeAttractionUrl(String attractionUrl) {
        this.attractionUrl = attractionUrl;
    }
}
