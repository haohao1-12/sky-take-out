package com.sky.service;

import com.sky.entity.AddressBook;

public interface AddressBookService {
    /**
     * 新增地址
     * @param addressBook
     */
    void save(AddressBook addressBook);
}
