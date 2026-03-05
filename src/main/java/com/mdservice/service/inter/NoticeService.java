package com.mdservice.service.inter;

import com.mdservice.entity.SystemNotice;
import com.mdservice.utils.Result;

public interface NoticeService {
    Result deleteSystemNotice(String id);

    Result modifySystemNotice(SystemNotice systemNotice);

    Result getAllSystemNotice();

    Result getAllSystemToUser(String userId);
}
