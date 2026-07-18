package com.chargesafe.modules.device.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@TableName("device_category")
public class DeviceCategory {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称不能超过50个字符")
    private String name;
    @NotBlank(message = "分类编码不能为空")
    @Size(max = 50, message = "分类编码不能超过50个字符")
    private String code;
    @NotBlank(message = "适用功率不能为空")
    @Size(max = 20, message = "适用功率不能超过20个字符")
    private String power;
    @Size(max = 500, message = "描述不能超过500个字符")
    private String description;
    @Min(value = 0, message = "状态只能为0或1")
    @Max(value = 1, message = "状态只能为0或1")
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
