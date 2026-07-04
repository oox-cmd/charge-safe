package com.chargesafe.modules.user.service;

import com.chargesafe.common.PageQuery;
import com.chargesafe.common.PageResult;
import com.chargesafe.modules.user.dto.*;
import com.chargesafe.modules.user.vo.UserVO;

public interface UserService {

    PageResult<UserVO> pageList(UserQueryDTO query, PageQuery pageQuery);

    UserVO create(UserCreateDTO dto);

    UserVO update(Long id, UserUpdateDTO dto);

    void delete(Long id);

    void updateStatus(Long id, Integer status);

    void assignRoles(Long id, AssignRoleDTO dto);

    void resetPassword(Long id, String password);
}
