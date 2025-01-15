package com.sky.controller.user;

import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userAddressController")
@RequestMapping("/user/addressBook")
@Api(tags = "C端 - 地址相关接口")
@Slf4j
public class AddressBookController {

    @Autowired
    private AddressBookService addressService;


    @PostMapping
    @ApiOperation("新增地址")
    public Result add(@RequestBody AddressBook addressBook){
        log.info("新增地址");
        addressService.save(addressBook);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("查询当前用户的所有地址信息")
    public Result<List<AddressBook>> list(){
        log.info("查询当前用户的所有地址信息");
        List<AddressBook> list = addressService.list();
        return Result.success(list);
    }

    @GetMapping("/default")
    @ApiOperation("查询默认地址")
    public Result<AddressBook> getDefault(){
        log.info("查询默认地址");
        AddressBook addressBook = addressService.getDefault();
        return Result.success(addressBook);
    }

    @PutMapping("/default")
    @ApiOperation("设置默认地址")
    public Result setDefault(@RequestBody AddressBook addressBook){
        log.info("设置默认地址");
        addressService.setDefault(addressBook);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("修改地址")
    public Result update(@RequestBody AddressBook addressBook){
        log.info("修改地址");
        addressService.update(addressBook);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询地址")
    public Result<AddressBook> getById(@PathVariable Long id){
        log.info("根据id查询地址");
        AddressBook addressBook = addressService.getById(id);
        return Result.success(addressBook);
    }

    @DeleteMapping
    @ApiOperation("删除地址")
    public Result delete(Long id){
        log.info("删除地址");
        addressService.deleteById(id);
        return Result.success();
    }

}
