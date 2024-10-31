package org.sensorhub.oshconnect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.sensorhub.oshconnect.TestConstants.IS_SECURE;
import static org.sensorhub.oshconnect.TestConstants.PASSWORD;
import static org.sensorhub.oshconnect.TestConstants.SENSOR_HUB_ROOT;
import static org.sensorhub.oshconnect.TestConstants.USERNAME;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sensorhub.oshconnect.notification.INotificationNode;
import org.sensorhub.oshconnect.oshdatamodels.OSHNode;

import java.lang.reflect.Field;
import java.util.Set;

class NotificationManagerTest {
    private OSHConnect oshConnect;
    private NotificationManager notificationManager;

    @BeforeEach
    void setUp() {
        oshConnect = new OSHConnect();
        notificationManager = oshConnect.getNotificationManager();
    }

    @AfterEach
    void tearDown() {
        oshConnect.shutdown();
    }

    @Test
    void addNodeNotificationListener() throws NoSuchFieldException, IllegalAccessException {
        Field field = NotificationManager.class.getDeclaredField("nodeNotificationListeners");
        field.setAccessible(true);
        Set<?> listeners = (Set<?>) field.get(notificationManager);

        assertEquals(0, listeners.size());
        notificationManager.addNodeNotificationListener(createNodeNotificationListener(new boolean[1], new boolean[1]));
        assertEquals(1, listeners.size());
    }

    @Test
    void addNodeNotificationListener_Event() {
        boolean[] added = {false};
        boolean[] removed = {false};
        notificationManager.addNodeNotificationListener(createNodeNotificationListener(added, removed));

        OSHNode node = oshConnect.createNode(SENSOR_HUB_ROOT, IS_SECURE, USERNAME, PASSWORD);
        assertTrue(added[0]);
        oshConnect.getNodeManager().removeNode(node);
        assertTrue(removed[0]);
    }

    @Test
    void removeNodeNotificationListener() throws NoSuchFieldException, IllegalAccessException {
        Field field = NotificationManager.class.getDeclaredField("nodeNotificationListeners");
        field.setAccessible(true);
        Set<?> listeners = (Set<?>) field.get(notificationManager);

        assertEquals(0, listeners.size());
        INotificationNode listener = createNodeNotificationListener(new boolean[1], new boolean[1]);
        notificationManager.addNodeNotificationListener(listener);
        assertEquals(1, listeners.size());
        notificationManager.removeNodeNotificationListener(listener);
        assertEquals(0, listeners.size());
    }

    INotificationNode createNodeNotificationListener(boolean[] added, boolean[] removed) {
        return new INotificationNode() {
            @Override
            public void onItemAdded(OSHNode item) {
                added[0] = true;
            }

            @Override
            public void onItemRemoved(OSHNode item) {
                removed[0] = true;
            }
        };
    }

}
