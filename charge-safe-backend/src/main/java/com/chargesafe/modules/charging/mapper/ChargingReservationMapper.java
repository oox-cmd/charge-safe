package com.chargesafe.modules.charging.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chargesafe.modules.charging.entity.ChargingReservation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChargingReservationMapper extends BaseMapper<ChargingReservation> {
}
