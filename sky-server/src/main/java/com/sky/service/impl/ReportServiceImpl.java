package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 统计指定时间内的营业额数据
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        // 1. 计算 dateList, 存放从begin 到 end 的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            // 计算指定日期的后一天
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        // 2. 存放日期对应的营业额
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList){
            // 查询date 对应的营业额数据
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            // select sum(amount) from orders where order_time > ? and order_time < ? and status = 5;
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            turnoverList.add(turnover == null ? 0.0 : turnover);
        }

        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /**
     * 统计指定时间内的新增用户数据
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        // 1.查询开始时间前的用户总量
        Integer total = userMapper.countBeforeDate(begin);
        // 1. 计算 dateList, 存放从begin 到 end 的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            // 计算指定日期的后一天
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        // 2. 存放每天的新增用户数量 select count(id) from user where create_time > ? and create_time < ?
        List<Integer> newUserList = new ArrayList<>();

        for (LocalDate date : dateList){
            Integer newNum = userMapper.countByDate(date);
            newUserList.add(newNum == null ? 0 : newNum);
        }

        // 3. 存放每天的总用户数量
        List<Integer> totalUserList = new ArrayList<>();
        for(Integer num : newUserList){
            total += num;
            totalUserList.add(total);
        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();

    }
}
