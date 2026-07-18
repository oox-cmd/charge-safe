package com.chargesafe.modules.notification.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chargesafe.modules.notification.entity.AlarmRecord;
import com.chargesafe.modules.notification.mapper.AlarmRecordMapper;
import com.chargesafe.modules.notification.service.AlarmRecordService;
import org.springframework.stereotype.Service;

@Service
public class AlarmRecordServiceImpl extends ServiceImpl<AlarmRecordMapper, AlarmRecord> implements AlarmRecordService {
}
