package com.sky.service.impl;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressBookServiceImpl implements AddressBookService {

    @Autowired
    AddressBookMapper addressBookMapper;

    /**
     * 新增地址
     * @param addressBook
     */
    public void save(AddressBook addressBook) {
        Long userId = BaseContext.getCurrentId();
        addressBook.setUserId(userId);
        addressBook.setIsDefault(0);
        addressBookMapper.insert(addressBook);
    }


    public List<AddressBook> list() {
        Long userId = BaseContext.getCurrentId();
        AddressBook addressBook = AddressBook.builder().userId(userId).build();
        return addressBookMapper.list(addressBook);
    }

    @Override
    public AddressBook getDefault() {
        Long userId = BaseContext.getCurrentId();
        AddressBook addressBook = AddressBook.builder()
                .userId(userId)
                .isDefault(1)
                .build();
        return addressBookMapper.list(addressBook).get(0);
    }

    @Transactional
    public void setDefault(AddressBook addressBook) {
        // 将当前用户的所有地址设置为非默认地址
        addressBook.setIsDefault(0);
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBookMapper.updateIsDefaultByUserId(addressBook);
        // 将当前地址设置为默认地址
        addressBook.setIsDefault(1);
        addressBookMapper.update(addressBook);
    }

    @Override
    public void update(AddressBook addressBook) {
        addressBookMapper.update(addressBook);
    }

    @Override
    public AddressBook getById(Long id) {
        return addressBookMapper.getById(id);
    }

    @Override
    public void deleteById(Long id) {
        addressBookMapper.deleteById(id);
    }
}
