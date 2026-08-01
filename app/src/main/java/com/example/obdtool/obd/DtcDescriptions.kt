package com.example.obdtool.obd

/**
 * Persian descriptions for the most common GENERIC (SAE-standard) OBD2 trouble codes.
 * These P0xxx/P2xxx/U0xxx codes mean the same thing on every OBD2-compliant vehicle,
 * regardless of manufacturer — so they're accurate for a Bosch M7.4.4 ECU the same
 * way they are for any other EOBD-compliant car.
 *
 * Manufacturer-specific codes (the ranges reserved as P1xxx on many brands, or any
 * code not in this table) are NOT standardized — their meaning is defined by Bosch/
 * the vehicle maker internally and isn't public. For those, this returns a generic
 * fallback message rather than guessing.
 */
object DtcDescriptions {

    private val descriptions: Map<String, String> = mapOf(
        // --- Fuel & Air Metering ---
        "P0100" to "مشکل در مدار جریان‌سنج هوا (MAF)",
        "P0101" to "عملکرد نامناسب جریان‌سنج هوا (MAF)",
        "P0102" to "سیگنال ورودی جریان‌سنج هوا ضعیف",
        "P0103" to "سیگنال ورودی جریان‌سنج هوا زیاد",
        "P0110" to "مشکل در مدار سنسور دمای هوای ورودی",
        "P0115" to "مشکل در مدار سنسور دمای آب موتور",
        "P0116" to "عملکرد نامناسب سنسور دمای آب موتور",
        "P0120" to "مشکل در مدار سنسور موقعیت دریچه گاز",
        "P0125" to "دمای آب موتور برای کنترل حلقه‌بسته سوخت به حد کافی نرسیده",
        "P0130" to "مشکل در مدار سنسور اکسیژن (بانک۱، سنسور۱)",
        "P0131" to "ولتاژ سنسور اکسیژن پایین (بانک۱، سنسور۱)",
        "P0132" to "ولتاژ سنسور اکسیژن بالا (بانک۱، سنسور۱)",
        "P0133" to "پاسخ کند سنسور اکسیژن (بانک۱، سنسور۱)",
        "P0134" to "عدم فعالیت سنسور اکسیژن (بانک۱، سنسور۱)",
        "P0135" to "مشکل در گرم‌کن سنسور اکسیژن (بانک۱، سنسور۱)",
        "P0170" to "خطای ترکیب سوخت-هوا (بانک۱)",
        "P0171" to "ترکیب سوخت رقیق (بانک۱)",
        "P0172" to "ترکیب سوخت غنی (بانک۱)",
        // --- Ignition & Misfire ---
        "P0200" to "مشکل در مدار انژکتور",
        "P0201" to "مشکل در مدار انژکتور سیلندر ۱",
        "P0202" to "مشکل در مدار انژکتور سیلندر ۲",
        "P0203" to "مشکل در مدار انژکتور سیلندر ۳",
        "P0204" to "مشکل در مدار انژکتور سیلندر ۴",
        "P0300" to "میس‌فایر تصادفی/چندسیلندری تشخیص داده شد",
        "P0301" to "میس‌فایر در سیلندر ۱",
        "P0302" to "میس‌فایر در سیلندر ۲",
        "P0303" to "میس‌فایر در سیلندر ۳",
        "P0304" to "میس‌فایر در سیلندر ۴",
        "P0325" to "مشکل در مدار سنسور ضربه (Knock Sensor)",
        "P0335" to "مشکل در مدار سنسور موقعیت میل‌لنگ",
        "P0340" to "مشکل در مدار سنسور موقعیت میل‌سوپاپ",
        // --- Emissions ---
        "P0400" to "خطا در جریان بازگردانی گاز اگزوز (EGR)",
        "P0401" to "جریان ناکافی EGR",
        "P0402" to "جریان بیش‌ازحد EGR",
        "P0420" to "کارایی سیستم کاتالیزور پایین‌تر از حد استاندارد (بانک۱)",
        "P0430" to "کارایی سیستم کاتالیزور پایین‌تر از حد استاندارد (بانک۲)",
        "P0440" to "خطا در سیستم کنترل بخارات سوخت (EVAP)",
        "P0441" to "جریان نامناسب تخلیه بخار در سیستم EVAP",
        "P0442" to "نشتی کوچک در سیستم EVAP",
        "P0446" to "مشکل در مدار کنترل ونت سیستم EVAP",
        "P0455" to "نشتی بزرگ در سیستم EVAP",
        // --- Speed, Idle & Auxiliary ---
        "P0500" to "مشکل در مدار سنسور سرعت خودرو",
        "P0505" to "مشکل در سیستم کنترل دور آرام (Idle)",
        "P0506" to "دور آرام پایین‌تر از حد مورد انتظار",
        "P0507" to "دور آرام بالاتر از حد مورد انتظار",
        // --- Computer & Auxiliary Outputs ---
        "P0600" to "خطا در باس ارتباطی سریال (Serial Communication)",
        "P0601" to "خطای حافظه داخلی واحد کنترل موتور (ECU)",
        "P0602" to "برنامه‌ریزی نشدن واحد کنترل موتور",
        "P0603" to "خطای حافظه KAM در واحد کنترل موتور",
        "P0605" to "خطای حافظه ROM واحد کنترل موتور",
        // --- Transmission ---
        "P0700" to "خطا در سیستم کنترل گیربکس (رجوع به کدهای گیربکس)",
        "P0715" to "مشکل در مدار سنسور سرعت ورودی گیربکس",
        "P0720" to "مشکل در مدار سنسور سرعت خروجی گیربکس",
        "P0740" to "مشکل در سیستم کلاچ کانورتور گشتاور",
        // --- Network / communication ---
        "U0100" to "از دست رفتن ارتباط با واحد کنترل موتور/گیربکس",
        "U0101" to "از دست رفتن ارتباط با واحد کنترل گیربکس",
        "U0155" to "از دست رفتن ارتباط با مدول کلاستر آلات",
    )

    /**
     * Returns a Persian description for a standard code, or a safe fallback
     * message (not a guess) if the code isn't in the generic SAE table.
     */
    fun describe(code: String): String {
        val normalized = code.trim().uppercase()
        return descriptions[normalized]
            ?: "کد استاندارد شناخته‌شده نیست — ممکن است کد اختصاصی سازنده (Bosch) باشد؛ برای معنی دقیق به کتاب سرویس مراجعه کنید."
    }
}
