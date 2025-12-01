package com.dongyang.mock.model.DAO;

import com.dongyang.mock.model.DTO.StockPriceDailyDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

public class StockPriceDailyDAO {

    public int[] insertPriceDailyBatch(List<StockPriceDailyDTO> dataList, Connection conn) throws SQLException {
        if (dataList == null || dataList.isEmpty()) {
            return new int[0];
        }

        // 복합 PK (stock_id, price_date)를 사용하여 ON DUPLICATE KEY UPDATE를 적용합니다.
        final String sql = "INSERT INTO stock_price_daily (" +
                "stock_id, price_date, open_price, high_price, low_price, close_price, volume, amount, " +
                "prev_close, change_price, change_rate) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE" +
                "    open_price = VALUES(open_price)," +
                "    high_price = VALUES(high_price)," +
                "    low_price = VALUES(low_price)," +
                "    close_price = VALUES(close_price)," +
                "    volume = VALUES(volume)," +
                "    amount = VALUES(amount)," +
                "    prev_close = VALUES(prev_close)," +
                "    change_price = VALUES(change_price)," +
                "    change_rate = VALUES(change_rate)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (StockPriceDailyDTO data : dataList) {

                int index = 1;

                // 1. stock_id (BIGINT NOT NULL / 복합 PK & FK)
                pstmt.setLong(index++, data.getStockId());

                // 2. price_date (DATE NOT NULL / 복합 PK)
                pstmt.setDate(index++, data.getPriceDate());

                // 3. open_price (DECIMAL NOT NULL)
                pstmt.setBigDecimal(index++, data.getOpenPrice());

                // 4. high_price (DECIMAL NOT NULL)
                pstmt.setBigDecimal(index++, data.getHighPrice());

                // 5. low_price (DECIMAL NOT NULL)
                pstmt.setBigDecimal(index++, data.getLowPrice());

                // 6. close_price (DECIMAL NOT NULL)
                pstmt.setBigDecimal(index++, data.getClosePrice());

                // 7. volume (BIGINT NOT NULL)
                pstmt.setLong(index++, data.getVolume());

                // 8. amount (BIGINT NULL)
                if (data.getAmount() != null) {
                    pstmt.setLong(index++, data.getAmount());
                } else {
                    pstmt.setNull(index++, Types.BIGINT);
                }

                // 9. prev_close (DECIMAL NULL)
                if (data.getPrevClose() != null) {
                    pstmt.setBigDecimal(index++, data.getPrevClose());
                } else {
                    pstmt.setNull(index++, Types.DECIMAL);
                }

                // 10. change_price (DECIMAL NULL)
                if (data.getChangePrice() != null) {
                    pstmt.setBigDecimal(index++, data.getChangePrice());
                } else {
                    pstmt.setNull(index++, Types.DECIMAL);
                }

                // 11. change_rate (DECIMAL NULL)
                if (data.getChangeRate() != null) {
                    pstmt.setBigDecimal(index++, data.getChangeRate());
                } else {
                    pstmt.setNull(index++, Types.DECIMAL);
                }

                pstmt.addBatch();
            }

            return pstmt.executeBatch();

        } catch (SQLException e) {
            System.err.println("일별 주가 데이터 배치 저장/갱신 실패: " + e.getMessage());
            throw e;
        }
    }
}