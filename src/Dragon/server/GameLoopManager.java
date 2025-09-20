package Dragon.server;

import Dragon.models.map.Map;
import Dragon.models.player.Player;
import Dragon.utils.Logger;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author ahwuocdz use chatgpt nhé các em :))
 *         GameLoopManager - Tối ưu game loop thay vì mỗi map 1 thread
 *         Giảm từ ~50 threads xuống 4 threads
 */
public class GameLoopManager {

    private static GameLoopManager instance;

    // Thread pool cho game loop
    private ScheduledExecutorService gameLoopExecutor;
    private ScheduledExecutorService playerUpdateExecutor;
    private ScheduledExecutorService systemUpdateExecutor;

    // Performance monitoring
    private AtomicLong totalUpdateTime = new AtomicLong(0);
    private AtomicLong updateCount = new AtomicLong(0);

    // Configuration
    private static final int MAP_UPDATE_INTERVAL = 1000; // 1 giây
    private static final int PLAYER_UPDATE_INTERVAL = 1000; // 1 giây (was 100ms - too fast!)
    private static final int SYSTEM_UPDATE_INTERVAL = 2000; // 2 giây

    private boolean isRunning = false;

    public static GameLoopManager getInstance() {
        if (instance == null) {
            instance = new GameLoopManager();
        }
        return instance;
    }

    /**
     * Khởi tạo game loop manager
     */
    public void initialize() {
        Logger.log("GameLoopManager: Initializing optimized game loop...");

        // Tạo thread pools
        gameLoopExecutor = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "GameLoop-Map");
            t.setDaemon(true);
            return t;
        });

        playerUpdateExecutor = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "GameLoop-Player");
            t.setDaemon(true);
            return t;
        });

        systemUpdateExecutor = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "GameLoop-System");
            t.setDaemon(true);
            return t;
        });

        Logger.log("GameLoopManager: Thread pools created successfully");
    }

    /**
     * Bắt đầu game loop
     */
    public void start() {
        if (isRunning) {
            Logger.log("GameLoopManager: Already running");
            return;
        }

        Logger.log("GameLoopManager: Starting optimized game loop...");

        // Map update loop
        gameLoopExecutor.scheduleAtFixedRate(() -> {
            try {
                updateMaps();
            } catch (Exception e) {
                Logger.logException(GameLoopManager.class, e);
            }
        }, 0, MAP_UPDATE_INTERVAL, TimeUnit.MILLISECONDS);

        // Player update loop
        playerUpdateExecutor.scheduleAtFixedRate(() -> {
            try {
                updatePlayers();
            } catch (Exception e) {
                Logger.logException(GameLoopManager.class, e);
            }
        }, 0, PLAYER_UPDATE_INTERVAL, TimeUnit.MILLISECONDS);

        // System update loop
        systemUpdateExecutor.scheduleAtFixedRate(() -> {
            try {
                updateSystems();
            } catch (Exception e) {
                Logger.logException(GameLoopManager.class, e);
            }
        }, 0, SYSTEM_UPDATE_INTERVAL, TimeUnit.MILLISECONDS);

        isRunning = true;
        Logger.log("GameLoopManager: Game loop started successfully");
    }

    /**
     * Update tất cả maps
     */
    private void updateMaps() {
        long startTime = System.currentTimeMillis();

        try {
            List<Map> maps = Manager.MAPS;
            if (maps != null && !maps.isEmpty()) {
                for (Map map : maps) {
                    if (map != null) {
                        map.update();
                    }
                }
            }
        } catch (Exception e) {
            Logger.logException(GameLoopManager.class, e);
        }

        long updateTime = System.currentTimeMillis() - startTime;
        totalUpdateTime.addAndGet(updateTime);
        updateCount.incrementAndGet();

        // Log performance mỗi 60 giây
        if (updateCount.get() % 60 == 0) {
            long avgTime = totalUpdateTime.get() / updateCount.get();
            Logger.log("GameLoopManager: Map update performance - Avg: " + avgTime + "ms, Total maps: "
                    + Manager.MAPS.size());
        }
    }

    /**
     * Update players (batch processing)
     */
    private void updatePlayers() {
        try {
            // Batch update players để tối ưu performance
            List<Player> players = Client.gI().getPlayers();
            if (players != null && !players.isEmpty()) {
                for (Player player : players) {
                    if (player != null && !player.isBot) {
                        // Throttle player updates to prevent spam
                        long now = System.currentTimeMillis();
                        if (player.lastUpdateTime == 0 || (now - player.lastUpdateTime) >= 1000) {
                            player.update();
                            player.lastUpdateTime = now;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.logException(GameLoopManager.class, e);
        }
    }

    /**
     * Update systems
     */
    private void updateSystems() {
        try {
            // Update các system khác
            // Có thể thêm các system update khác ở đây
        } catch (Exception e) {
            Logger.logException(GameLoopManager.class, e);
        }
    }

    /**
     * Dừng game loop
     */
    public void stop() {
        if (!isRunning) {
            return;
        }

        Logger.log("GameLoopManager: Stopping game loop...");

        isRunning = false;

        if (gameLoopExecutor != null) {
            gameLoopExecutor.shutdown();
        }
        if (playerUpdateExecutor != null) {
            playerUpdateExecutor.shutdown();
        }
        if (systemUpdateExecutor != null) {
            systemUpdateExecutor.shutdown();
        }

        Logger.log("GameLoopManager: Game loop stopped");
    }

    /**
     * Lấy thống kê performance
     */
    public String getPerformanceStats() {
        if (updateCount.get() == 0) {
            return "No updates yet";
        }

        long avgTime = totalUpdateTime.get() / updateCount.get();
        return String.format(
                "GameLoopManager Stats:\n" +
                        "- Total updates: %d\n" +
                        "- Average update time: %dms\n" +
                        "- Total maps: %d\n" +
                        "- Thread pools: 3\n" +
                        "- Status: %s",
                updateCount.get(),
                avgTime,
                Manager.MAPS.size(),
                isRunning ? "Running" : "Stopped");
    }

    /**
     * Force update maps (for testing)
     */
    public void forceUpdateMaps() {
        Logger.log("GameLoopManager: Force updating maps...");
        updateMaps();
    }

    /**
     * Check if running
     */
    public boolean isRunning() {
        return isRunning;
    }
}
