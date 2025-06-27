package com.example.server.batch.mapper;

import com.example.server.dto.DailySalesReport;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class DailySalesReportRowMapper implements RowMapper<DailySalesReport> {

    /**
     *
     * @param rs: result set for JdbcCursorItemReader to read data from DB
     * @param rowNum:
     * @return report: daily sales report
     * @throws SQLException: SQL error in reading data
     */
    @Override
    public DailySalesReport mapRow(ResultSet rs, int rowNum) throws SQLException {
        DailySalesReport report = new DailySalesReport();
        report.setItemName(rs.getString("item_name"));
        report.setTotalQuantity(rs.getInt("total_quantity"));
        report.setTotalRevenue(rs.getBigDecimal("total_revenue"));
        return report;
    }
}
