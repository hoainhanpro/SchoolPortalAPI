package com.ptithcm.portal.service;

public interface AuthService {
    /**
     * Thay đổi mật khẩu cho người dùng
     * 
     * @param username Tên đăng nhập của người dùng
     * @param currentPassword Mật khẩu hiện tại
     * @param newPassword Mật khẩu mới
     * @return true nếu thay đổi thành công, false nếu thất bại
     * @throws Exception nếu có lỗi xảy ra trong quá trình xác thực hoặc cập nhật
     */
    boolean changePassword(String username, String currentPassword, String newPassword) throws Exception;
    boolean verifyPassword(String username, String currentPassword) throws Exception;
} 