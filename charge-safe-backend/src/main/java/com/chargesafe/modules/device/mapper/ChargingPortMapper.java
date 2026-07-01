package com.chargesafe.modules.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chargesafe.modules.device.entity.ChargingPort;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChargingPortMapper extends BaseMapper<ChargingPort> {
}
