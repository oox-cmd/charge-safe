package com.chargesafe.modules.charging.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chargesafe.modules.charging.entity.ChargingReservation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ChargingReservationMapper extends BaseMapper<ChargingReservation> {

    @Update("""
            UPDATE charging_reservation
            SET status = #{targetStatus}, cancel_reason = #{cancelReason}, updated_time = CURRENT_TIMESTAMP
            WHERE id = #{reservationId} AND status = #{expectedStatus} AND deleted = 0
            """)
    int transitionStatus(@Param("reservationId") Long reservationId,
                         @Param("expectedStatus") String expectedStatus,
                         @Param("targetStatus") String targetStatus,
                         @Param("cancelReason") String cancelReason);
}
