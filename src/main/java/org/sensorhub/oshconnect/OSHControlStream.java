package org.sensorhub.oshconnect;

import org.sensorhub.api.command.ICommandStreamInfo;
import org.sensorhub.impl.service.consys.client.ConSysApiClient;
import org.sensorhub.impl.service.consys.resource.ResourceFormat;
import org.sensorhub.oshconnect.constants.Service;
import org.sensorhub.oshconnect.datamodels.CommandData;
import org.sensorhub.oshconnect.net.ConSysApiClientExtras;
import org.sensorhub.oshconnect.util.Utilities;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class OSHControlStream implements OSHStream {
    private final OSHSystem parentSystem;
    private final String id;
    private ICommandStreamInfo controlStreamResource;

    public OSHControlStream(OSHSystem parentSystem, String id, ICommandStreamInfo controlStreamResource) {
        this.parentSystem = parentSystem;
        this.id = id;
        this.controlStreamResource = controlStreamResource;
    }

    /**
     * Returns the command with the specified ID.
     *
     * @param commandId The ID of the command to get.
     * @return The command with the specified ID.
     */
    public CommandData getCommand(String commandId) throws ExecutionException, InterruptedException {
        return getConnectedSystemsApiClientExtras().getCommand(id, commandId, controlStreamResource).get();
    }

    /**
     * Returns the latest commands of this control stream.
     *
     * @return A list of CommandData objects.
     */
    public List<CommandData> getCommands() throws ExecutionException, InterruptedException {
        return getConnectedSystemsApiClientExtras().getCommands(id, controlStreamResource).get();
    }

    /**
     * Updates the control stream properties on the server.
     * Note: After updating the control stream, the properties are refreshed from the server,
     * not set to the provided properties.
     *
     * @param controlStreamInfo The new control stream properties.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateControlStream(ICommandStreamInfo controlStreamInfo) throws ExecutionException, InterruptedException {
        Integer response = getConnectedSystemsApiClientExtras().updateControlStream(id, controlStreamInfo).get();
        boolean success = response != null && response >= 200 && response < 300;

        if (success) {
            return refreshControlStream();
        }
        return false;
    }

    /**
     * Refreshes the control stream properties from the server.
     *
     * @return true if the operation was successful, otherwise false.
     */
    public boolean refreshControlStream() throws ExecutionException, InterruptedException {
        ICommandStreamInfo response = getConnectedSystemsApiClient().getControlStreamById(id, ResourceFormat.JSON, true).get();
        boolean success = response != null;

        if (success) {
            controlStreamResource = response;
        }
        return success;
    }

    /**
     * Push a command to this control stream.
     *
     * @param command The command to add.
     * @return The ID of the command if the operation was successful, otherwise null.
     */
    public String pushCommand(CommandData command) throws ExecutionException, InterruptedException {
        return getConnectedSystemsApiClientExtras().pushCommand(id, controlStreamResource, command).get();
    }

    public ConSysApiClient getConnectedSystemsApiClient() {
        return parentSystem.getConnectedSystemsApiClient();
    }

    public ConSysApiClientExtras getConnectedSystemsApiClientExtras() {
        return parentSystem.getConnectedSystemsApiClientExtras();
    }

    public OSHSystem getParentSystem() {
        return parentSystem;
    }

    /**
     * Returns the endpoint for the commands of this control stream.
     */
    public String getEndpoint() {
        return Utilities.joinPath(parentSystem.getParentNode().getApiEndpoint(), Service.CONTROLSTREAMS.getEndpoint(), getId(), Service.COMMANDS.getEndpoint());
    }

    public String getId() {
        return id;
    }

    public ICommandStreamInfo getControlStreamResource() {
        return controlStreamResource;
    }
}
