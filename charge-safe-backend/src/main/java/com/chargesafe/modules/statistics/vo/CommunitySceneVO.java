package com.chargesafe.modules.statistics.vo;

import lombok.Data;

/**
 * 三级大屏一级场景使用的社区定位数据。
 *
 * <p>仅输出场景渲染需要的业务 ID、名称和已登记的经纬度，避免向前端暴露联系人等社区管理字段。</p>
 */
@Data
public class CommunitySceneVO {

    private Long id;
    private String name;
    private Double lat;
    private Double lng;
}
