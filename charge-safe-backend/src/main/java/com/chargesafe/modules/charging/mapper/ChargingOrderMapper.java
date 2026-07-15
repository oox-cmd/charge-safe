package com.chargesafe.modules.charging.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chargesafe.modules.charging.entity.ChargingOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Mapper
public interface ChargingOrderMapper extends BaseMapper<ChargingOrder> {

    @Update("""
            UPDATE charging_order
            SET end_time = #{endTime}, duration_minutes = #{durationMinutes}, amount = #{amount},
                status = #{targetStatus}, updated_time = CURRENT_TIMESTAMP
            WHERE id = #{orderId} AND status = #{expectedStatus} AND deleted = 0
            """)
    int finishOrder(@Param("orderId") Long orderId,
                    @Param("expectedStatus") String expectedStatus,
                    @Param("targetStatus") String targetStatus,
                    @Param("endTime") LocalDateTime endTime,
                    @Param("durationMinutes") Integer durationMinutes,
                    @Param("amount") BigDecimal amount);
}
