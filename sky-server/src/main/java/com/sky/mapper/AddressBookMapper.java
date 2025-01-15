package com.sky.mapper;

import com.sky.entity.AddressBook;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AddressBookMapper {

    /**
     * 新增地址
     * @param addressBook
     */
    @Insert("insert into address_book (user_id, consignee, sex, phone, province_code, province_name, city_code, city_name, district_code, district_name, detail, label, is_default) values " +
            "(#{userId}, #{consignee}, #{sex}, #{phone}, #{provinceCode}, #{provinceName}, #{cityCode}, #{cityName}, #{districtCode},#{districtName}, #{detail}, #{label}, #{isDefault})")
    void insert(AddressBook addressBook);

    /**
     * 查询地址
     * @param addressBook
     * @return
     */
    List<AddressBook> list(AddressBook addressBook);

    void update(AddressBook addressBook);

    @Select("select * from address_book where id = #{io}")
    AddressBook getById(Long id);

    @Delete("delete from address_book where id = #{id}")
    void deleteById(Long id);
}
