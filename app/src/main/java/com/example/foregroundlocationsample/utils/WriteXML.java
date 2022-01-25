package com.example.foregroundlocationsample.utils;

import android.content.Context;
import android.location.GnssClock;
import android.location.GnssMeasurement;
import android.location.GnssStatus;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.example.foregroundlocationsample.models.RawGnssData;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class WriteXML {

    public static void createGnssXML(List<RawGnssData> rawData, Context context) throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // root element (gnss)
        Document document = docBuilder.newDocument();
        Element gnssElement = document.createElement("gnss");
        document.appendChild(gnssElement);

        for (RawGnssData raw : rawData) {
            if (raw.getMeasurements() == null || raw.getMeasurements().size() <= 0) {
                continue;
            }
            // raw element
            Element rawElement = document.createElement("raw");
            gnssElement.appendChild(rawElement);

            // clock element
            Element clockElement = document.createElement("clock");
            clockElement.setTextContent(writeClockContext(raw.getClock()));
            rawElement.appendChild(clockElement);

            // list of measurements elements
            if (raw.getMeasurements() == null || raw.getMeasurements().size() <= 0) {
                return;
            }
            for (GnssMeasurement measurement : raw.getMeasurements()) {
                Element measurementElement = document.createElement("measurement");
                if (measurement.getConstellationType() != GnssStatus.CONSTELLATION_GPS) {
                    continue;
                }
                measurementElement.setTextContent(writeMeasurementContext(measurement));
                rawElement.appendChild(measurementElement);
            }
        }

        writeXml(document, context);
    }

    private static void writeXml(Document document, Context context) {
        try {
            File xmlFile = new File(context.getFilesDir(), "raw_gnss.xml");
            FileOutputStream output = new FileOutputStream(xmlFile);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(output);
            transformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String writeMeasurementContext(GnssMeasurement measurement) {
        List<String> measurements = new ArrayList<>();
        measurements.add(String.valueOf(measurement.getSvid())); // Svid
        measurements.add(String.valueOf(measurement.getTimeOffsetNanos())); // TimeOffsetNanos
        measurements.add(String.valueOf(measurement.getState())); // State
        measurements.add(String.valueOf(measurement.getReceivedSvTimeNanos())); // ReceivedSvTimeNanos
        measurements.add(String.valueOf(measurement.getReceivedSvTimeUncertaintyNanos())); // ReceivedSvTimeUncertaintyNanos
        measurements.add(String.valueOf(measurement.getCn0DbHz())); // Cn0DbHz
        measurements.add(String.valueOf(measurement.getPseudorangeRateMetersPerSecond())); // PseudorangeRateMetersPerSecond
        measurements.add(String.valueOf(measurement.getPseudorangeRateUncertaintyMetersPerSecond())); // PseudorangeRateUncertaintyMetersPerSecond
        measurements.add(String.valueOf(measurement.getAccumulatedDeltaRangeState())); // AccumulatedDeltaRangeState
        measurements.add(String.valueOf(measurement.getAccumulatedDeltaRangeMeters())); // AccumulatedDeltaRangeMeters
        measurements.add(String.valueOf(measurement.getAccumulatedDeltaRangeUncertaintyMeters())); // AccumulatedDeltaRangeUncertaintyMeters
        measurements.add(measurement.hasCarrierFrequencyHz() ? String.valueOf(measurement.getCarrierFrequencyHz()) : ""); // CarrierFrequencyHz
        measurements.add(measurement.hasCarrierCycles() ? String.valueOf(measurement.getCarrierCycles()) : ""); // CarrierCycles
        measurements.add(measurement.hasCarrierPhase() ? String.valueOf(measurement.getCarrierPhase()) : ""); // CarrierPhase
        measurements.add(measurement.hasCarrierPhaseUncertainty() ? String.valueOf(measurement.getCarrierPhaseUncertainty()) : ""); // CarrierPhaseUncertainty
        measurements.add(String.valueOf(measurement.getMultipathIndicator())); // MultipathIndicator
        measurements.add(measurement.hasSnrInDb() ? String.valueOf(measurement.getSnrInDb()) : ""); // SnrInDb
        measurements.add(String.valueOf(measurement.getConstellationType())); // ConstellationType

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && measurement.hasAutomaticGainControlLevelDb()) {
            measurements.add(String.valueOf(measurement.getAutomaticGainControlLevelDb())); // AutomaticGainControlLevelDb
        } else {
            measurements.add("");
        }

        return TextUtils.join(",", measurements);
    }

    private static String writeClockContext(@Nullable GnssClock clock) {
        if (clock == null) {
            return "";
        }
        List<String> clocks = new ArrayList<>();
        clocks.add(String.valueOf(clock.getTimeNanos())); // timeNanos
        clocks.add(clock.hasLeapSecond() ? String.valueOf(clock.getLeapSecond()) : ""); // leapSecond
        clocks.add(clock.hasTimeUncertaintyNanos() ? String.valueOf(clock.getTimeUncertaintyNanos()) : ""); // timeUncertaintyNanos
        clocks.add(String.valueOf(clock.getFullBiasNanos())); // fullBiasNanos
        clocks.add(clock.hasBiasNanos() ? String.valueOf(clock.getBiasNanos()) : ""); // BiasNanos
        clocks.add(clock.hasBiasUncertaintyNanos() ? String.valueOf(clock.getBiasUncertaintyNanos()) : ""); // BiasUncertaintyNanos(
        clocks.add(clock.hasDriftNanosPerSecond() ? String.valueOf(clock.getDriftNanosPerSecond()) : ""); // DriftNanosPerSecond
        clocks.add(clock.hasDriftUncertaintyNanosPerSecond() ? String.valueOf(clock.getDriftUncertaintyNanosPerSecond()) : ""); // DriftUncertaintyNanosPerSecond
        clocks.add(String.valueOf(clock.getHardwareClockDiscontinuityCount()));
        return TextUtils.join(",", clocks);
    }
}
