package com.shoppi.cloudwave;

public class UserTokenInfo {

    private String userId;
    private String token;

    // 생성자, getter, setter 등 필요한 메서드 추가

    public UserTokenInfo(String userId, String token) {
        this.userId = userId;
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
