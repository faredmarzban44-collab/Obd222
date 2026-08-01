package com.example.obdtool.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

/**
 * OBD-II link protocols the ELM327 can be told to use (AT SP command).
 * Most 2008+ vehicles use ISO 15765-4 (CAN). Older ECUs — including Bosch
 * M7.4.4 / ME7.4.4 as used on Peugeot 405/Pars and Iran Khodro Samand —
 * speak ISO 14230-4 (KWP2000) over K-Line instead. If AUTO picks the wrong
 * one (no response, or garbage data), pick KWP_FAST or KWP_SLOW explicitly.
 */
enum class ObdProtocol(val atCommand: String, val label: String) {
    AUTO("ATSP0", "تشخیص خودکار"),
    ISO9141("ATSP3", "ISO 9141-2 (K-Line قدیمی)"),
    KWP_SLOW("ATSP4", "ISO 14230-4 KWP (5-baud init) — مناسب M7.4.4"),
    KWP_FAST("ATSP5", "ISO 14230-4 KWP (fast init) — مناسب M7.4.4"),
    CAN_11BIT_500K("ATSP6", "ISO 15765-4 CAN (11bit/500k)"),
    CAN_29BIT_500K("ATSP7", "ISO 15765-4 CAN (29bit/500k)");
}

/**
 * Manages a Classic Bluetooth (SPP) connection to an ELM327-compatible OBD2 dongle.
 *
 * Most cheap OBD2 dongles emulate a Serial Port Profile (SPP) device and accept
 * plain-text AT commands (ELM327 command set) followed by \r.
 */
class BluetoothObdManager {

    companion object {
        // Standard SPP UUID used by virtually all ELM327 clones
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    private var socket: BluetoothSocket? = null
    private var input: InputStream? = null
    private var output: OutputStream? = null

    val isConnected: Boolean
        get() = socket?.isConnected == true

    /**
     * Connect to the given paired device. The device must already be paired
     * with the phone via Android Bluetooth settings.
     */
    @SuppressLint("MissingPermission") // Caller is responsible for runtime permission checks
    suspend fun connect(
        device: BluetoothDevice,
        protocol: ObdProtocol = ObdProtocol.AUTO
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            BluetoothAdapter.getDefaultAdapter()?.cancelDiscovery()
            val sock = device.createRfcommSocketToServiceRecord(SPP_UUID)
            sock.connect()
            socket = sock
            input = sock.inputStream
            output = sock.outputStream

            // Initialize the ELM327 chip to a known state
            initializeElm(protocol)
            Result.success(Unit)
        } catch (e: IOException) {
            disconnect()
            Result.failure(e)
        }
    }

    private suspend fun initializeElm(protocol: ObdProtocol) {
        sendRawCommand("ATZ")   // reset
        sendRawCommand("ATE0")  // echo off
        sendRawCommand("ATL0")  // linefeeds off
        sendRawCommand("ATS0")  // spaces off
        sendRawCommand("ATH0")  // headers off (turn on if you need per-ECU addressing)
        sendRawCommand(protocol.atCommand)
    }

    /**
     * Switch protocol without reconnecting. Useful if auto-detect (ATSP0) picks the
     * wrong protocol for older ECUs like Bosch M7.4.4 / ME7.4.4, which speak
     * ISO 14230-4 (KWP2000) over K-Line rather than modern CAN.
     */
    suspend fun setProtocol(protocol: ObdProtocol) {
        sendRawCommand(protocol.atCommand)
    }

    /**
     * Send a raw AT or OBD command and return the trimmed response.
     * Commands are terminated with \r as required by the ELM327 spec.
     */
    suspend fun sendRawCommand(command: String): String = withContext(Dispatchers.IO) {
        val out = output ?: throw IOException("Not connected")
        val inp = input ?: throw IOException("Not connected")

        out.write((command + "\r").toByteArray())
        out.flush()

        val buffer = ByteArray(1024)
        val sb = StringBuilder()
        while (true) {
            val bytes = inp.read(buffer)
            if (bytes <= 0) break
            val chunk = String(buffer, 0, bytes)
            sb.append(chunk)
            // ELM327 signals end of response with '>'
            if (chunk.contains('>')) break
        }
        sb.toString().replace(">", "").trim()
    }

    fun disconnect() {
        try {
            input?.close()
            output?.close()
            socket?.close()
        } catch (_: IOException) {
            // ignore
        } finally {
            input = null
            output = null
            socket = null
        }
    }
}
