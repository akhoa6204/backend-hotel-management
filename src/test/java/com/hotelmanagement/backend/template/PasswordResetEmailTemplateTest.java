package com.hotelmanagement.backend.template;

import com.hotelmanagement.backend.enums.BookingEmailLocale;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordResetEmailTemplateTest {

    @Test
    void rendersDiamondSeaClientBrandAndEscapesResetUrl() {
        String html = PasswordResetEmailTemplate.build(
                "https://diamondsea.example/reset-password?token=123456&next=\"unsafe\""
        );

        assertTrue(html.contains("Diamond Sea"));
        assertTrue(html.contains("#2E90FA"));
        assertTrue(html.contains("#F7F5F0"));
        assertTrue(html.contains("#0D2936"));
        assertTrue(html.contains("123456&amp;next=&quot;unsafe&quot;"));
        assertTrue(html.contains("15 phút"));
        assertFalse(html.contains("Skyline"));
        assertFalse(html.contains("DiamondSea Hotel"));
    }

    @Test
    void rendersEnglishContentAndSubjectFromLocale() {
        String html = PasswordResetEmailTemplate.build(
                "https://diamondsea.example/reset-password?token=123456",
                BookingEmailLocale.EN
        );

        assertTrue(html.contains("<html lang=\"en\">"));
        assertTrue(html.contains("Reset your password"));
        assertTrue(html.contains("15 minutes"));
        assertTrue(html.contains("margin:28px auto 26px"));
        assertTrue(html.contains("align=\"center\""));
        assertTrue(PasswordResetEmailTemplate.subject(BookingEmailLocale.EN).contains("Diamond Sea"));
        assertFalse(PasswordResetEmailTemplate.subject(BookingEmailLocale.EN).contains("Skyline"));
        assertFalse(html.contains("DA NANG"));
    }
}
