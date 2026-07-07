package com.chargesafe.modules.charging.scheduler;

import com.chargesafe.modules.charging.entity.ChargingReservation;
import com.chargesafe.modules.charging.enums.ReservationStatus;
import com.chargesafe.modules.charging.mapper.ChargingReservationMapper;
import com.chargesafe.modules.charging.service.ChargingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约过期自动取消定时任务
 *
 * <p>每分钟扫描处于 PENDING 且已过过期时间的预约，自动取消并恢复端口为空闲状态。
 * 复用 {@link ChargingService#expireReservation(Long)} 中的业务（端口状态恢复 + 通知）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationExpireScheduler {

    private final ChargingReservationMapper reservationMapper;
    private final ChargingService chargingService;

    @Scheduled(cron = "0 * * * * ?")
    public void expireOverdueReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<ChargingReservation> overdue = reservationMapper.selectList(
                new LambdaQueryWrapper<ChargingReservation>()
                        .eq(ChargingReservation::getStatus, ReservationStatus.PENDING.getCode())
                        .lt(ChargingReservation::getExpireTime, now)
        );

        if (overdue.isEmpty()) {
            return;
        }

        log.info("扫描到 {} 条过期预约，开始自动取消", overdue.size());
        for (ChargingReservation reservation : overdue) {
            try {
                chargingService.expireReservation(reservation.getId());
            } catch (Exception e) {
                log.error("自动取消预约 {} 失败", reservation.getId(), e);
            }
        }
    }
}
