package com.healthy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.healthy.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

	@Select("SELECT id, account, password, name, student_id AS studentId, gender, role, verified, created_at AS createdAt, updated_at AS updatedAt FROM accounts WHERE account = #{account} LIMIT 1")
	User selectByAccount(@Param("account") String account);

	@Select("SELECT id, account, password, name, student_id AS studentId, gender, role, verified, created_at AS createdAt, updated_at AS updatedAt FROM accounts WHERE id = #{id} LIMIT 1")
	User selectByIdSafe(@Param("id") Long id);

	@Select("SELECT COUNT(1) FROM accounts WHERE account = #{account}")
	long countByAccount(@Param("account") String account);

}
