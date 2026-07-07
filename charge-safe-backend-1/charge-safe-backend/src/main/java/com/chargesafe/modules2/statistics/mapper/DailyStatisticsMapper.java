package com.chargesafe.modules2.statistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chargesafe.modules2.statistics.entity.DailyStatistics;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DailyStatisticsMapper extends BaseMapper<DailyStatistics> {
}