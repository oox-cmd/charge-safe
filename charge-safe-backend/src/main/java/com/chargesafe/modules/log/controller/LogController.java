package com.chargesafe.modules.log.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chargesafe.common.OperationLogEntity;
import com.chargesafe.common.OperationLogMapper;
import com.chargesafe.common.PageQuery;
import com.chargesafe.common.PageResult;
import com.chargesafe.common.Result;
import com.chargesafe.security.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Tag(name = "操作日志")
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final OperationLogMapper logMapper;

    @Operation(summary = "分页查询操作日志")
    @RequiresPermission("operation-log:view")
    @GetMapping
    public Result<PageResult<OperationLogEntity>> pageList(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operation,
            PageQuery pageQuery) {

        Page<OperationLogEntity> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        LambdaQueryWrapper<OperationLogEntity> wrapper = new LambdaQueryWrapper<OperationLogEntity>()
                .eq(StringUtils.hasText(username), OperationLogEntity::getUsername, username)
                .eq(StringUtils.hasText(module), OperationLogEntity::getModule, module)
                .like(StringUtils.hasText(operation), OperationLogEntity::getOperation, operation)
                .orderByDesc(OperationLogEntity::getCreatedTime);

        Page<OperationLogEntity> result = logMapper.selectPage(page, wrapper);
        return Result.success(new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords()));
    }

    @Operation(summary = "根据ID查询日志详情")
    @RequiresPermission("operation-log:view")
    @GetMapping("/{id}")
    public Result<OperationLogEntity> getById(@PathVariable Long id) {
        OperationLogEntity log = logMapper.selectById(id);
        return Result.success(log);
    }
}
