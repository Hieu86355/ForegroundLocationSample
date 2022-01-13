package com.example.foregroundlocationsample.models

/**
 * Raw measurements data
 */
data class MeasurementModel(
    var svid: String = "",
    var timeOffsetNanos: String = "",
    var state: String = "",
    var receivedSvTimeNanos: String = "",
    var receivedSvTimeUncertaintyNanos: String = "",
    var cn0DbHz: String = "",
    var pseudorangeRateMetersPerSecond: String = "",
    var pseudorangeRateUncertaintyMetersPerSecond: String = "",
    var accumulatedDeltaRangeState: String = "",
    var accumulatedDeltaRangeMeters: String = "",
    var accumulatedDeltaRangeUncertaintyMeters: String = "",
    var carrierFrequencyHz: String = "",
    var carrierCycles: String = "",
    var carrierPhase: String = "",
    var carrierPhaseUncertainty: String = "",
    var multipathIndicator: String = "",
    var snrInDb: String = "",
    var constellationType: String = "",
    var automaticGainControlLevelDb: String = "",
)
