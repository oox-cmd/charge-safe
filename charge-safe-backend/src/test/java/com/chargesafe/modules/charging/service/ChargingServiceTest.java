package com.chargesafe.modules.charging.service;

import com.chargesafe.common.BusinessException;
import com.chargesafe.common.PageQuery;
import com.chargesafe.common.PageResult;
import com.chargesafe.modules.charging.dto.ReservationDTO;
import com.chargesafe.modules.charging.entity.ChargingOrder;
import com.chargesafe.modules.charging.entity.ChargingReservation;
import com.chargesafe.modules.charging.enums.OrderStatus;
import com.chargesafe.modules.charging.enums.ReservationStatus;
import com.chargesafe.modules.charging.mapper.ChargingOrderMapper;
import com.chargesafe.modules.charging.mapper.ChargingReservationMapper;
import com.chargesafe.modules.charging.service.impl.ChargingServiceImpl;
import com.chargesafe.modules.community.entity.ParkingArea;
import com.chargesafe.modules.community.mapper.ParkingAreaMapper;
import com.chargesafe.modules.device.entity.ChargingDevice;
import com.chargesafe.modules.device.entity.ChargingPort;
import com.chargesafe.modules.device.enums.PortStatus;
import com.chargesafe.modules.device.mapper.ChargingDeviceMapper;
import com.chargesafe.modules.device.mapper.ChargingPortMapper;
import com.chargesafe.modules.notification.entity.Notification;
import com.chargesafe.modules.notification.mapper.NotificationMapper;
import com.chargesafe.modules.user.mapper.SysUserMapper;
import com.chargesafe.security.LoginUser;
import com.chargesafe.security.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockMakers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class ChargingServiceTest {

    private ChargingReservationMapper reservationMapper;
    private ChargingOrderMapper orderMapper;
    private ChargingPortMapper portMapper;
    private ChargingDeviceMapper deviceMapper;
    private ParkingAreaMapper parkingAreaMapper;
    private NotificationMapper notificationMapper;
    private SysUserMapper userMapper;

    private ChargingServiceImpl chargingService;

    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_PORT_ID = 10L;
    private static final Long TEST_DEVICE_ID = 100L;
    private static final Long TEST_PARKING_AREA_ID = 1000L;
    private static final Long TEST_COMMUNITY_ID = 10000L;
    private static final Long TEST_RESERVATION_ID = 100L;
    private static final Long TEST_ORDER_ID = 200L;

    @BeforeEach
    void setUp() {
        reservationMapper = mock(ChargingReservationMapper.class, withSettings().mockMaker(MockMakers.PROXY));
        orderMapper = mock(ChargingOrderMapper.class, withSettings().mockMaker(MockMakers.PROXY));
        portMapper = mock(ChargingPortMapper.class, withSettings().mockMaker(MockMakers.PROXY));
        deviceMapper = mock(ChargingDeviceMapper.class, withSettings().mockMaker(MockMakers.PROXY));
        parkingAreaMapper = mock(ParkingAreaMapper.class, withSettings().mockMaker(MockMakers.PROXY));
        notificationMapper = mock(NotificationMapper.class, withSettings().mockMaker(MockMakers.PROXY));
        userMapper = mock(SysUserMapper.class, withSettings().mockMaker(MockMakers.PROXY));
        chargingService = new ChargingServiceImpl(
                reservationMapper,
                orderMapper,
                portMapper,
                deviceMapper,
                parkingAreaMapper,
                notificationMapper,
                userMapper);
    }

    @AfterEach
    void tearDown() {
        UserContext.remove();
    }

    // ==================== createReservation 测试 ====================

    @Test
    void createReservation_shouldSucceed_whenPortIdleAndNoActiveReservation() {
        // Given: 端口空闲，用户无有效预约，端口无有效预约
        mockUserContext(TEST_USER_ID);
        when(reservationMapper.selectCount(any())).thenReturn(0L);
        when(portMapper.selectById(TEST_PORT_ID)).thenReturn(createPort(PortStatus.IDLE));
        when(deviceMapper.selectById(anyLong())).thenReturn(createDevice());
        when(parkingAreaMapper.selectById(anyLong())).thenReturn(createParkingArea());
        when(reservationMapper.insert(any())).thenReturn(1);
        when(portMapper.updateById(any())).thenReturn(1);
        when(notificationMapper.insert(any())).thenReturn(1);

        ReservationDTO dto = new ReservationDTO();
        dto.setPortId(TEST_PORT_ID);

        // When
        ChargingReservation result = chargingService.createReservation(dto);

        // Then: 预约创建成功，端口状态变为 RESERVED
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.PENDING.getCode());
        verify(portMapper).updateById(argThat(port ->
                PortStatus.RESERVED.getCode().equals(port.getStatus())
        ));
    }

    @Test
    void createReservation_shouldFail_whenUserHasActiveReservation() {
        // Given: 用户已有有效预约
        mockUserContext(TEST_USER_ID);
        when(reservationMapper.selectCount(any()))
                .thenReturn(1L)  // 用户有效预约计数
                .thenReturn(0L); // 端口有效预约计数

        ReservationDTO dto = new ReservationDTO();
        dto.setPortId(TEST_PORT_ID);

        // When & Then
        assertThatThrownBy(() -> chargingService.createReservation(dto))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo(6001));
    }

    @Test
    void createReservation_shouldFail_whenPortHasActiveReservation() {
        // Given: 端口已有有效预约
        mockUserContext(TEST_USER_ID);
        when(reservationMapper.selectCount(any()))
                .thenReturn(0L)  // 用户有效预约计数
                .thenReturn(1L); // 端口有效预约计数

        ReservationDTO dto = new ReservationDTO();
        dto.setPortId(TEST_PORT_ID);

        // When & Then
        assertThatThrownBy(() -> chargingService.createReservation(dto))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo(6002));
    }

    @Test
    void createReservation_shouldFail_whenPortNotIdle() {
        // Given: 端口非空闲状态
        mockUserContext(TEST_USER_ID);
        when(reservationMapper.selectCount(any())).thenReturn(0L);
        when(portMapper.selectById(TEST_PORT_ID)).thenReturn(createPort(PortStatus.CHARGING));

        ReservationDTO dto = new ReservationDTO();
        dto.setPortId(TEST_PORT_ID);

        // When & Then
        assertThatThrownBy(() -> chargingService.createReservation(dto))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo(6002));
    }

    // ==================== cancelReservation 测试 ====================

    @Test
    void cancelReservation_shouldSucceed_whenReservationPending() {
        // Given: 预约存在且为 PENDING 状态
        mockUserContext(TEST_USER_ID);
        ChargingReservation reservation = createReservation(ReservationStatus.PENDING);
        when(reservationMapper.selectById(TEST_RESERVATION_ID)).thenReturn(reservation);
        when(reservationMapper.updateById(any())).thenReturn(1);
        when(portMapper.selectById(TEST_PORT_ID)).thenReturn(createPort(PortStatus.RESERVED));
        when(portMapper.updateById(any())).thenReturn(1);
        when(notificationMapper.insert(any())).thenReturn(1);

        // When
        chargingService.cancelReservation(TEST_RESERVATION_ID);

        // Then: 预约状态变为 CANCELLED，端口恢复 IDLE
        verify(reservationMapper).updateById(argThat(r ->
                ReservationStatus.CANCELLED.getCode().equals(r.getStatus())
        ));
        verify(portMapper).updateById(argThat(port ->
                PortStatus.IDLE.getCode().equals(port.getStatus())
        ));
    }

    @Test
    void cancelReservation_shouldFail_whenReservationNotPending() {
        // Given: 预约已转订单
        mockUserContext(TEST_USER_ID);
        ChargingReservation reservation = createReservation(ReservationStatus.CONVERTED);
        when(reservationMapper.selectById(TEST_RESERVATION_ID)).thenReturn(reservation);

        // When & Then
        assertThatThrownBy(() -> chargingService.cancelReservation(TEST_RESERVATION_ID))
                .isInstanceOf(BusinessException.class);
    }

    // ==================== expireReservation 测试 ====================

    @Test
    void expireReservation_shouldSucceed_whenReservationPending() {
        // Given
        ChargingReservation reservation = createReservation(ReservationStatus.PENDING);
        when(reservationMapper.selectById(TEST_RESERVATION_ID)).thenReturn(reservation);
        when(reservationMapper.updateById(any())).thenReturn(1);
        when(portMapper.selectById(TEST_PORT_ID)).thenReturn(createPort(PortStatus.RESERVED));
        when(portMapper.updateById(any())).thenReturn(1);
        when(notificationMapper.insert(any())).thenReturn(1);

        // When
        chargingService.expireReservation(TEST_RESERVATION_ID);

        // Then: 预约状态变为 EXPIRED，端口恢复 IDLE
        verify(reservationMapper).updateById(argThat(r ->
                ReservationStatus.EXPIRED.getCode().equals(r.getStatus())
        ));
        verify(portMapper).updateById(argThat(port ->
                PortStatus.IDLE.getCode().equals(port.getStatus())
        ));
    }

    @Test
    void expireReservation_shouldDoNothing_whenReservationNotPending() {
        // Given: 预约已处理
        ChargingReservation reservation = createReservation(ReservationStatus.CANCELLED);
        when(reservationMapper.selectById(TEST_RESERVATION_ID)).thenReturn(reservation);

        // When
        chargingService.expireReservation(TEST_RESERVATION_ID);

        // Then: 不更新预约和端口
        verify(reservationMapper, never()).updateById(any());
        verify(portMapper, never()).updateById(any());
    }

    // ==================== faultCancelReservation 测试 ====================

    @Test
    void faultCancelReservation_shouldSucceed_whenReservationPending() {
        // Given
        ChargingReservation reservation = createReservation(ReservationStatus.PENDING);
        when(reservationMapper.selectById(TEST_RESERVATION_ID)).thenReturn(reservation);
        when(reservationMapper.updateById(any())).thenReturn(1);
        when(portMapper.selectById(TEST_PORT_ID)).thenReturn(createPort(PortStatus.RESERVED));
        when(portMapper.updateById(any())).thenReturn(1);
        when(notificationMapper.insert(any())).thenReturn(1);

        // When
        chargingService.faultCancelReservation(TEST_RESERVATION_ID, "充电器故障");

        // Then: 预约状态变为 FAULT_CANCELLED，端口变为 FAULT
        verify(reservationMapper).updateById(argThat(r ->
                ReservationStatus.FAULT_CANCELLED.getCode().equals(r.getStatus())
        ));
        verify(portMapper).updateById(argThat(port ->
                PortStatus.FAULT.getCode().equals(port.getStatus())
        ));
    }

    // ==================== startCharging 测试 ====================

    @Test
    void startCharging_shouldSucceed_whenReservationValid() {
        // Given: 预约有效，端口 RESERVED
        mockUserContext(TEST_USER_ID);
        ChargingReservation reservation = createReservation(ReservationStatus.PENDING);
        reservation.setExpireTime(LocalDateTime.now().plusMinutes(30));
        when(reservationMapper.selectById(TEST_RESERVATION_ID)).thenReturn(reservation);
        when(portMapper.selectById(TEST_PORT_ID)).thenReturn(createPort(PortStatus.RESERVED));
        when(reservationMapper.updateById(any())).thenReturn(1);
        when(portMapper.updateById(any())).thenReturn(1);
        when(orderMapper.insert(any())).thenReturn(1);

        // When
        ChargingOrder order = chargingService.startCharging(TEST_RESERVATION_ID);

        // Then: 预约变为 CONVERTED，端口变为 CHARGING，订单创建
        assertThat(order).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CHARGING.getCode());
        verify(reservationMapper).updateById(argThat(r ->
                ReservationStatus.CONVERTED.getCode().equals(r.getStatus())
        ));
        verify(portMapper).updateById(argThat(port ->
                PortStatus.CHARGING.getCode().equals(port.getStatus())
        ));
    }

    @Test
    void startCharging_shouldFail_whenReservationExpired() {
        // Given: 预约已过期
        mockUserContext(TEST_USER_ID);
        ChargingReservation reservation = createReservation(ReservationStatus.PENDING);
        reservation.setExpireTime(LocalDateTime.now().minusMinutes(1));
        when(reservationMapper.selectById(TEST_RESERVATION_ID)).thenReturn(reservation);
        when(reservationMapper.updateById(any())).thenReturn(1);

        // When & Then
        assertThatThrownBy(() -> chargingService.startCharging(TEST_RESERVATION_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo(6003));
    }

    // ==================== startChargingDirectly 测试 ====================

    @Test
    void startChargingDirectly_shouldSucceed_whenPortIdle() {
        // Given: 端口空闲
        mockUserContext(TEST_USER_ID);
        when(portMapper.selectById(TEST_PORT_ID)).thenReturn(createPort(PortStatus.IDLE));
        when(deviceMapper.selectById(anyLong())).thenReturn(createDevice());
        when(parkingAreaMapper.selectById(anyLong())).thenReturn(createParkingArea());
        when(portMapper.updateById(any())).thenReturn(1);
        when(orderMapper.insert(any())).thenReturn(1);

        // When
        ChargingOrder order = chargingService.startChargingDirectly(TEST_PORT_ID);

        // Then: 端口变为 CHARGING，订单创建
        assertThat(order).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CHARGING.getCode());
        verify(portMapper).updateById(argThat(port ->
                PortStatus.CHARGING.getCode().equals(port.getStatus())
        ));
    }

    @Test
    void startChargingDirectly_shouldFail_whenPortNotIdle() {
        // Given: 端口非空闲
        mockUserContext(TEST_USER_ID);
        when(portMapper.selectById(TEST_PORT_ID)).thenReturn(createPort(PortStatus.CHARGING));

        // When & Then
        assertThatThrownBy(() -> chargingService.startChargingDirectly(TEST_PORT_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo(6002));
    }

    // ==================== finishCharging 测试 ====================

    @Test
    void finishCharging_shouldSucceed_whenOrderCharging() {
        // Given: 订单状态为 CHARGING
        mockUserContext(TEST_USER_ID);
        ChargingOrder order = createOrder(OrderStatus.CHARGING);
        order.setStartTime(LocalDateTime.now().minusMinutes(60));
        when(orderMapper.selectById(TEST_ORDER_ID)).thenReturn(order);
        when(orderMapper.updateById(any())).thenReturn(1);
        when(portMapper.selectById(TEST_PORT_ID)).thenReturn(createPort(PortStatus.CHARGING));
        when(portMapper.updateById(any())).thenReturn(1);
        when(notificationMapper.insert(any())).thenReturn(1);

        // When
        ChargingOrder result = chargingService.finishCharging(TEST_ORDER_ID);

        // Then: 订单变为 COMPLETED，端口恢复 IDLE
        assertThat(result.getStatus()).isEqualTo(OrderStatus.COMPLETED.getCode());
        verify(portMapper).updateById(argThat(port ->
                PortStatus.IDLE.getCode().equals(port.getStatus())
        ));
    }

    @Test
    void finishCharging_shouldFail_whenOrderNotCharging() {
        // Given: 订单已结束
        mockUserContext(TEST_USER_ID);
        ChargingOrder order = createOrder(OrderStatus.COMPLETED);
        when(orderMapper.selectById(TEST_ORDER_ID)).thenReturn(order);

        // When & Then
        assertThatThrownBy(() -> chargingService.finishCharging(TEST_ORDER_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode()).isEqualTo(6011));
    }

    // ==================== 辅助方法 ====================

    private void mockUserContext(Long userId) {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(userId);
        loginUser.setUsername("testuser");
        loginUser.setRoleCode("RESIDENT");
        UserContext.setUser(loginUser);
    }

    private ChargingPort createPort(PortStatus status) {
        ChargingPort port = new ChargingPort();
        port.setId(TEST_PORT_ID);
        port.setDeviceId(TEST_DEVICE_ID);
        port.setPortNumber(1);
        port.setPortName("1号端口");
        port.setStatus(status.getCode());
        return port;
    }

    private ChargingDevice createDevice() {
        ChargingDevice device = new ChargingDevice();
        device.setId(TEST_DEVICE_ID);
        device.setParkingAreaId(TEST_PARKING_AREA_ID);
        return device;
    }

    private ParkingArea createParkingArea() {
        ParkingArea area = new ParkingArea();
        area.setId(TEST_PARKING_AREA_ID);
        area.setCommunityId(TEST_COMMUNITY_ID);
        return area;
    }

    private ChargingReservation createReservation(ReservationStatus status) {
        ChargingReservation reservation = new ChargingReservation();
        reservation.setId(TEST_RESERVATION_ID);
        reservation.setUserId(TEST_USER_ID);
        reservation.setPortId(TEST_PORT_ID);
        reservation.setCommunityId(TEST_COMMUNITY_ID);
        reservation.setStatus(status.getCode());
        reservation.setReserveTime(LocalDateTime.now().minusMinutes(10));
        reservation.setExpireTime(LocalDateTime.now().plusMinutes(20));
        return reservation;
    }

    private ChargingOrder createOrder(OrderStatus status) {
        ChargingOrder order = new ChargingOrder();
        order.setId(TEST_ORDER_ID);
        order.setUserId(TEST_USER_ID);
        order.setPortId(TEST_PORT_ID);
        order.setReservationId(TEST_RESERVATION_ID);
        order.setCommunityId(TEST_COMMUNITY_ID);
        order.setStartTime(LocalDateTime.now().minusMinutes(30));
        order.setStatus(status.getCode());
        return order;
    }
}
