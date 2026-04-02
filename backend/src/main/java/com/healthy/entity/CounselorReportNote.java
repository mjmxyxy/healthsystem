package com.healthy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("counselor_report_note")
public class CounselorReportNote {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("counselor_id")
    private Long counselorId;

    @TableField("report_id")
    private Long reportId;

    private String analysis;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
