package org.sensorhub.oshconnect.notification;

import org.sensorhub.oshconnect.oshdatamodels.OSHNode;

public interface INotificationNode extends INotificationItem<OSHNode> {
    @Override
    void onItemAdded(OSHNode item);

    @Override
    void onItemRemoved(OSHNode item);
}
