package com.blooddonation.dto;

/**
 * 表示一条库存推荐及其可解释理由。
 *
 * @param item 库存综合信息
 * @param reason 推荐理由
 */
public record RecommendationDTO(ItemInsightDTO item, String reason) {
}
