package com.chargesafe.modules.device.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chargesafe.modules.device.entity.DeviceCategory;

public interface DeviceCategoryService extends IService<DeviceCategory> {

    DeviceCategory create(DeviceCategory category);

    DeviceCategory update(Long id, DeviceCategory category);

    void delete(Long id);
}
