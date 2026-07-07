package com.chargesafe.modules.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chargesafe.modules.device.entity.ChargingDevice;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChargingDeviceMapper extends BaseMapper<ChargingDevice> {
}
