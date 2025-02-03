package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkspaceService workspaceService;

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

    /**
     * 统计指定时间内的订单数据
     * @param begin
     * @param end
     * @return
     */
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        // 1. 计算 dateList, 存放从begin 到 end 的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            // 计算指定日期的后一天
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();

        // 2. 遍历dateList集合，查询每天的有效订单数和订单总数
        for (LocalDate date : dateList) {
            Map map = new HashMap();
            map.put("date", date);
            // 查询每天的订单总数 select count(id) from orders where order_time like '2023-03-01%'
            Integer totalOrderCount = orderMapper.getCountByMap(map);
            map.put("status", Orders.COMPLETED);
            // 查询每天的有效订单数 select count(id) from orders where order_time like '2023-03-01%' and status = 5
            Integer validOrderCount = orderMapper.getCountByMap(map);

            orderCountList.add(totalOrderCount);
            validOrderCountList.add(validOrderCount);
        }
        // 计算时间区间内的订单总数量
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        // 计算时间区间内的有效订单数量
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();
        // 计算订单完成率
        Double orderCompletionRate = totalOrderCount == 0? 0.0 : validOrderCount.doubleValue() / totalOrderCount;

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 统计销量top10
     * @param begin
     * @param end
     * @return
     */
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        // select od.name, sum(od.number) number from order_datail od, orders o
        // where od.order_id = o.id
        // and o.order_time > ? and o.order_time < ?
        // and o.status = 5
        // group by od.name
        // order by sum(od.number) desc
        // limit 0,10
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);

        List<String> nameList = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numberList = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(numberList, ","))
                .build();
    }

    /**
     * 导出数据
     * @param response
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        // 1. 查询数据库，获取营业数据 -- 查询最近30天的运营数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);

        // 查询总订单数

        // 2. 通过POI将数据写入到Excel文件中
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        // 基于模板文件创建一个新的Excel文件
        try (XSSFWorkbook excel = new XSSFWorkbook(in)) {
            // 获取第一个sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");
            // 填充数据 -- 时间
            sheet.getRow(1).getCell(1).setCellValue("时间： " + dateBegin + "至" + dateEnd);


            // 填充明细数据
            BusinessDataVO total = new BusinessDataVO(0.0, 0, 0.0, 0.0, 0);
            BusinessDataVO tmp = new BusinessDataVO();
            for(int rowNum = 7; rowNum < 37; rowNum++){
                LocalDate date = dateBegin.plusDays(rowNum - 7);
                tmp = workspaceService.getBusinessData(date);
                total.setTurnover(total.getTurnover() + tmp.getTurnover());
                total.setValidOrderCount(total.getValidOrderCount() + tmp.getValidOrderCount());
                total.setOrderCompletionRate(total.getOrderCompletionRate() + tmp.getOrderCompletionRate());
                total.setNewUsers(total.getNewUsers() + tmp.getNewUsers());

                sheet.getRow(rowNum).getCell(1).setCellValue(String.valueOf(date));
                sheet.getRow(rowNum).getCell(2).setCellValue(tmp.getTurnover());
                sheet.getRow(rowNum).getCell(3).setCellValue(tmp.getValidOrderCount());

                sheet.getRow(rowNum).getCell(4).setCellValue(tmp.getOrderCompletionRate());
                sheet.getRow(rowNum).getCell(5).setCellValue(tmp.getUnitPrice());
                sheet.getRow(rowNum).getCell(6).setCellValue(tmp.getNewUsers());
            }
            // 查询总订单数
            LocalDateTime begin = LocalDateTime.of(dateBegin, LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(dateEnd, LocalTime.MAX);
            Integer totalOrderCount = orderMapper.countAllOrders(begin, end);
            total.setOrderCompletionRate(totalOrderCount == 0? 0.0 : total.getValidOrderCount().doubleValue() / totalOrderCount);
            total.setUnitPrice(total.getTurnover() / total.getValidOrderCount());

            // 填充第4行
            sheet.getRow(3).getCell(2).setCellValue(total.getTurnover());
            sheet.getRow(3).getCell(4).setCellValue(total.getOrderCompletionRate());
            sheet.getRow(3).getCell(6).setCellValue(total.getNewUsers());

            // 填充第5行
            sheet.getRow(4).getCell(2).setCellValue(total.getValidOrderCount());
            sheet.getRow(4).getCell(4).setCellValue(total.getUnitPrice());


            // 3. 通过输入流读取Excel文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            // 关闭资源
            out.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
