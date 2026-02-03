package com.hrai.org.dto;

/**
 * 登录响应 DTO
 */
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserInfo userInfo;

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public Long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }

    public UserInfo getUserInfo() { return userInfo; }
    public void setUserInfo(UserInfo userInfo) { this.userInfo = userInfo; }

    public static LoginResponseBuilder builder() {
        return new LoginResponseBuilder();
    }

    public static class LoginResponseBuilder {
        private final LoginResponse response = new LoginResponse();

        public LoginResponseBuilder accessToken(String accessToken) {
            response.accessToken = accessToken;
            return this;
        }

        public LoginResponseBuilder refreshToken(String refreshToken) {
            response.refreshToken = refreshToken;
            return this;
        }

        public LoginResponseBuilder tokenType(String tokenType) {
            response.tokenType = tokenType;
            return this;
        }

        public LoginResponseBuilder expiresIn(Long expiresIn) {
            response.expiresIn = expiresIn;
            return this;
        }

        public LoginResponseBuilder userInfo(UserInfo userInfo) {
            response.userInfo = userInfo;
            return this;
        }

        public LoginResponse build() {
            return response;
        }
    }

    /**
     * 用户信息
     */
    public static class UserInfo {
        private Long userId;
        private String username;
        private String realName;
        private String email;
        private String phone;
        private String avatar;
        private String tenantId;
        private String roleCode;
        private String roleName;
        private String planType;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getRealName() { return realName; }
        public void setRealName(String realName) { this.realName = realName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }

        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }

        public String getRoleCode() { return roleCode; }
        public void setRoleCode(String roleCode) { this.roleCode = roleCode; }

        public String getRoleName() { return roleName; }
        public void setRoleName(String roleName) { this.roleName = roleName; }

        public String getPlanType() { return planType; }
        public void setPlanType(String planType) { this.planType = planType; }

        public static UserInfoBuilder builder() {
            return new UserInfoBuilder();
        }

        public static class UserInfoBuilder {
            private final UserInfo info = new UserInfo();

            public UserInfoBuilder userId(Long userId) { info.userId = userId; return this; }
            public UserInfoBuilder username(String username) { info.username = username; return this; }
            public UserInfoBuilder realName(String realName) { info.realName = realName; return this; }
            public UserInfoBuilder email(String email) { info.email = email; return this; }
            public UserInfoBuilder phone(String phone) { info.phone = phone; return this; }
            public UserInfoBuilder avatar(String avatar) { info.avatar = avatar; return this; }
            public UserInfoBuilder tenantId(String tenantId) { info.tenantId = tenantId; return this; }
            public UserInfoBuilder roleCode(String roleCode) { info.roleCode = roleCode; return this; }
            public UserInfoBuilder roleName(String roleName) { info.roleName = roleName; return this; }
            public UserInfoBuilder planType(String planType) { info.planType = planType; return this; }

            public UserInfo build() { return info; }
        }
    }
}
