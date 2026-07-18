package com.chargesafe.modules.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chargesafe.modules.user.entity.SysDept;

public interface SysDeptService extends IService<SysDept> {

    SysDept create(SysDept dept);

    SysDept update(Long id, SysDept dept);

    void delete(Long id);
}
