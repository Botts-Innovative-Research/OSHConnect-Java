package org.sensorhub.oshconnect.constants;

/**
 * Enumeration of the services supported by OpenSensorHub and their endpoints
 */
public enum Service {
    /**
     * Connected Systems API
     */
    API("api"),
    SYSTEMS("systems"),
    DATASTREAMS("datastreams"),
    CONTROLSTREAMS("controlstreams"),
    OBSERVATIONS("observations"),
    COMMANDS("commands");

    /**
     * String representing the endpoint where the service is supported on the server.
     */
    private final String endpoint;

    /**
     * Constructor
     *
     * @param endpoint String representing the endpoint where the service is supported on the server.
     */
    Service(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public String toString() {
        return getEndpoint();
    }

    /**
     * String representing the endpoint where the service is supported on the server.
     */
    public String getEndpoint() {
        return endpoint;
    }
}
