package com.chargesafe.modules.charging.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chargesafe.modules.charging.entity.ChargingOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChargingOrderMapper extends BaseMapper<ChargingOrder> {
}
