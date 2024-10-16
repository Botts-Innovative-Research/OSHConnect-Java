package org.sensorhub.oshconnect.config;

import org.sensorhub.oshconnect.oshdatamodels.OSHNode;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OSHConnectConfigData {
    private String name;
    private List<OSHNode> nodes;

    public OSHConnectConfigData(String name, List<OSHNode> nodes) {
        this.name = name;
        this.nodes = nodes;
    }
}
