package com.chargesafe.modules.auth.service;

import com.chargesafe.modules.auth.dto.LoginDTO;
import com.chargesafe.modules.auth.vo.LoginVO;

public interface AuthService {

    /**
     * 用户登录
     */
    LoginVO login(LoginDTO dto);

    /**
     * 获取当前登录用户信息
     */
    LoginVO getCurrentUser();
}
