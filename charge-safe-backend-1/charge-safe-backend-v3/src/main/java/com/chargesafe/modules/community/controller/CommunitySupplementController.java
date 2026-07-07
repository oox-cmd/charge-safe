package com.chargesafe.modules.community.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chargesafe.common.Result;
import com.chargesafe.modules.community.entity.Community;
import com.chargesafe.modules.community.mapper.CommunityMapper;
import com.chargesafe.permission.CommunityDataScope;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "社区补充接口")
@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
public class CommunitySupplementController {

    private final CommunityMapper communityMapper;

    @Operation(summary = "获取社区列表（小程序用，返回扁平数组）")
    @GetMapping
    public Result<List<Map<String, Object>>> listAll() {
        List<Community> list = communityMapper.selectList(
                new LambdaQueryWrapper<Community>().eq(Community::getDeleted, 0));
        List<Map<String, Object>> result = list.stream().map(c -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("name", c.getName());
            m.put("address", c.getAddress());
            m.put("contactName", c.getContactName());
            m.put("contactPhone", c.getContactPhone());
            return m;
        }).collect(Collectors.toList());
        return Result.success(result);
    }

    @Operation(summary = "获取全部社区列表（备用）")
    @GetMapping("/list")
    @CommunityDataScope
    public Result<List<Map<String, Object>>> listAllBackup() {
        return listAll();
    }

    @Operation(summary = "按坐标获取最近社区")
    @GetMapping("/nearest")
    public Result<Community> getNearest(@RequestParam(required = false) Double lat,
                                         @RequestParam(required = false) Double lng,
                                         @RequestParam(required = false) Double latitude,
                                         @RequestParam(required = false) Double longitude) {
        double la = lat != null ? lat : latitude;
        double lo = lng != null ? lng : longitude;
        if (la == 0 && lo == 0) {
            return Result.success(null);
        }

        List<Community> communities = communityMapper.selectList(
                new LambdaQueryWrapper<Community>().eq(Community::getStatus, 1));

        if (communities.isEmpty()) {
            return Result.success(null);
        }

        Community nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Community c : communities) {
            if (c.getLongitude() != null && c.getLatitude() != null) {
                double distance = calculateDistance(la, lo, c.getLatitude(), c.getLongitude());
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = c;
                }
            }
        }

        if (nearest == null) {
            nearest = communities.get(0);
        }

        return Result.success(nearest);
    }

    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double a = radLat1 - radLat2;
        double b = Math.toRadians(lng1) - Math.toRadians(lng2);
        double s = 2 * Math.asin(Math.sqrt(
                Math.pow(Math.sin(a / 2), 2) +
                Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        return s * 6371000;
    }
}
