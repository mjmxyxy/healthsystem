package com.healthy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("counselor_student_note")
public class CounselorStudentNote {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("counselor_id")
    private Long counselorId;

    @TableField("student_id")
    private Long studentId;

    private String note;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
