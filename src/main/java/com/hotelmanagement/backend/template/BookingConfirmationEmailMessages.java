package com.hotelmanagement.backend.template;

import com.hotelmanagement.backend.enums.BookingEmailLocale;
import com.hotelmanagement.backend.enums.BookingStatus;
import com.hotelmanagement.backend.enums.PaymentMethod;
import com.hotelmanagement.backend.enums.PaymentStatus;

record BookingConfirmationEmailMessages(
        String languageTag,
        String subject,
        String preheader,
        String confirmed,
        String introduction,
        String bookingCode,
        String room,
        String stayDetails,
        String checkIn,
        String checkOut,
        String nights,
        String guestDetails,
        String name,
        String email,
        String phone,
        String services,
        String quantity,
        String priceSummary,
        String roomSubtotal,
        String servicesSubtotal,
        String otherCharges,
        String discount,
        String total,
        String payment,
        String paymentMethod,
        String paymentStatus,
        String amountPaid,
        String bookingStatus,
        String viewBooking,
        String automatedMessage
) {
    static BookingConfirmationEmailMessages forLocale(BookingEmailLocale locale) {
        return locale == BookingEmailLocale.EN ? english() : vietnamese();
    }

    String paymentMethod(PaymentMethod method) {
        if (method == null) {
            return null;
        }
        return switch (method) {
            case CASH -> languageTag.equals("en") ? "Cash" : "Tiền mặt";
            case CREDIT_CARD -> languageTag.equals("en") ? "Credit card" : "Thẻ tín dụng";
            case BANK_TRANSFER -> languageTag.equals("en") ? "Bank transfer" : "Chuyển khoản ngân hàng";
            case E_WALLET -> languageTag.equals("en") ? "E-wallet" : "Ví điện tử";
        };
    }

    String paymentStatus(PaymentStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case SUCCESS -> languageTag.equals("en") ? "Successful" : "Thành công";
            case PENDING -> languageTag.equals("en") ? "Pending" : "Đang chờ";
            case FAILED -> languageTag.equals("en") ? "Failed" : "Thất bại";
        };
    }

    String bookingStatus(BookingStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case PENDING -> languageTag.equals("en") ? "Pending" : "Đang chờ";
            case CONFIRMED -> languageTag.equals("en") ? "Confirmed" : "Đã xác nhận";
            case CHECKED_IN -> languageTag.equals("en") ? "Checked in" : "Đã nhận phòng";
            case CHECKED_OUT -> languageTag.equals("en") ? "Checked out" : "Đã trả phòng";
            case CANCELLED -> languageTag.equals("en") ? "Cancelled" : "Đã hủy";
            case NO_SHOW -> languageTag.equals("en") ? "No-show" : "Không đến";
        };
    }

    private static BookingConfirmationEmailMessages vietnamese() {
        return new BookingConfirmationEmailMessages(
                "vi",
                "Xác nhận đặt phòng • Diamond Sea • %s",
                "Đặt phòng %s của bạn tại Diamond Sea đã được xác nhận.",
                "Đặt phòng thành công",
                "Kỳ nghỉ của bạn tại Diamond Sea đã được xác nhận.",
                "Mã đặt phòng",
                "Hạng phòng",
                "Thông tin lưu trú",
                "Nhận phòng",
                "Trả phòng",
                "Số đêm",
                "Thông tin khách",
                "Họ và tên",
                "Email",
                "Điện thoại",
                "Dịch vụ bổ sung",
                "Số lượng",
                "Chi tiết chi phí",
                "Tiền phòng",
                "Dịch vụ",
                "Chi phí khác",
                "Ưu đãi",
                "Tổng cộng",
                "Thông tin thanh toán",
                "Phương thức",
                "Trạng thái thanh toán",
                "Đã thanh toán",
                "Trạng thái đặt phòng",
                "Xem đặt phòng",
                "Đây là email xác nhận đặt phòng tự động. Vui lòng không trả lời email này."
        );
    }

    private static BookingConfirmationEmailMessages english() {
        return new BookingConfirmationEmailMessages(
                "en",
                "Booking confirmed • Diamond Sea • %s",
                "Your Diamond Sea booking %s is confirmed.",
                "Booking confirmed",
                "Your stay at Diamond Sea is confirmed.",
                "Booking code",
                "Your room",
                "Stay details",
                "Check-in",
                "Check-out",
                "Nights",
                "Guest details",
                "Name",
                "Email",
                "Phone",
                "Additional services",
                "Quantity",
                "Price summary",
                "Room subtotal",
                "Services",
                "Other charges",
                "Discount",
                "Total",
                "Payment details",
                "Method",
                "Payment status",
                "Amount paid",
                "Booking status",
                "View booking",
                "This is an automated booking confirmation email. Please do not reply to this message."
        );
    }
}
