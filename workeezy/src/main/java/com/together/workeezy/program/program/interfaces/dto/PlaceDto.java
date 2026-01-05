package com.together.workeezy.program.program.interfaces.dto;

import com.together.workeezy.program.program.domain.model.entity.PlaceType;
import java.util.List;

public record PlaceDto(
        Long id,
        String name,
        String address,
        String phone,
        String photo1,
        String photo2,
        String photo3,
        String photo4,
        String equipment,
        String url,
        PlaceType type,
        String region,
        List<RoomDto> rooms   // 숙소인 경우에만 사용
) {}
