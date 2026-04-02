package com.healthy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("accounts")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String account;
    private String password;
    private String name;
    private String studentId;
    private String gender;
    @TableField(exist = false)
    private String avatar;
    private String role; // student|counselor|admin
    private Boolean verified; // student identity verified

    @TableField(exist = false)
    private String nickname;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
