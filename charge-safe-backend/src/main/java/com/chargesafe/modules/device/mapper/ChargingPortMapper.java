package com.chargesafe.modules.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chargesafe.modules.device.entity.ChargingPort;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ChargingPortMapper extends BaseMapper<ChargingPort> {

    @Update("""
            UPDATE charging_port
            SET status = #{targetStatus}, updated_time = CURRENT_TIMESTAMP
            WHERE id = #{portId} AND status = #{expectedStatus} AND deleted = 0
            """)
    int transitionStatus(@Param("portId") Long portId,
                         @Param("expectedStatus") String expectedStatus,
                         @Param("targetStatus") String targetStatus);
}
