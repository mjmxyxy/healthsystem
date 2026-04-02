package com.healthy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("article")
public class Article {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    private String cover;

    @TableField("author_id")
    private Long authorId;

    private Integer status;

    @TableField("publish_time")
    private LocalDateTime publishTime;

    @TableField("create_time")
    private LocalDateTime createTime;
}
