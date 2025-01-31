package org.sensorhub.oshconnect.datamodels;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import net.opengis.swe.v20.DataBlock;
import org.sensorhub.api.command.ICommandStatus;
import org.vast.util.Asserts;
import org.vast.util.BaseBuilder;
import org.vast.util.TimeExtent;

import java.time.Instant;
import java.util.List;

@Getter
public class CommandData {
    /**
     * Local ID of the command.
     */
    protected String id;
    /**
     * Local ID of the control stream that the command is part of.
     */
    @SerializedName("controlstream@id")
    protected String controlStreamId;
    /**
     * Local ID of the sampling feature that is the target of the command.
     */
    @SerializedName("samplingFeature@id")
    protected String samplingFeatureId;
    /**
     * Link to the procedure/method used to make the command.
     */
    @SerializedName("procedure@link")
    protected Link procedureLink;
    /**
     * Time at which the command was issued.
     * If omitted on creation, the server sets it to the time the request was received.
     */
    protected Instant issueTime;
    /**
     * Time period during which the command was executed
     */
    protected TimeExtent executionTime;
    /**
     * Identifier of the person or entity who submitted the command.
     */
    protected String sender;
    /**
     * Current status of the command.
     */
    protected ICommandStatus.CommandStatusCode currentStatus;
    /**
     * Command parameters.
     * Must be valid, according to the schema provided in the control stream metadata.
     */
    protected DataBlock parameters;
    /**
     * Links to related resources.
     */
    protected List<Link> links;

    public static CommandData.CommandDataBuilder newBuilder() {
        return new CommandData.CommandDataBuilder();
    }

    public static class CommandDataBuilder extends BaseBuilder<CommandData> {
        CommandDataBuilder() {
            this.instance = new CommandData();
        }

        public CommandDataBuilder id(String id) {
            instance.id = id;
            return this;
        }

        public CommandDataBuilder controlStreamId(String controlStreamId) {
            instance.controlStreamId = controlStreamId;
            return this;
        }

        public CommandDataBuilder samplingFeatureId(String samplingFeatureId) {
            instance.samplingFeatureId = samplingFeatureId;
            return this;
        }

        public CommandDataBuilder procedureLink(Link procedureLink) {
            instance.procedureLink = procedureLink;
            return this;
        }

        public CommandDataBuilder issueTime(Instant issueTime) {
            instance.issueTime = issueTime;
            return this;
        }

        public CommandDataBuilder executionTime(TimeExtent executionTime) {
            instance.executionTime = executionTime;
            return this;
        }

        public CommandDataBuilder sender(String sender) {
            instance.sender = sender;
            return this;
        }

        public CommandDataBuilder currentStatus(ICommandStatus.CommandStatusCode currentStatus) {
            instance.currentStatus = currentStatus;
            return this;
        }

        public CommandDataBuilder parameters(DataBlock parameters) {
            instance.parameters = parameters;
            return this;
        }

        public CommandDataBuilder links(List<Link> links) {
            instance.links = links;
            return this;
        }

        @Override
        public CommandData build() {
            Asserts.checkNotNull(instance.parameters, "parameters");
            return instance;
        }
    }
}
