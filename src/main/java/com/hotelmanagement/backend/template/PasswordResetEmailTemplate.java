package com.hotelmanagement.backend.template;

import com.hotelmanagement.backend.enums.BookingEmailLocale;
import org.springframework.web.util.HtmlUtils;

public final class PasswordResetEmailTemplate {

    private PasswordResetEmailTemplate() {
    }

    public static String build(String resetUrl) {
        return build(resetUrl, BookingEmailLocale.VI);
    }

    public static String subject(BookingEmailLocale locale) {
        return messages(locale).subject();
    }

    public static String build(String resetUrl, BookingEmailLocale locale) {
        String safeResetUrl = HtmlUtils.htmlEscape(resetUrl, "UTF-8");
        Messages messages = messages(locale);

        return """
                <!doctype html>
                <html lang="%s">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width,initial-scale=1">
                  <style>
                    @media only screen and (max-width:600px) {
                      .email-shell { width:100%% !important; }
                      .content-pad { padding-left:20px !important; padding-right:20px !important; }
                      .reset-button { display:block !important; }
                    }
                  </style>
                </head>
                <body style="margin:0;padding:0;background:#F7F5F0;color:#163B47;font-family:'Segoe UI',Arial,sans-serif;">
                  <div style="display:none;max-height:0;overflow:hidden;opacity:0;color:transparent;">
                    %s
                  </div>
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="width:100%%;background:#F7F5F0;">
                    <tr>
                      <td align="center" style="padding:28px 12px;">
                        <table role="presentation" class="email-shell" width="600" cellspacing="0" cellpadding="0" border="0" style="width:600px;max-width:600px;background:#FFFDFC;border:1px solid #DEDAD2;border-radius:14px;overflow:hidden;">
                          <tr>
                            <td align="center" style="padding:28px 24px 24px;border-bottom:1px solid #DEDAD2;">
                              <div style="font-family:Georgia,'Times New Roman',serif;font-size:28px;line-height:34px;font-weight:600;letter-spacing:.02em;color:#2E90FA;">Diamond Sea</div>
                            </td>
                          </tr>
                          <tr>
                            <td class="content-pad" style="padding:42px 48px 38px;">
                              <div style="font-size:11px;line-height:15px;font-weight:750;letter-spacing:1.8px;color:#2E90FA;">%s</div>
                              <h1 style="margin:12px 0 14px;font-family:Georgia,'Times New Roman',serif;font-size:32px;line-height:40px;font-weight:600;color:#163B47;">%s</h1>
                              <p style="margin:0;font-size:15px;line-height:24px;color:#66716F;">
                                %s
                              </p>

                              <table role="presentation" align="center" cellspacing="0" cellpadding="0" border="0" style="margin:28px auto 26px;">
                                <tr>
                                  <td align="center" bgcolor="#2E90FA" style="border-radius:10px;">
                                    <a class="reset-button" href="%s" target="_blank" style="display:inline-block;padding:14px 26px;color:#FFFFFF;text-decoration:none;font-size:15px;line-height:20px;font-weight:700;border-radius:10px;">
                                      %s
                                    </a>
                                  </td>
                                </tr>
                              </table>

                              <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="width:100%%;background:#F2F8FE;border:1px solid #D8EAFB;border-radius:10px;">
                                <tr>
                                  <td style="padding:16px 18px;font-size:13px;line-height:21px;color:#435B64;">
                                    %s
                                  </td>
                                </tr>
                              </table>

                              <p style="margin:24px 0 0;font-size:13px;line-height:21px;color:#66716F;">
                                %s
                              </p>
                            </td>
                          </tr>
                          <tr>
                            <td align="center" style="padding:25px 28px;background:#0D2936;color:#FFFFFF;">
                              <div style="font-family:Georgia,'Times New Roman',serif;font-size:21px;line-height:27px;font-weight:600;">Diamond Sea</div>
                              <div style="margin-top:9px;font-size:12px;line-height:19px;color:rgba(255,255,255,.68);">
                                %s
                              </div>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """
                .formatted(
                        messages.languageTag(),
                        messages.preheader(),
                        messages.eyebrow(),
                        messages.heading(),
                        messages.introduction(),
                        safeResetUrl,
                        messages.button(),
                        messages.expiry(),
                        messages.ignoreNotice(),
                        messages.footer()
                );
    }

    private static Messages messages(BookingEmailLocale locale) {
        if (locale == BookingEmailLocale.EN) {
            return new Messages(
                    "en", "Reset your Diamond Sea account password",
                    "Complete your Diamond Sea account password reset request.",
                    "ACCOUNT SECURITY", "Reset your password",
                    "We received a request to reset the password for your Diamond Sea account.",
                    "Reset password",
                    "This secure link will expire in <strong style=\"color:#163B47;\">15 minutes</strong>.",
                    "If you did not request a password change, you can ignore this email. Your current password will remain unchanged.",
                    "This is an automated security email. Please do not reply."
            );
        }
        return new Messages(
                "vi", "Đặt lại mật khẩu tài khoản Diamond Sea",
                "Hoàn tất yêu cầu đặt lại mật khẩu tài khoản Diamond Sea của bạn.",
                "BẢO MẬT TÀI KHOẢN", "Đặt lại mật khẩu",
                "Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu cho tài khoản Diamond Sea của bạn.",
                "Đặt lại mật khẩu",
                "Liên kết bảo mật này sẽ hết hạn sau <strong style=\"color:#163B47;\">15 phút</strong>.",
                "Nếu bạn không yêu cầu thay đổi mật khẩu, bạn có thể bỏ qua email này. Mật khẩu hiện tại của bạn vẫn được giữ nguyên.",
                "Đây là email bảo mật tự động. Vui lòng không trả lời email này."
        );
    }

    private record Messages(
            String languageTag,
            String subject,
            String preheader,
            String eyebrow,
            String heading,
            String introduction,
            String button,
            String expiry,
            String ignoreNotice,
            String footer
    ) {
    }
}
