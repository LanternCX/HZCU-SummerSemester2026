package com.blooddonation.dto;

import java.math.BigDecimal;

public record ItemInsightDTO(
    long itemId,
    String title,
    String categoryName,
    BigDecimal amount,
    int status,
    String description,
    String bloodType,
    int commentCount,
    double averageRating,
    int actionCount,
    int orderCount
) {
}
