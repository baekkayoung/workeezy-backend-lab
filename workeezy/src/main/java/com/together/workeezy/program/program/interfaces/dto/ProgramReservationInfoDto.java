package com.together.workeezy.program.program.interfaces.dto;

import java.util.List;

public record ProgramReservationInfoDto(
        Long programId,
        String programTitle,
        int programPrice,
        Long stayId,
        String stayName,
        Long officeId,
        String officeName,
        List<RoomSimpleDto> rooms,
        int programPeople
) {}
