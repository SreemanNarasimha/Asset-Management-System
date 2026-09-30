package com.mams.security;

import com.mams.exception.BusinessException;
import com.mams.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class BaseAccessService {

    public Long resolveBaseId(Long requested, AuthUser user) {
        if ("ADMIN".equals(user.getRoleName())) {
            return requested;
        }
        if (requested == null) {
            return user.getBaseId();
        }
        if (!Objects.equals(requested, user.getBaseId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Access denied to requested base");
        }
        return requested;
    }

    public void assertCanAccess(Long baseId, AuthUser user) {
        if ("ADMIN".equals(user.getRoleName())) {
            return;
        }
        if (!Objects.equals(baseId, user.getBaseId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Access denied to requested base");
        }
    }
}
