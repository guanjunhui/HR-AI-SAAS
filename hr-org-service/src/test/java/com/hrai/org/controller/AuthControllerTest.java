package com.hrai.org.controller;

import com.hrai.common.dto.Result;
import com.hrai.common.exception.BizException;
import com.hrai.org.dto.LoginResponse;
import com.hrai.org.service.AuthService;
import com.hrai.org.util.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthController authController;

    @Test
    void getCurrentUser_fallbackToTokenClaimsWhenHeadersMissing() {
        String token = "test.token.value";
        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .userId(1L)
                .username("admin")
                .tenantId("tenant_default")
                .build();

        when(jwtUtils.validateToken(eq(token))).thenReturn(true);
        when(jwtUtils.getUserId(eq(token))).thenReturn(1L);
        when(jwtUtils.getTenantId(eq(token))).thenReturn("tenant_default");
        when(authService.getCurrentUser(eq(1L), eq("tenant_default"))).thenReturn(userInfo);

        Result<LoginResponse.UserInfo> result = authController.getCurrentUser(
                null,
                null,
                "Bearer " + token
        );

        assertEquals(200, result.getCode());
        assertSame(userInfo, result.getData());
        verify(authService).getCurrentUser(1L, "tenant_default");
    }

    @Test
    void getCurrentUser_throw401WhenNoHeaderAndNoToken() {
        BizException ex = assertThrows(BizException.class, () ->
                authController.getCurrentUser(null, null, null));
        assertEquals(401, ex.getCode());
    }
}
