package com.chargesafe.modules.community.controller;

import com.chargesafe.common.PageQuery;
import com.chargesafe.common.PageResult;
import com.chargesafe.common.Result;
import com.chargesafe.modules.community.dto.ParkingAreaDTO;
import com.chargesafe.modules.community.entity.ParkingArea;
import com.chargesafe.modules.community.service.ParkingAreaService;
import com.chargesafe.security.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "车棚管理")
@RestController
@RequestMapping("/api/parking-areas")
@RequiredArgsConstructor
public class ParkingAreaController {

    private final ParkingAreaService parkingAreaService;

    @Operation(summary = "分页查询车棚列表")
    @RequiresPermission("parking-area:view")
    @GetMapping
    public Result<PageResult<ParkingArea>> pageList(
            @RequestParam(required = false) Long communityId,
            PageQuery pageQuery) {
        return Result.success(parkingAreaService.pageList(communityId, pageQuery));
    }

    @Operation(summary = "查询社区下所有车棚")
    @RequiresPermission("parking-area:view")
    @GetMapping("/list")
    public Result<List<ParkingArea>> listByCommunityId(
            @RequestParam(required = false) Long communityId) {
        return Result.success(parkingAreaService.listByCommunityId(communityId));
    }

    @Operation(summary = "根据ID查询车棚")
    @RequiresPermission("parking-area:view")
    @GetMapping("/{id}")
    public Result<ParkingArea> getById(@PathVariable Long id) {
        return Result.success(parkingAreaService.getById(id));
    }

    @Operation(summary = "创建车棚")
    @RequiresPermission("parking-area:view")
    @PostMapping
    public Result<ParkingArea> create(@Valid @RequestBody ParkingAreaDTO dto) {
        return Result.success(parkingAreaService.create(dto));
    }

    @Operation(summary = "更新车棚")
    @RequiresPermission("parking-area:view")
    @PutMapping("/{id}")
    public Result<ParkingArea> update(@PathVariable Long id, @Valid @RequestBody ParkingAreaDTO dto) {
        dto.setId(id);
        return Result.success(parkingAreaService.update(dto));
    }

    @Operation(summary = "删除车棚")
    @RequiresPermission("parking-area:view")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        parkingAreaService.delete(id);
        return Result.success();
    }
}
