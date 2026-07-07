package com.chargesafe.modules.community.controller;

import com.chargesafe.common.OperationLog;
import com.chargesafe.common.PageQuery;
import com.chargesafe.common.PageResult;
import com.chargesafe.common.Result;
import com.chargesafe.modules.community.dto.CommunityDTO;
import com.chargesafe.modules.community.entity.Community;
import com.chargesafe.modules.community.service.CommunityService;
import com.chargesafe.security.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "社区管理")
@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @Operation(summary = "分页查询社区列表")
    @RequiresPermission("community:list:view")
    @GetMapping
    public Result<PageResult<Community>> pageList(PageQuery pageQuery) {
        return Result.success(communityService.pageList(pageQuery));
    }

    @Operation(summary = "查询所有启用的社区")
    @RequiresPermission("community:list:view")
    @GetMapping("/list")
    public Result<List<Community>> listAll() {
        return Result.success(communityService.listAll());
    }

    @Operation(summary = "根据ID查询社区")
    @RequiresPermission("community:list:view")
    @GetMapping("/{id}")
    public Result<Community> getById(@PathVariable Long id) {
        return Result.success(communityService.getById(id));
    }

    @Operation(summary = "创建社区")
    @OperationLog(module = "社区管理", description = "创建社区", type = "CREATE")
    @RequiresPermission("community:list:view")
    @PostMapping
    public Result<Community> create(@Valid @RequestBody CommunityDTO dto) {
        return Result.success(communityService.create(dto));
    }

    @Operation(summary = "更新社区")
    @OperationLog(module = "社区管理", description = "更新社区", type = "UPDATE")
    @RequiresPermission("community:list:view")
    @PutMapping("/{id}")
    public Result<Community> update(@PathVariable Long id, @Valid @RequestBody CommunityDTO dto) {
        dto.setId(id);
        return Result.success(communityService.update(dto));
    }

    @Operation(summary = "启用/停用社区")
    @OperationLog(module = "社区管理", description = "启用/停用社区", type = "UPDATE")
    @RequiresPermission("community:list:view")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        communityService.updateStatus(id, status);
        return Result.success();
    }
}
