package com.healthy.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("scale_question")
public class Question {
    @TableId
    private Long id;

    @TableField("scale_id")
    private Long scaleId;

    @TableField("question_no")
    private Integer seq;

    @TableField("content")
    private String text;

    @TableField("options")
    private String options;

    @TableField("reverse_score")
    private Integer reverseScore;

    private String dimension;

    @TableField(exist = false)
    private Integer maxScore;

    @TableField("create_time")
    private LocalDateTime createdAt;
}
