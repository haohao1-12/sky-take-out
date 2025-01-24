package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.core.annotation.Order;

import java.time.LocalDateTime;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单数据
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号和用户id查询订单
     * @param outTradeNo
     * @return
     */
    @Select("select * from orders where number = #{outTradeNo} and user_id = #{userId}")
    Orders getByNumberAndUserId(String outTradeNo, Long userId);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     *
     * @param orderStatus
     * @param orderPaidStatus
     * @param checkoutTime
     * @param id
     */
    @Update("update orders set status = #{orderStatus}, pay_status = #{orderPaidStatus}, checkout_time = #{checkoutTime} where id = #{id}")
    void updateStatus(Integer orderStatus, Integer orderPaidStatus, LocalDateTime checkoutTime, Long id);

    /**
     * 分页查询订单，并按照下单时间排序
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id = #{id}")
    Orders getByOrderId(Long orderId);
}
