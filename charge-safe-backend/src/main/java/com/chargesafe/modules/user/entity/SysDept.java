package com.chargesafe.modules.user.entity;

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
@TableName("sys_dept")
public class SysDept {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentId;
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 50, message = "部门名称不能超过50个字符")
    private String name;
    @NotBlank(message = "部门编码不能为空")
    @Size(max = 50, message = "部门编码不能超过50个字符")
    private String code;
    @Size(max = 50, message = "负责人不能超过50个字符")
    private String leader;
    @Size(max = 20, message = "联系电话不能超过20个字符")
    private String phone;
    @Min(value = 0, message = "排序不能小于0")
    @Max(value = 999, message = "排序不能大于999")
    private Integer sort;
    @Min(value = 0, message = "状态只能为0或1")
    @Max(value = 1, message = "状态只能为0或1")
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
