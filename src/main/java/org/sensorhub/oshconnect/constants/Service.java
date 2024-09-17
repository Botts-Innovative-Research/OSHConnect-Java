package org.sensorhub.oshconnect.constants;

/**
 * Enumeration of the services supported by OpenSensorHub and their endpoints
 */
public enum Service {
    /**
     * Sensor Web API
     */
    API("api"),
    /**
     * Sensor Observation Service
     */
    SOS("sos"),
    /**
     * Sensor Planning Service
     */
    SPS("sps"),
    /**
     * Visualization Recommendation Service
     */
    DISCOVERY("discovery");

    /**
     * String representing the endpoint where the service is supported on the server
     */
    private final String endpoint;

    /**
     * Constructor
     *
     * @param endpoint String representing the endpoint where the service is supported on the server
     */
    Service(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public String toString() {
        return endpoint;
    }
}
