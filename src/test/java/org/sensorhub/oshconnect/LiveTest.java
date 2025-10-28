package org.sensorhub.oshconnect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.sensorhub.impl.SensorHub;

import java.lang.Thread;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class LiveTest {

    private OSHNode node = null;

    @BeforeEach
    void createNode() {

//        var oshConnect = new OSHConnect();
//
//        boolean isSecure = true;
//        String lUrl = "https://osh-dev.botts-inc.com:8443/sensorhub";
//        node = oshConnect.createNode(lUrl, isSecure, "anonymous", "anonymous");
    }

    @Test
    void TestAgainstLiveNode() {


        try {
            Thread thread = new Thread(() -> {
                System.out.println("Starting OSH");

                SensorHub.main(new String[] {"src/main/resources/testconfig.json",
                        "storage"});

                while ( true ) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            thread.start();

            Thread.sleep(2000);
            var oshConnect = new OSHConnect();
            boolean isSecure = false;
            String lUrl = "http://127.0.0.1:8181/sensorhub";
            node = oshConnect.createNode(lUrl, isSecure, "anonymous", "anonymous");



            CompletableFuture
                    .supplyAsync(() -> {
                        try {
                            return node != null ? node.discoverSystems() : null;
                        } catch ( ExecutionException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .thenAccept(systemsResult -> {
                        if (systemsResult == null) return;

                        for (OSHSystem cSystem : systemsResult) {
                            System.out.println("OSHSystemID OpenSensorHub System Found. ID: " + cSystem.getId());
                        }
                    })
                    .exceptionally(ex -> {
                        System.out.println("OSHSystemID Exception thrown: " + ex.getMessage());
                        return null;
                    })
                    .join();



            thread.join();

        } catch (InterruptedException e ) {
            throw new RuntimeException(e);
        }
    }
}
