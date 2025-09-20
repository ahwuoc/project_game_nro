package Dragon.server.netty;

import Dragon.utils.Logger;

/**
 * Netty Server Manager - provides integration with existing ServerManager
 * Allows switching between traditional and Netty networking
 */
public class NettyServerManager {

    private static NettyServerManager instance;
    private NettyServer nettyServer;
    private boolean useNetty = false;

    public static NettyServerManager getInstance() {
        if (instance == null) {
            instance = new NettyServerManager();
        }
        return instance;
    }

    private NettyServerManager() {
        // Private constructor for singleton
    }

    /**
     * Start Netty server instead of traditional server
     */
    public void startNettyServer(int port) {
        if (useNetty) {
            Logger.log("NettyServerManager: Netty server is already running!");
            return;
        }

        try {
            Logger.log("NettyServerManager: Starting Netty server on port " + port + "...");
            nettyServer = NettyServer.getInstance();

            // Start server in a separate thread to avoid blocking
            Thread serverThread = new Thread(() -> {
                try {
                    nettyServer.start(port);
                } catch (InterruptedException e) {
                    Logger.log("NettyServerManager: Netty server interrupted");
                } catch (Exception e) {
                    Logger.logException(NettyServerManager.class, e, "Failed to start Netty server");
                }
            }, "NettyServerThread");

            serverThread.setDaemon(true);
            serverThread.start();

            useNetty = true;
            Logger.log("NettyServerManager: Netty server started successfully!");

        } catch (Exception e) {
            Logger.logException(NettyServerManager.class, e, "Failed to initialize Netty server");
            useNetty = false;
        }
    }

    /**
     * Stop Netty server
     */
    public void stopNettyServer() {
        if (!useNetty || nettyServer == null) {
            Logger.log("NettyServerManager: Netty server is not running!");
            return;
        }

        try {
            Logger.log("NettyServerManager: Stopping Netty server...");
            nettyServer.shutdown();
            useNetty = false;
            Logger.log("NettyServerManager: Netty server stopped successfully!");

        } catch (Exception e) {
            Logger.logException(NettyServerManager.class, e, "Failed to stop Netty server");
        }
    }

    /**
     * Check if using Netty
     */
    public boolean isUsingNetty() {
        return useNetty;
    }

    /**
     * Get Netty server instance
     */
    public NettyServer getNettyServer() {
        return nettyServer;
    }

    /**
     * Get server statistics
     */
    public String getStats() {
        if (useNetty && nettyServer != null) {
            return nettyServer.getStats();
        } else {
            return "NettyServerManager: Netty server is not running";
        }
    }

    /**
     * Force close all connections
     */
    public void forceCloseAllConnections() {
        if (useNetty && nettyServer != null) {
            nettyServer.forceCloseAllConnections();
        } else {
            Logger.log("NettyServerManager: Netty server is not running!");
        }
    }

    /**
     * Switch to Netty mode (for testing)
     */
    public void enableNettyMode() {
        Logger.log("NettyServerManager: Netty mode enabled");
        Logger.log("NettyServerManager: Next server restart will use Netty");
        // This flag can be used by ServerManager to decide which server to start
        System.setProperty("use.netty", "true");
    }

    /**
     * Switch to traditional mode
     */
    public void disableNettyMode() {
        Logger.log("NettyServerManager: Traditional mode enabled");
        Logger.log("NettyServerManager: Next server restart will use traditional server");
        System.setProperty("use.netty", "false");
    }

    /**
     * Check if Netty mode is enabled via system property
     */
    public static boolean isNettyModeEnabled() {
        return "true".equals(System.getProperty("use.netty", "false"));
    }
}
