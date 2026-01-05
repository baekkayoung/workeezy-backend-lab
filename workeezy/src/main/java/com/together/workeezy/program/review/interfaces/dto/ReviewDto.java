package com.together.workeezy.program.review.interfaces.dto;

public record ReviewDto(
        Long reviewId,
        Long programId,        // 상세 이동용
        String programName,    // 프로그램명
        String reviewText,     // 리뷰 내용
        Integer rating,        // 별점
        String userName,
        String image,          // 대표 이미지
        String region          // 지역 정보
) {

    // ⭐️ 기존 DTO에 region/image만 채운 새 DTO 생성용
    public ReviewDto withRegionAndImage(String region, String image) {
        return new ReviewDto(
                reviewId,
                programId,
                programName,
                reviewText,
                rating,
                userName,
                image,
                region
        );
    }
}
