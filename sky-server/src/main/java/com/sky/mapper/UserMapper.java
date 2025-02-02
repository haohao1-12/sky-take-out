package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.Map;

@Mapper
public interface UserMapper {

    /**
     * 根据openid查询用户信息
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{open_id}")
    User getByOpenid(String openid);

    /**
     * 插入用户数据
     * @param user
     */
    void insert(User user);

    /**
     * 根据id查询用户数据
     * @param id
     * @return
     */
    @Select("select * from user where id = #{id}")
    User getById(Long id);

    /**
     * 根据动态条件统计用户数量
     * @param date
     * @return
     */
    Integer countByDate(LocalDate date);

    /**
     * 根据动态条件统计用户数量
     * @param begin
     * @return
     */
    @Select("select count(id) from user where create_time < #{begin}")
    Integer countBeforeDate(LocalDate begin);
}
