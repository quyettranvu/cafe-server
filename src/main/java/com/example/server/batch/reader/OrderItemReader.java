package com.example.server.batch.reader;

import com.example.server.batch.mapper.DailySalesReportRowMapper;
import com.example.server.dto.DailySalesReport;
import com.example.server.utils.SqlUtils;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;

@Component
public class OrderItemReader {

    /**
     * JdbcCursorItemReader<DailySalesReport>: JDBC Item Reader to read data from DB
     * @param dataSource: our database
     * @return ItemReader where read data within step
     */
    @Bean
    public JdbcCursorItemReader<DailySalesReport> dailySalesReportReader(DataSource dataSource) {
        JdbcCursorItemReader<DailySalesReport> result = new JdbcCursorItemReader<>();
        result.setDataSource(dataSource);

        //load Sql
        result.setSql(SqlUtils.loadSql("calculate_total_sales_per_item.sql"));

        // set parameters
        result.setPreparedStatementSetter(new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                LocalDate today = LocalDate.now();
                ps.setTimestamp(1, Timestamp.valueOf(today.atStartOfDay()));
                ps.setTimestamp(2, Timestamp.valueOf(today.plusDays(1).atStartOfDay()));
            }
        });

        // set row mapper to read data
        result.setRowMapper(new DailySalesReportRowMapper());
        return result;
    }
}
