package org.sensorhub.oshconnect;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.api.database.IObsSystemDatabase;
import org.sensorhub.api.module.IModule;
import org.sensorhub.api.module.ModuleEvent;
import org.sensorhub.impl.SensorHub;
import org.sensorhub.impl.datastore.h2.MVObsSystemDatabaseConfig;
import org.sensorhub.impl.service.HttpServer;
import org.sensorhub.impl.service.HttpServerConfig;
import org.sensorhub.impl.service.consys.ConSysApiService;
import org.sensorhub.impl.service.consys.ConSysApiServiceConfig;

import java.io.File;
import java.io.IOException;

import static org.sensorhub.oshconnect.TestConstants.*;

public class TestBase {
    static final long TIMEOUT = 10000;
    protected SensorHub hub;
    protected File dbFile;
    protected ConSysApiService service;
    protected IObsSystemDatabase db;
    protected OSHConnect oshConnect;
    protected OSHNode node;

    @BeforeEach
    void setupTestBase() throws IOException, SensorHubException {
        // Use a temp DB file.
        dbFile = File.createTempFile("swe-api-db-", ".dat");
        dbFile.deleteOnExit();

        // Get the instance with in-memory DB
        hub = new SensorHub();
        hub.start();
        var moduleRegistry = hub.getModuleRegistry();

        // Start the HTTP server
        HttpServerConfig httpConfig = new HttpServerConfig();
        httpConfig.httpPort = SERVER_PORT;
        var httpServer = (HttpServer) moduleRegistry.loadModule(httpConfig, TIMEOUT);

        // Start the DB
        MVObsSystemDatabaseConfig dbCfg = new MVObsSystemDatabaseConfig();
        dbCfg.storagePath = dbFile.getAbsolutePath();
        dbCfg.databaseNum = 2;
        dbCfg.readOnly = false;
        dbCfg.name = "SWE API Database";
        dbCfg.autoStart = true;
        db = (IObsSystemDatabase) moduleRegistry.loadModule(dbCfg, TIMEOUT);
        ((IModule<?>) db).waitForState(ModuleEvent.ModuleState.STARTED, TIMEOUT);

        // Start the Connected Systems API service
        ConSysApiServiceConfig swaCfg = new ConSysApiServiceConfig();
        swaCfg.databaseID = dbCfg.id;
        swaCfg.endPoint = "/api";
        swaCfg.name = "ConSys API Service";
        swaCfg.autoStart = true;
        service = (ConSysApiService) moduleRegistry.loadModule(swaCfg, TIMEOUT);
        service.waitForState(ModuleEvent.ModuleState.STARTED, TIMEOUT);

        // Create the OSHConnect instance
        oshConnect = new OSHConnect();
        node = oshConnect.createNode(httpServer.getServletsBaseUrl(), IS_SECURE, USERNAME, PASSWORD);
    }

    @AfterEach
    void teardownTestBase() {
        try {
            if (hub != null)
                hub.stop();
        } finally {
            if (dbFile != null) {
                boolean deleted = dbFile.delete();
                if (!deleted)
                    dbFile.deleteOnExit();
            }
        }

        oshConnect.shutdown();
    }
}
