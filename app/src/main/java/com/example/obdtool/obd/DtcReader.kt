package com.example.obdtool.obd

import com.example.obdtool.bluetooth.BluetoothObdManager

/** A DTC code paired with its Persian description. */
data class DtcResult(val code: String, val descriptionFa: String)

/**
 * Reads (Mode 03) and clears (Mode 04) stored Diagnostic Trouble Codes.
 * This part of the OBD2 spec IS standardized and safe to expose generically.
 */
class DtcReader(private val manager: BluetoothObdManager) {

    suspend fun readStoredCodes(): List<DtcResult> {
        val response = manager.sendRawCommand("03")
        return parseDtcResponse(response)
    }

    suspend fun readPendingCodes(): List<DtcResult> {
        val response = manager.sendRawCommand("07")
        return parseDtcResponse(response)
    }

    /** Clears stored codes AND turns off the check-engine light / resets readiness monitors. */
    suspend fun clearCodes(): Boolean {
        val response = manager.sendRawCommand("04")
        return !response.contains("ERROR", ignoreCase = true)
    }

    private fun parseDtcResponse(raw: String): List<DtcResult> {
        val hex = raw.replace("\\s".toRegex(), "").uppercase()
        // Strip leading mode/response byte (43 for mode 03, 47 for mode 07) if present
        val body = when {
            hex.startsWith("43") -> hex.substring(2)
            hex.startsWith("47") -> hex.substring(2)
            else -> hex
        }

        val results = mutableListOf<DtcResult>()
        var i = 0
        while (i + 4 <= body.length) {
            val chunk = body.substring(i, i + 4)
            if (chunk != "0000") {
                val code = decodeDtc(chunk)
                results.add(DtcResult(code, DtcDescriptions.describe(code)))
            }
            i += 4
        }
        return results
    }

    private fun decodeDtc(fourHexChars: String): String {
        val firstByte = fourHexChars.substring(0, 2).toInt(16)
        val category = when ((firstByte shr 6) and 0x03) {
            0 -> "P" // Powertrain
            1 -> "C" // Chassis
            2 -> "B" // Body
            else -> "U" // Network
        }
        val firstDigit = (firstByte shr 4) and 0x03
        val secondDigit = firstByte and 0x0F
        val rest = fourHexChars.substring(2)
        return "$category$firstDigit$secondDigit$rest"
    }
}
