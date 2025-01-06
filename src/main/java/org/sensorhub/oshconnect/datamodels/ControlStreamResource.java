package org.sensorhub.oshconnect.datamodels;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ControlStreamResource {
    /**
     * Local resource ID. If set on creation, the server may ignore it.
     */
    private final String id;

    /**
     * Human readable name of the resource.
     */
    private final String name;

    /**
     * Human readable description of the resource.
     */
    private final String description;

    /**
     * Validity period of the resource.
     */
    private final List<String> validTime;

    /**
     * List of available formats.
     */
    private final List<String> formats;

    /**
     * ID of the system producing the observations.
     */
    @SerializedName("system@id")
    private final String systemId;

    /**
     * Link to the system producing the observations.
     */
    @SerializedName("system@link")
    private final Link systemLink;

    /**
     * Name of the system control input receiving data from this control stream.
     */
    private final String inputName;

    /**
     * Link to the procedure used to execute commands
     * (only provided if all commands in the control stream share the same procedure).
     */
    @SerializedName("procedure@link")
    private final Link procedureLink;

    /**
     * Link to the deployment during which the commands are/were received
     * (only provided if all commands in the control stream share the same deployment).
     */
    @SerializedName("deployment@link")
    private final Link deploymentLink;

    /**
     * Link to the ultimate feature of interest
     * (only provided if all commands in the control stream share the same feature of interest).
     */
    @SerializedName("featureOfInterest@link")
    private final Link featureOfInterestLink;

    /**
     * Link to the sampling feature
     * (only provided if all commands in the control stream share the same sampling feature).
     */
    @SerializedName("samplingFeature@link")
    private final Link samplingFeature;

    /**
     * List of properties that are controllable through this control stream.
     */
    private final List<ObservedProperty> actuableProperties;

    /**
     * Time extent spanning all issue times of commands in this control stream.
     */
    private final List<String> issueTime;

    /**
     * Time extent spanning all execution times of commands in this control stream.
     */
    private final List<String> actuationTime;

    /**
     * Flag indicating if the command channel can currently accept commands.
     */
    private final Boolean live;

    /**
     * Flag indicating if the command channel processes commands asynchronously.
     */
    private final Boolean async;

    /**
     * Links to related resources.
     */
    private final List<Link> links;

    public String toJson() {
        return new Gson().toJson(this);
    }

    @Override
    public String toString() {
        return toJson();
    }
}
