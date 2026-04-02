package com.healthy.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("assessment")
public class Report {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("scale_id")
    private Long scaleId;

    @TableField("total_score")
    private Double totalScore;

    @TableField("standard_score")
    private Double avgScore;

    @TableField("dimension_scores")
    private String dimensionScores;

    @TableField("level_desc")
    private String level;

    @TableField("report")
    private String details; // JSON string

    @TableField("answers")
    private String answers;

    @TableField("level")
    private Integer levelCode;

    @TableField("test_time")
    private LocalDateTime testTime;

    @TableField("create_time")
    private LocalDateTime createdAt;
}
