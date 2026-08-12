package com.hotelmanagement.backend.template;

import com.hotelmanagement.backend.dto.internal.BookingConfirmationEmailData;
import org.springframework.web.util.HtmlUtils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class BookingConfirmationEmailTemplate {
    private BookingConfirmationEmailTemplate() {
    }

    public static RenderedEmail render(BookingConfirmationEmailData data) {
        BookingConfirmationEmailMessages messages =
                BookingConfirmationEmailMessages.forLocale(data.getLocale());
        Locale locale = Locale.forLanguageTag(messages.languageTag());
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(
                messages.languageTag().equals("en") ? "dd MMM yyyy" : "dd/MM/yyyy",
                locale
        );

        String bookingCode = escape(data.getBookingCode());
        StringBuilder html = new StringBuilder(12_000);
        html.append("""
                <!doctype html>
                <html lang="%s">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width,initial-scale=1">
                  <style>
                    @media only screen and (max-width: 640px) {
                      .email-shell { width: 100%% !important; }
                      .content-pad { padding-left: 20px !important; padding-right: 20px !important; }
                      .detail-label, .detail-value { display: block !important; width: 100%% !important; text-align: left !important; }
                      .detail-value { padding-top: 4px !important; }
                      .mobile-stack { display: block !important; width: 100%% !important; }
                    }
                  </style>
                </head>
                <body style="margin:0;padding:0;background:#F7F5F0;color:#163B47;font-family:'Segoe UI',Arial,sans-serif;">
                  <div style="display:none;max-height:0;overflow:hidden;opacity:0;color:transparent;">%s</div>
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="width:100%%;background:#F7F5F0;">
                    <tr><td align="center" style="padding:28px 12px;">
                      <table role="presentation" class="email-shell" width="640" cellspacing="0" cellpadding="0" border="0" style="width:640px;max-width:640px;background:#FFFDFC;border:1px solid #DEDAD2;border-radius:14px;overflow:hidden;">
                        <tr><td align="center" style="padding:30px 24px;">
                          <div style="font-family:Georgia,'Times New Roman',serif;font-size:28px;line-height:34px;font-weight:600;letter-spacing:.02em;color:#2E90FA;">Diamond Sea</div>
                        </td></tr>
                        <tr><td class="content-pad" align="center" style="padding:34px 44px 42px;background:#FCFBF8;border-top:1px solid #E7E7E2;">
                          <h1 style="margin:0 0 10px;font-family:Georgia,'Times New Roman',serif;font-size:34px;line-height:42px;font-weight:600;color:#163B47;">%s</h1>
                          <p style="margin:0;max-width:440px;font-size:15px;line-height:25px;color:#66716F;">%s</p>
                          <div style="margin-top:28px;padding:17px 24px;background:#FFFDFC;border:1px solid #DEDAD2;border-radius:10px;">
                            <div style="font-size:12px;line-height:17px;font-weight:650;color:#66716F;">%s</div>
                            <div style="margin-top:5px;font-size:22px;line-height:29px;font-weight:750;letter-spacing:.03em;color:#163B47;">%s</div>
                          </div>
                        </td></tr>
                """.formatted(
                messages.languageTag(),
                escape(messages.preheader().formatted(data.getBookingCode())),
                escape(messages.confirmed()),
                escape(messages.introduction()),
                escape(messages.bookingCode()),
                bookingCode
        ));

        appendRoomSection(html, data, messages);
        appendSectionStart(html, messages.stayDetails());
        appendDetailRow(html, messages.checkIn(), escape(data.getCheckInDate().format(dateFormatter)));
        appendDetailRow(html, messages.checkOut(), escape(data.getCheckOutDate().format(dateFormatter)));
        appendDetailRow(html, messages.nights(), Long.toString(data.getNumberOfNights()));
        appendSectionEnd(html);

        appendSectionStart(html, messages.guestDetails());
        appendDetailRow(html, messages.name(), escape(data.getGuestName()));
        appendOptionalDetailRow(html, messages.email(), data.getRecipientEmail());
        appendOptionalDetailRow(html, messages.phone(), data.getGuestPhone());
        appendSectionEnd(html);

        if (data.getServices() != null && !data.getServices().isEmpty()) {
            appendServices(html, data, messages, locale);
        }

        appendPriceSummary(html, data, messages, locale);
        appendPaymentDetails(html, data, messages, locale);

        if (data.getBookingDetailUrl() != null && !data.getBookingDetailUrl().isBlank()) {
            html.append("""
                        <tr><td class="content-pad" align="center" style="padding:8px 44px 38px;">
                          <a href="%s" target="_blank" style="display:inline-block;background:#2E90FA;color:#FFFFFF;text-decoration:none;font-size:15px;line-height:20px;font-weight:700;padding:14px 28px;border-radius:10px;">%s</a>
                        </td></tr>
                    """.formatted(escape(data.getBookingDetailUrl()), escape(messages.viewBooking())));
        }

        html.append("""
                        <tr><td align="center" style="padding:26px 28px;background:#0D2936;color:#FFFFFF;">
                          <div style="font-family:Georgia,'Times New Roman',serif;font-size:22px;line-height:28px;font-weight:600;">Diamond Sea</div>
                          <div style="margin-top:10px;font-size:12px;line-height:19px;color:rgba(255,255,255,.68);">%s</div>
                        </td></tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(escape(messages.automatedMessage())));

        return new RenderedEmail(
                messages.subject().formatted(data.getBookingCode()),
                html.toString()
        );
    }

    private static void appendRoomSection(
            StringBuilder html,
            BookingConfirmationEmailData data,
            BookingConfirmationEmailMessages messages
    ) {
        if (data.getRoomImageUrl() != null && !data.getRoomImageUrl().isBlank()) {
            html.append("""
                        <tr><td style="padding:0 0 0;">
                          <img src="%s" width="640" alt="%s" style="display:block;width:100%%;max-width:640px;height:auto;border:0;">
                        </td></tr>
                    """.formatted(escape(data.getRoomImageUrl()), escape(data.getRoomImageAlt())));
        }
        html.append("""
                    <tr><td class="content-pad" style="padding:32px 44px 36px;">
                      <div style="font-size:12px;line-height:17px;font-weight:700;color:#2E90FA;">%s</div>
                      <div style="margin-top:8px;font-family:Georgia,'Times New Roman',serif;font-size:27px;line-height:35px;color:#163B47;">%s</div>
                      %s
                    </td></tr>
                """.formatted(
                escape(messages.room()),
                escape(data.getRoomTypeName()),
                data.getRoomName() == null || data.getRoomName().isBlank()
                        ? ""
                        : "<div style=\"margin-top:5px;font-size:13px;line-height:20px;color:#66716F;\">"
                        + escape(data.getRoomName()) + "</div>"
        ));
    }

    private static void appendSectionStart(StringBuilder html, String title) {
        html.append("""
                    <tr><td class="content-pad" style="padding:32px 44px;border-top:1px solid #E7E7E2;">
                      <div style="margin-bottom:15px;font-family:Georgia,'Times New Roman',serif;font-size:21px;line-height:28px;font-weight:600;color:#163B47;">%s</div>
                      <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="width:100%%;">
                """.formatted(escape(title)));
    }

    private static void appendSectionEnd(StringBuilder html) {
        html.append("</table></td></tr>");
    }

    private static void appendDetailRow(StringBuilder html, String label, String value) {
        html.append("""
                <tr>
                  <td class="detail-label" width="46%%" style="padding:8px 0;font-size:13px;line-height:20px;color:#66716F;">%s</td>
                  <td class="detail-value" width="54%%" align="right" style="padding:8px 0;font-size:14px;line-height:20px;font-weight:650;color:#163B47;">%s</td>
                </tr>
                """.formatted(escape(label), value));
    }

    private static void appendOptionalDetailRow(StringBuilder html, String label, String value) {
        if (value != null && !value.isBlank()) {
            appendDetailRow(html, label, escape(value));
        }
    }

    private static void appendServices(
            StringBuilder html,
            BookingConfirmationEmailData data,
            BookingConfirmationEmailMessages messages,
            Locale locale
    ) {
        appendSectionStart(html, messages.services());
        for (BookingConfirmationEmailData.ServiceLine service : data.getServices()) {
            String name = service.getName();
            if (service.getQuantity() > 1) {
                name += " × " + service.getQuantity();
            }
            appendDetailRow(html, name, formatMoney(service.getAmount(), locale));
        }
        appendSectionEnd(html);
    }

    private static void appendPriceSummary(
            StringBuilder html,
            BookingConfirmationEmailData data,
            BookingConfirmationEmailMessages messages,
            Locale locale
    ) {
        appendSectionStart(html, messages.priceSummary());
        appendDetailRow(html, messages.roomSubtotal(), formatMoney(data.getRoomSubtotal(), locale));
        if (isPositive(data.getServicesSubtotal())) {
            appendDetailRow(html, messages.servicesSubtotal(), formatMoney(data.getServicesSubtotal(), locale));
        }
        if (isPositive(data.getOtherChargesSubtotal())) {
            appendDetailRow(html, messages.otherCharges(), formatMoney(data.getOtherChargesSubtotal(), locale));
        }
        if (isPositive(data.getDiscountAmount())) {
            String label = messages.discount();
            if (data.getPromotionName() != null && !data.getPromotionName().isBlank()) {
                label += " · " + data.getPromotionName();
            }
            appendDetailRow(html, label, "− " + formatMoney(data.getDiscountAmount(), locale));
        }
        html.append("<tr><td colspan=\"2\" style=\"padding-top:12px;border-top:1px solid #DEDAD2;\"></td></tr>");
        html.append("""
                <tr>
                  <td style="padding:7px 0 0;font-size:14px;line-height:21px;font-weight:750;color:#163B47;">%s</td>
                  <td align="right" style="padding:7px 0 0;font-size:22px;line-height:28px;font-weight:750;color:#163B47;white-space:nowrap;">%s</td>
                </tr>
                """.formatted(escape(messages.total()), formatMoney(data.getTotalAmount(), locale)));
        appendSectionEnd(html);
    }

    private static void appendPaymentDetails(
            StringBuilder html,
            BookingConfirmationEmailData data,
            BookingConfirmationEmailMessages messages,
            Locale locale
    ) {
        appendSectionStart(html, messages.payment());
        String paymentMethod = messages.paymentMethod(data.getPaymentMethod());
        String paymentStatus = messages.paymentStatus(data.getPaymentStatus());
        String bookingStatus = messages.bookingStatus(data.getBookingStatus());
        if (paymentMethod != null) {
            appendDetailRow(html, messages.paymentMethod(), escape(paymentMethod));
        }
        if (paymentStatus != null) {
            appendDetailRow(html, messages.paymentStatus(), escape(paymentStatus));
        }
        appendDetailRow(html, messages.amountPaid(), formatMoney(data.getAmountPaid(), locale));
        if (bookingStatus != null) {
            appendDetailRow(html, messages.bookingStatus(), escape(bookingStatus));
        }
        appendSectionEnd(html);
    }

    private static boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    private static String formatMoney(BigDecimal amount, Locale locale) {
        NumberFormat formatter = NumberFormat.getIntegerInstance(locale);
        return escape(formatter.format(amount == null ? BigDecimal.ZERO : amount)) + " VND";
    }

    private static String escape(String value) {
        return HtmlUtils.htmlEscape(value == null ? "" : value, "UTF-8");
    }

    public record RenderedEmail(String subject, String html) {
    }
}
