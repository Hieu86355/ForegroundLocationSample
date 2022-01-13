package com.example.foregroundlocationsample.models

import android.location.GnssClock
import android.location.GnssMeasurement
import android.location.GnssMeasurementsEvent

class RawGnssData(event: GnssMeasurementsEvent?) {
    var clock: GnssClock? = event?.clock
    var measurements: MutableCollection<GnssMeasurement>? = event?.measurements


    override fun toString(): String {
        val clockDataStr = "CLOCK DATA: " +
                "\n :: timeNanos: ${clock?.timeNanos}" +
                "\n :: leapSecond: ${clock?.leapSecond}" +
                "\n :: timeUncertaintyNanos: ${clock?.timeUncertaintyNanos}" +
                "\n :: fullBiasNanos: ${clock?.fullBiasNanos}" +
                "\n :: biasNanos: ${clock?.biasNanos}" +
                "\n :: biasUncertaintyNanos: ${clock?.biasUncertaintyNanos}" +
                "\n :: driftNanosPerSecond: ${clock?.driftNanosPerSecond}" +
                "\n :: driftUncertaintyNanosPerSecond: ${clock?.driftUncertaintyNanosPerSecond}" +
                "\n :: hardwareClockDiscontinuityCount: ${clock?.hardwareClockDiscontinuityCount}"

        var measurementDataStr = ""

        measurements?.forEach { measurement ->
            measurementDataStr +=  "\nRAW MEASUREMENTS: " +
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
                    "\n :: carrierFrequencyHz: ${measurement.carrierFrequencyHz}" +
                    "\n :: carrierCycles: ${measurement.carrierCycles}" +
                    "\n :: carrierPhase: ${measurement.carrierPhase}" +
                    "\n :: carrierPhaseUncertainty: ${measurement.carrierPhaseUncertainty}" +
                    "\n :: multipathIndicator: ${measurement.multipathIndicator}" +
                    "\n :: snrInDb: ${measurement.snrInDb}" +
                    "\n :: constellationType: ${measurement.constellationType}" +
                    "\n :: automaticGainControlLevelDb: ${measurement.automaticGainControlLevelDb}\n"
        }

        return "\n$clockDataStr \n$measurementDataStr " +
                "\n-------------------------------------------------\n"
    }
}