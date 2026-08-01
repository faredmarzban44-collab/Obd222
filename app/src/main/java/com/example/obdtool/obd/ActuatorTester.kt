package com.example.obdtool.obd

import com.example.obdtool.bluetooth.BluetoothObdManager

/**
 * Actuator tests (turning on a relay, cycling a fuel pump, firing an injector, etc.)
 * are NOT part of standard OBD2 (SAE J1979). They are implemented by each manufacturer
 * through UDS (ISO 14229) service 0x31 "Routine Control", using routine IDs that are
 * specific to that ECU's software. There is no universal PID list for this.
 *
 * This class gives you the plumbing (raw UDS request/response over the ELM327 link)
 * plus a registry where you plug in the routine IDs for your specific vehicle once
 * you have them (from a factory service manual, a tool like a dealer scan tool,
 * or manufacturer documentation). Sending an unknown/incorrect routine ID will
 * simply return a "negative response" from the ECU — it will not do anything unsafe
 * on its own, but you should only run actuator tests with correct, vehicle-matched
 * routine IDs and with the engine OFF unless the procedure explicitly calls for it running.
 */
class ActuatorTester(private val manager: BluetoothObdManager) {

    data class ActuatorRoutine(
        val name: String,
        val routineIdHex: String, // e.g. "0203" - vehicle/manufacturer specific
        val ecuHeader: String? = null // optional 11-bit/29-bit address to target a specific ECU
    )

    /**
     * Example registry — EMPTY by default. Fill this in with routine IDs documented
     * for your specific make/model/ECU. Values below are illustrative placeholders,
     * not real IDs for any vehicle — do not assume they will work on your car.
     */
    val knownRoutines: MutableList<ActuatorRoutine> = mutableListOf()

    suspend fun switchToUdsProtocol(headerHex: String? = null) {
        if (headerHex != null) {
            manager.sendRawCommand("ATH1") // headers on, needed for addressed UDS frames
            manager.sendRawCommand("ATSH$headerHex")
        }
    }

    /**
     * Start a routine (UDS 0x31 0x01 <routineId>).
     * Returns the raw ECU response so you can interpret the positive/negative response code.
     */
    suspend fun startRoutine(routine: ActuatorRoutine): String {
        switchToUdsProtocol(routine.ecuHeader)
        return manager.sendRawCommand("31 01 ${routine.routineIdHex}")
    }

    /** Stop a routine (UDS 0x31 0x02 <routineId>) — always call this after a start test. */
    suspend fun stopRoutine(routine: ActuatorRoutine): String {
        return manager.sendRawCommand("31 02 ${routine.routineIdHex}")
    }

    /** Request routine results (UDS 0x31 0x03 <routineId>). */
    suspend fun getRoutineResult(routine: ActuatorRoutine): String {
        return manager.sendRawCommand("31 03 ${routine.routineIdHex}")
    }
}

/**
 * ---------------------------------------------------------------------------
 * IMPORTANT — about changing fuel/ignition timing/spark parameters:
 * ---------------------------------------------------------------------------
 * This is fundamentally different from reading data or running an actuator test.
 * It means writing into the ECU's live calibration/tune tables, which requires:
 *   1. A "Security Access" (UDS 0x27) seed/key exchange specific to that ECU's
 *      software/firmware version — these algorithms are proprietary and generally
 *      not public.
 *   2. Knowledge of the exact memory addresses / table layout for that ECU,
 *      which comes from reverse engineering or licensed calibration definition
 *      files (e.g. via tools like HP Tuners, EFI Live, Cobb, ETAS INCA).
 *   3. In most markets, modifying emissions-related parameters (fuel maps,
 *      ignition timing) on a road-registered vehicle is also regulated
 *      (e.g. EPA tampering rules in the US).
 *
 * There's no generic Kotlin code that can do this across vehicles — it's not a
 * missing feature, it's how the ECUs are designed (for safety and anti-tampering
 * reasons). If you have a specific vehicle + calibration definitions you're
 * licensed to use, that write logic would plug into BluetoothObdManager.sendRawCommand()
 * the same way ActuatorTester does, using that ECU's documented UDS services.
 */
