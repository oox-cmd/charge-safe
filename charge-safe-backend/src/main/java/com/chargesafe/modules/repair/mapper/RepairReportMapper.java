package com.chargesafe.modules.repair.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chargesafe.modules.repair.entity.RepairReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RepairReportMapper extends BaseMapper<RepairReport> {

    @Update("UPDATE repair_report SET status = #{targetStatus} "
            + "WHERE id = #{reportId} AND status IN ('PENDING', 'REJECTED')")
    int transitionToDispatched(@Param("reportId") Long reportId,
                               @Param("targetStatus") String targetStatus);
}
