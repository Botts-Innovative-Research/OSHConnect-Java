package org.sensorhub.oshconnect.notification;

import org.sensorhub.oshconnect.OSHNode;

public interface INotificationNode extends INotificationItem<OSHNode> {
    @Override
    void onItemAdded(OSHNode item);

    @Override
    void onItemRemoved(OSHNode item);
}
