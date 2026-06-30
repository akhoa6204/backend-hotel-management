package com.hotelmanagement.backend.template;

public class PasswordResetEmailTemplate {

    private PasswordResetEmailTemplate() {
    }

    public static String build(String resetUrl) {
        return """
                <div style="background-color:#f5f5f5;padding:24px 0;font-family:system-ui,-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;">
                  <div style="max-width:520px;margin:0 auto;background-color:#ffffff;border-radius:16px;box-shadow:0 4px 16px rgba(0,0,0,0.06);overflow:hidden;">
                    <div style="padding:24px 32px 8px 32px;text-align:center;">
                      <div style="font-size:24px;font-weight:700;color:#2E90FA;margin-bottom:4px;">DiamondSea Hotel</div>
                      <div style="font-size:18px;font-weight:600;color:#333;">Đặt lại mật khẩu</div>
                    </div>

                    <div style="padding:8px 32px 24px 32px;font-size:14px;color:#333;line-height:1.6;">
                      <p>Chào bạn,</p>

                      <p>
                        Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản DiamondSea Hotel của bạn.
                      </p>

                      <a href="%s"
                         target="_blank"
                         style="display:block;text-align:center;background:#2E90FA;color:white!important;text-decoration:none;padding:12px 16px;border-radius:8px;font-weight:600;">
                         Đặt lại mật khẩu
                      </a>

                      <p>
                        Liên kết này sẽ hết hạn trong <strong>15 phút</strong>.
                      </p>

                      <p>
                        Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.
                      </p>
                    </div>
                  </div>
                </div>
                """
                .formatted(resetUrl);
    }
}