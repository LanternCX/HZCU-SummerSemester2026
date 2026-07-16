package com.blooddonation.dto;

import java.math.BigDecimal;

/**
 * 汇总库存主数据、详情、评分、热度和申请数量。
 *
 * @param itemId 库存编号
 * @param title 库存名称
 * @param categoryName 分类名称
 * @param amount 可用数量
 * @param status 库存状态
 * @param description 详情描述
 * @param bloodType 血型
 * @param commentCount 评论数量
 * @param averageRating 平均评分
 * @param actionCount 行为次数
 * @param orderCount 申请数量
 */
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
