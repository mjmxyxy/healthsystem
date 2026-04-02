package com.healthy.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("scale")
public class Scale {
    @TableId
    private Long id;
    private String code;
    private String name;
    private String description;

    @TableField("question_count")
    private Integer questionCount;

    private String dimensions;

    @TableField("scoring_rules")
    private String scoringRules;

    @TableField("create_time")
    private LocalDateTime createTime;
}
