package com.example.foregroundlocationsample.models
/**
 * Clock data
 */
data class ClockModel(
    var elapsedRealtime: String = "",
    var timeNanos: String = "",
    var leapSecond: String = "",
    var timeUncertaintyNanos: String = "",
    var fullBiasNanos: String = "",
    var biasNanos: String = "",
    var biasUncertaintyNanos: String = "",
    var driftNanosPerSecond: String = "",
    var driftUncertaintyNanosPerSecond: String = "",
    var hardwareClockDiscontinuityCount: String = "",
)