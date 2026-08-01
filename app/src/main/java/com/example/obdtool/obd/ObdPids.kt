package com.example.obdtool.obd

/**
 * Standard Mode 01 (current data) PIDs defined by SAE J1979.
 * These are supported by essentially every OBD2-compliant vehicle (2008+ in the US,
 * varies by market elsewhere) and are READ-ONLY — the spec has no "write" equivalent
 * for these values. There is no standard PID to change spark timing or the fuel map;
 * those live inside manufacturer-specific ECU memory and are not exposed over J1979.
 *
 * gaugeMin/gaugeMax define a sensible default scale for dashboard gauges.
 */
enum class ObdPid(
    val pid: String,
    val label: String,
    val shortLabel: String,
    val unit: String,
    val bytesExpected: Int,
    val gaugeMin: Double,
    val gaugeMax: Double
) {
    ENGINE_RPM("010C", "دور موتور (RPM)", "دور موتور", "rpm", 2, 0.0, 7000.0),
    VEHICLE_SPEED("010D", "سرعت خودرو (km/h)", "سرعت", "km/h", 1, 0.0, 220.0),
    COOLANT_TEMP("0105", "دمای آب موتور (°C)", "دمای آب", "°C", 1, -40.0, 120.0),
    INTAKE_AIR_TEMP("010F", "دمای هوای ورودی (°C)", "دمای هوا", "°C", 1, -40.0, 80.0),
    THROTTLE_POS("0111", "موقعیت دریچه گاز (%)", "دریچه گاز", "%", 1, 0.0, 100.0),
    FUEL_LEVEL("012F", "سطح سوخت (%)", "سطح سوخت", "%", 1, 0.0, 100.0),
    ENGINE_LOAD("0104", "بار موتور (%)", "بار موتور", "%", 1, 0.0, 100.0),
    TIMING_ADVANCE("010E", "آوانس جرقه فعلی (درجه)", "آوانس جرقه", "°", 1, -20.0, 60.0), // READ ONLY
    FUEL_PRESSURE("010A", "فشار سوخت (kPa)", "فشار سوخت", "kPa", 1, 0.0, 765.0),
    O2_VOLTAGE_B1S1("0114", "ولتاژ سنسور اکسیژن بانک1 سنسور1 (V)", "اکسیژن B1S1", "V", 2, 0.0, 1.3),
    INTAKE_MAP("010B", "فشار منیفولد ورودی / بوست (kPa)", "بوست", "kPa", 1, 0.0, 255.0),
    BATTERY_VOLTAGE("0142", "ولتاژ باتری/برق خودرو (V)", "ولتاژ باتری", "V", 2, 8.0, 16.0),
    SHORT_FUEL_TRIM_B1("0106", "تریم سوخت کوتاه‌مدت بانک1 (%)", "تریم کوتاه", "%", 1, -100.0, 100.0),
    LONG_FUEL_TRIM_B1("0107", "تریم سوخت بلندمدت بانک1 (%)", "تریم بلند", "%", 1, -100.0, 100.0),
    FUEL_RATE("015E", "نرخ مصرف سوخت (L/h)", "مصرف سوخت", "L/h", 2, 0.0, 40.0);

    fun requestCommand(): String = pid
}

/**
 * Parses the raw hex response string returned by the ELM327 for a given PID
 * into a human-readable value. Response format after stripping spaces/headers
 * looks like: "41 0C 1A F8" -> mode(41) pid(0C) data bytes...
 */
object ObdResponseParser {

    fun parse(pid: ObdPid, rawResponse: String): Double? {
        val hex = rawResponse.replace("\\s".toRegex(), "").uppercase()
        // Expect response to start with 41 + pid (mode 01 response = 0x41)
        val marker = "41" + pid.pid.substring(2)
        val idx = hex.indexOf(marker)
        if (idx == -1) return null

        val dataStart = idx + marker.length
        val dataHex = hex.substring(dataStart)
        if (dataHex.length < pid.bytesExpected * 2) return null

        val a = dataHex.substring(0, 2).toIntOrNull(16) ?: return null
        val b = if (pid.bytesExpected >= 2 && dataHex.length >= 4)
            dataHex.substring(2, 4).toIntOrNull(16) else null

        return when (pid) {
            ObdPid.ENGINE_RPM -> ((a * 256) + (b ?: 0)) / 4.0
            ObdPid.VEHICLE_SPEED -> a.toDouble()
            ObdPid.COOLANT_TEMP -> a - 40.0
            ObdPid.INTAKE_AIR_TEMP -> a - 40.0
            ObdPid.THROTTLE_POS -> (a * 100.0) / 255.0
            ObdPid.FUEL_LEVEL -> (a * 100.0) / 255.0
            ObdPid.ENGINE_LOAD -> (a * 100.0) / 255.0
            ObdPid.TIMING_ADVANCE -> (a / 2.0) - 64.0
            ObdPid.FUEL_PRESSURE -> a * 3.0
            ObdPid.O2_VOLTAGE_B1S1 -> a / 200.0
            ObdPid.INTAKE_MAP -> a.toDouble()
            ObdPid.BATTERY_VOLTAGE -> ((a * 256) + (b ?: 0)) / 1000.0
            ObdPid.SHORT_FUEL_TRIM_B1 -> ((a - 128) * 100.0) / 128.0
            ObdPid.LONG_FUEL_TRIM_B1 -> ((a - 128) * 100.0) / 128.0
            ObdPid.FUEL_RATE -> ((a * 256) + (b ?: 0)) / 20.0
        }
    }
}
