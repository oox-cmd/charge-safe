package com.chargesafe.modules.charging.exception;

import com.chargesafe.common.BusinessException;
import com.chargesafe.common.ResultCode;

public class ReservationExpiredException extends BusinessException {

    public ReservationExpiredException() {
        super(ResultCode.RESERVATION_EXPIRED);
    }
}
