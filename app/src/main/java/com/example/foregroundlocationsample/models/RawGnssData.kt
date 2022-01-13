package com.example.foregroundlocationsample.models

import android.location.GnssClock
import android.location.GnssMeasurement
import android.location.GnssMeasurementsEvent
import android.os.Build

class RawGnssData(event: GnssMeasurementsEvent?) {
    var clock: GnssClock? = event?.clock
    var measurements: MutableCollection<GnssMeasurement>? = event?.measurements


    override fun toString(): String {
        val clockDataStr = getClockDataStr()

        var measurementDataStr = ""

        measurements?.forEach { measurement ->
            measurementDataStr +=  getMeasurementsDataStr(measurement)
        }

        return "\n$clockDataStr \n$measurementDataStr " +
                "\n-------------------------------------------------\n"
    }

    private fun getMeasurementsDataStr(measurement: GnssMeasurement): String {
        val carrierFrequencyHz = if (measurement.hasCarrierFrequencyHz()) measurement.carrierFrequencyHz else "null"
        val carrierCycles = if (measurement.hasCarrierCycles()) measurement.carrierCycles.toString() else "null"
        val carrierPhase = if (measurement.hasCarrierPhase()) measurement.carrierPhase.toString() else "null"
        val carrierPhaseUncertainty = if (measurement.hasCarrierPhaseUncertainty()) measurement.carrierPhaseUncertainty else "null"
        val snrInDb = if (measurement.hasSnrInDb()) measurement.snrInDb else "null"
        val automaticGainControlLevelDb =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && measurement.hasAutomaticGainControlLevelDb())
                measurement.automaticGainControlLevelDb
            else "null"


        return "\nRAW MEASUREMENTS: " +
                "\n :: svid: ${measurement.svid}" +
                "\n :: timeOffsetNanos: ${measurement.timeOffsetNanos}" +
                "\n :: state: ${measurement.state}" +
                "\n :: receivedSvTimeNanos: ${measurement.receivedSvTimeNanos}" +
                "\n :: receivedSvTimeUncertaintyNanos: ${measurement.receivedSvTimeUncertaintyNanos}" +
                "\n :: cn0DbHz: ${measurement.cn0DbHz}" +
                "\n :: pseudorangeRateMetersPerSecond: ${measurement.pseudorangeRateMetersPerSecond}" +
                "\n :: pseudorangeRateUncertaintyMetersPerSecond: ${measurement.pseudorangeRateUncertaintyMetersPerSecond}" +
                "\n :: accumulatedDeltaRangeState: ${measurement.accumulatedDeltaRangeState}" +
                "\n :: accumulatedDeltaRangeMeters: ${measurement.accumulatedDeltaRangeMeters}" +
                "\n :: accumulatedDeltaRangeUncertaintyMeters: ${measurement.accumulatedDeltaRangeUncertaintyMeters}" +
                "\n :: carrierFrequencyHz: $carrierFrequencyHz" +
                "\n :: carrierCycles: $carrierCycles" +
                "\n :: carrierPhase: $carrierPhase" +
                "\n :: carrierPhaseUncertainty: $carrierPhaseUncertainty" +
                "\n :: multipathIndicator: ${measurement.multipathIndicator}" +
                "\n :: snrInDb: $snrInDb" +
                "\n :: constellationType: ${measurement.constellationType}" +
                "\n :: automaticGainControlLevelDb: $automaticGainControlLevelDb\n"
    }

    private fun getClockDataStr(): String {
        var leapSecond = ""
        var timeUncertaintyNanos = ""
        var biasNanos = ""
        var biasUncertaintyNanos = ""
        var driftNanosPerSecond = ""
        var driftUncertaintyNanosPerSecond = ""

        clock?.let {
            leapSecond = if (it.hasLeapSecond()) it.leapSecond.toString() else "null"
            timeUncertaintyNanos = if (it.hasTimeUncertaintyNanos()) it.timeUncertaintyNanos.toString() else "null"
            biasNanos = if (it.hasBiasNanos()) it.biasNanos.toString() else "null"
            biasUncertaintyNanos = if (it.hasBiasUncertaintyNanos()) it.biasUncertaintyNanos.toString() else "null"
            driftNanosPerSecond = if (it.hasDriftNanosPerSecond()) it.hasDriftNanosPerSecond().toString() else "null"
            driftUncertaintyNanosPerSecond = if (it.hasDriftUncertaintyNanosPerSecond()) it.driftUncertaintyNanosPerSecond.toString() else "null"
        }

        return "CLOCK DATA: " +
                "\n :: timeNanos: ${clock?.timeNanos}" +
                "\n :: leapSecond: $leapSecond" +
                "\n :: timeUncertaintyNanos: $timeUncertaintyNanos" +
                "\n :: fullBiasNanos: ${clock?.fullBiasNanos}" +
                "\n :: biasNanos: $biasNanos" +
                "\n :: biasUncertaintyNanos: $biasUncertaintyNanos" +
                "\n :: driftNanosPerSecond: $driftNanosPerSecond" +
                "\n :: driftUncertaintyNanosPerSecond: $driftUncertaintyNanosPerSecond" +
                "\n :: hardwareClockDiscontinuityCount: ${clock?.hardwareClockDiscontinuityCount}"
    }
}