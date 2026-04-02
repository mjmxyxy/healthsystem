package com.healthy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("appointment")
public class AppointmentRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("counselor_id")
    private Long counselorId;

    @TableField("appt_time")
    private LocalDateTime apptTime;

    private Integer duration;

    // 0=待确认,1=已确认,2=已完成,3=已取消
    private Integer status;

    private String note;

    @TableField("create_time")
    private LocalDateTime createTime;
}
