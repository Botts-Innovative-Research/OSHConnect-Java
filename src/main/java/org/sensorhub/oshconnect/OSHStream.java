package org.sensorhub.oshconnect;

public interface OSHStream {
    OSHSystem getParentSystem();

    String getEndpoint();
}
