package org.sensorhub.oshconnect.net.websocket;

import lombok.Getter;
import org.json.JSONObject;
import org.sensorhub.oshconnect.net.RequestFormat;
import org.sensorhub.oshconnect.oshdatamodels.OSHDatastream;
import org.vast.util.TimeExtent;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Listener for a single datastream.
 * Override the {@link #onStreamUpdate(DatastreamEventArgs)} method to handle the data received from the datastream.
 * To listen to multiple datastreams, use a {@link DatastreamHandler}.
 */
public abstract class DatastreamListener implements DatastreamEventListener {
    private static final String DATE_REGEX_TEXT = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:";
    private static final String DATE_REGEX_XML = "<[^>]+>(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d+Z)</[^>]+>";
    /**
     * The datastream being listened to.
     */
    @Getter
    private final OSHDatastream datastream;
    private boolean isShutdown = false;
    /**
     * The WebSocket connection to the datastream.
     */
    private WebSocketConnection webSocketConnection;
    /**
     * The format of the request.
     * If null, the format will not be specified in the request, i.e., the data will be received in the default format.
     */
    @Getter
    private RequestFormat requestFormat;
    /**
     * The time period for the datastream.
     * If null, the time period will not be specified in the request, i.e., will listen to the datastream in real-time.
     */
    @Getter
    private TimeExtent timeExtent;
    /**
     * The replay speed for the datastream.
     * Only applicable for historical datastreams.
     * 1.0 is the default speed, 0.1 is 10 times slower, 10.0 is 10 times faster.
     * 0 or negative values will result in no data being received.
     */
    @Getter
    private double replaySpeed = 1;

    /**
     * Creates a new datastream listener for the specified datastream.
     * By default, the listener will listen to the datastream in real-time.
     * To listen to historical data, use the setTimeExtent method.
     *
     * @param datastream the datastream to listen to.
     */
    protected DatastreamListener(OSHDatastream datastream) {
        this.datastream = datastream;
    }

    /**
     * Called when the datastream receives an update.
     * Parses the data and calls {@link #onStreamUpdate(DatastreamEventArgs)}.
     *
     * @param data the data received from the datastream.
     */
    public void onStreamUpdate(byte[] data) {
        RequestFormat format = determineRequestFormat(data);
        long timestamp = determineTimestamp(format, data);

        onStreamUpdate(new DatastreamEventArgs(timestamp, data, format, datastream));
    }

    /**
     * Called when the datastream receives an update.
     * Override this method to handle the data received from the datastream.
     *
     * @param args the arguments of the event.
     */
    @Override
    public void onStreamUpdate(DatastreamEventArgs args) {
        // Default implementation does nothing
    }

    /**
     * Connects to the datastream using the specified request format, replay speed, and time period.
     */
    public void connect() {
        if (getStatus() == StreamStatus.SHUTDOWN) {
            throw new IllegalStateException("Listener has been shut down.");
        }

        disconnect();
        String request = buildRequestString();
        webSocketConnection = new WebSocketConnection(this, request);
        webSocketConnection.connect();
    }

    /**
     * Disconnects from the datastream.
     */
    public void disconnect() {
        if (webSocketConnection != null) {
            webSocketConnection.disconnect();
            webSocketConnection = null;
        }
    }

    /**
     * Shuts down the datastream listener.
     * Disconnects from the datastream and prevents further connections.
     */
    public void shutdown() {
        isShutdown = true;
        disconnect();
    }

    /**
     * Reconnects to the datastream.
     * Used when the request format, replay speed, or time period is changed to update the connection.
     */
    private void reconnectIfConnected() {
        if (webSocketConnection != null) {
            connect();
        }
    }

    /**
     * Determines the format of the request based on the data.
     *
     * @param data the data received from the datastream.
     * @return the format of the request.
     */
    private RequestFormat determineRequestFormat(byte[] data) {
        if (requestFormat != null) {
            return requestFormat;
        }

        if (data[0] == '{') {
            return RequestFormat.JSON;
        } else if (data[0] == '<') {
            return RequestFormat.SWE_XML;
        } else {
            // Check if the first 14 characters are a date.
            byte[] first14Bytes = new byte[14];
            System.arraycopy(data, 0, first14Bytes, 0, 14);
            String dataString = new String(first14Bytes);
            if (dataString.length() > 14 && dataString.substring(0, 14).matches(DATE_REGEX_TEXT)) {
                return RequestFormat.PLAIN_TEXT;
            } else {
                return RequestFormat.SWE_BINARY;
            }
        }
    }

    /**
     * Determines the timestamp of the data.
     *
     * @param format the format of the request.
     * @param data   the data received from the datastream.
     * @return the timestamp of the data.
     */
    private long determineTimestamp(RequestFormat format, byte[] data) {
        if (format == RequestFormat.JSON || format == RequestFormat.OM_JSON || format == RequestFormat.SWE_JSON) {
            JSONObject json = new JSONObject(new String(data));
            String phenomenonTime = json.getString("phenomenonTime");
            return Instant.parse(phenomenonTime).toEpochMilli();
        } else if (format == RequestFormat.SWE_XML) {
            // Get the timestamp from the first date in the XML
            String xml = new String(data);

            Pattern pattern = Pattern.compile(DATE_REGEX_XML);
            Matcher matcher = pattern.matcher(xml);

            if (matcher.find()) {
                String date = matcher.group(1);
                return Instant.parse(date).toEpochMilli();
            }
        } else if (format == RequestFormat.SWE_CSV || format == RequestFormat.PLAIN_TEXT) {
            // Get the timestamp from the first element of the CSV
            String text = new String(data);
            String[] parts = text.split(",");
            return Instant.parse(parts[0]).toEpochMilli();
        } else if (format == RequestFormat.SWE_BINARY) {
            // Get the timestamp from the first 8 bytes of the binary data
            byte[] timestampBytes = Arrays.copyOfRange(data, 0, 8);
            ByteBuffer buffer = ByteBuffer.wrap(timestampBytes);
            double timestampDouble = buffer.getDouble();
            return (long) (timestampDouble * 1000);
        }

        return -1;
    }

    /**
     * Sets the format of the request.
     * If null, the format will not be specified in the request, i.e., the data will be received in the default format.
     * Calling this method will reconnect to the datastream if it is already connected.
     *
     * @param requestFormat the format of the request.
     *                      Set to null to remove the previously set format.
     */
    public void setRequestFormat(RequestFormat requestFormat) {
        this.requestFormat = requestFormat;
        reconnectIfConnected();
    }

    /**
     * Sets the replay speed for the datastream.
     * Only applicable for historical datastreams.
     * 1.0 is the default speed, 0.1 is 10 times slower, 10.0 is 10 times faster.
     * 0 or negative values will result in no data being received.
     * Calling this method will reconnect to the datastream if it is already connected.
     *
     * @param replaySpeed the replay speed of the request.
     */
    public void setReplaySpeed(double replaySpeed) {
        this.replaySpeed = replaySpeed;
        reconnectIfConnected();
    }

    /**
     * Sets the time period for the datastream.
     * If null, the time period will not be specified in the request, i.e., will listen to the datastream in real-time.
     * Calling this method will reconnect to the datastream if it is already connected.
     *
     * @param timeExtent the time period of the request.
     *                   Set to null to remove the previously set time period.
     */
    public void setTimeExtent(TimeExtent timeExtent) {
        this.timeExtent = timeExtent;
        reconnectIfConnected();
    }

    /**
     * Builds the request string for the datastream.
     *
     * @return the request string.
     */
    private String buildRequestString() {
        Map<String, String> parameters = new HashMap<>();

        if (requestFormat != null) {
            parameters.put("format", requestFormat.getMimeType());
        }
        if (timeExtent != null && !timeExtent.isNow()) {
            parameters.put("phenomenonTime", timeExtent.isoStringUTC(false));
            parameters.put("replaySpeed", String.valueOf(replaySpeed));
        }

        StringBuilder request = new StringBuilder(datastream.getObservationsEndpoint());
        if (!parameters.isEmpty()) {
            request.append("?");
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                request.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            request = new StringBuilder(request.substring(0, request.length() - 1));
        }

        return request.toString();
    }

    public StreamStatus getStatus() {
        if (isShutdown) {
            return StreamStatus.SHUTDOWN;
        } else if (webSocketConnection == null) {
            return StreamStatus.DISCONNECTED;
        } else {
            return webSocketConnection.getStatus();
        }
    }
}
