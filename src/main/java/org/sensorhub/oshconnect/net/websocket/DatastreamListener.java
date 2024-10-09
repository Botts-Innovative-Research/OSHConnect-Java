package org.sensorhub.oshconnect.net.websocket;

import org.sensorhub.oshconnect.datamodels.Observation;
import org.sensorhub.oshconnect.net.RequestFormat;
import org.sensorhub.oshconnect.oshdatamodels.OSHDatastream;
import org.sensorhub.oshconnect.time.TimePeriod;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;

/**
 * Class representing a listener for a datastream.
 */
public abstract class DatastreamListener {
    private static final String DATE_REGEX_TEXT = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:";
    private static final String DATE_REGEX_XML = "<[^>]+>(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d+Z)</[^>]+>";
    /**
     * The datastream being listened to.
     */
    @Getter
    private final OSHDatastream datastream;
    private final WebSocketConnection webSocketConnection;
    private final RequestFormat requestFormat;

    protected DatastreamListener(OSHDatastream datastream) {
        this.datastream = datastream;
        this.requestFormat = null;
        webSocketConnection = new WebSocketConnection(this, datastream.getObservationsEndpoint());
    }

    protected DatastreamListener(OSHDatastream datastream, RequestFormat format) {
        this.datastream = datastream;
        this.requestFormat = format;
        webSocketConnection = new WebSocketConnection(this, datastream.getObservationsEndpoint(format));
    }

    protected DatastreamListener(OSHDatastream datastream, RequestFormat format, int replaySpeed) {
        this.datastream = datastream;
        this.requestFormat = format;
        webSocketConnection = new WebSocketConnection(this, datastream.getObservationsEndpoint(format, replaySpeed));
    }

    protected DatastreamListener(OSHDatastream datastream, RequestFormat format, int replaySpeed, String start, String end) {
        this.datastream = datastream;
        this.requestFormat = format;
        webSocketConnection = new WebSocketConnection(this, datastream.getObservationsEndpoint(format, replaySpeed, start, end));
    }

    protected DatastreamListener(OSHDatastream datastream, String request) {
        this.datastream = datastream;
        this.requestFormat = null;
        webSocketConnection = new WebSocketConnection(this, request);
    }

    protected DatastreamListener(OSHDatastream datastream, String request, RequestFormat format) {
        this.datastream = datastream;
        this.requestFormat = format;
        webSocketConnection = new WebSocketConnection(this, request);
    }

    public void onStreamUpdate(byte[] data) {
        RequestFormat format = requestFormat;
        if (format == null) {
            // Determine the format of the datastream
            if (data[0] == '{') {
                format = RequestFormat.JSON;
            } else if (data[0] == '<') {
                format = RequestFormat.SWE_XML;
            } else {
                // Check if the first 14 characters are a date.
                byte[] first14Bytes = new byte[14];
                System.arraycopy(data, 0, first14Bytes, 0, 14);
                String dataString = new String(first14Bytes);
                if (dataString.length() > 14 && dataString.substring(0, 14).matches(DATE_REGEX_TEXT)) {
                    format = RequestFormat.PLAIN_TEXT;
                } else {
                    format = RequestFormat.SWE_BINARY;
                }
            }
        }

        try {
            if (format == RequestFormat.JSON || format == RequestFormat.OM_JSON || format == RequestFormat.SWE_JSON) {
                Observation observation = Observation.fromJson(data);
                long timestamp = Instant.parse(observation.getPhenomenonTime()).toEpochMilli();
                onStreamJson(timestamp, observation);
            } else if (format == RequestFormat.SWE_XML) {
                // Get the timestamp from the first date in the XML
                String xml = new String(data);

                Pattern pattern = Pattern.compile(DATE_REGEX_XML);
                Matcher matcher = pattern.matcher(xml);

                long timestamp = -1;
                if (matcher.find()) {
                    String date = matcher.group(1);
                    timestamp = Instant.parse(date).toEpochMilli();
                }

                onStreamXml(timestamp, xml);
            } else if (format == RequestFormat.SWE_CSV || format == RequestFormat.PLAIN_TEXT) {
                // Get the timestamp from the first element of the CSV
                String text = new String(data);
                String[] parts = text.split(",");
                long timestamp = Instant.parse(parts[0]).toEpochMilli();
                onStreamCsv(timestamp, text);
            } else if (format == RequestFormat.SWE_BINARY) {
                ByteBuffer buffer = ByteBuffer.wrap(data);

                // In order to get timestamps from binary data we need to
                // extract first 8 bytes as a double,
                // then need to ensure we are getting UTC offset time,
                // otherwise timestamps will offset to UTC time.
                // Other timestamps already convert properly, so we don't have to do anything to them.
                long timestamp = TimePeriod.epochTimeToUtc(buffer.getDouble());
                onStreamBinary(timestamp, data);
            }
        } catch (Exception e) {
            System.out.println("Error parsing timestamp: " + e.getMessage());
        }
    }

    public void onStreamBinary(long timestamp, byte[] data) {
        // Default implementation does nothing
    }

    public void onStreamJson(long timestamp, Observation observation) {
        // Default implementation does nothing
    }

    public void onStreamCsv(long timestamp, String csv) {
        // Default implementation does nothing
    }

    public void onStreamXml(long timestamp, String xml) {
        // Default implementation does nothing
    }

    public void connect() {
        webSocketConnection.connect();
    }

    public void disconnect() {
        webSocketConnection.disconnect();
    }
}
