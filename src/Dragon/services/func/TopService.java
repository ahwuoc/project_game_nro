package Dragon.services.func;

import Dragon.jdbc.daos.TopRankingCache;
import Dragon.utils.Logger;

/**
 * TopService đã được tối ưu - không còn chạy background thread
 * Thay vào đó sử dụng TopRankingCache để load on-demand
 */
public class TopService implements Runnable {

    @Override
    public void run() {
        // Service này giờ chỉ khởi tạo cache và dừng lại
        // Không còn chạy background thread gây lag
        Logger.log("TopService: Initializing TopRankingCache...");
        TopRankingCache.getInstance();
        Logger.log("TopService: TopRankingCache initialized. Background thread stopped.");

        // Dừng thread này để không gây lag
        return;
    }

    /**
     * Method để tương thích với code cũ
     * 
     * @deprecated Sử dụng TopRankingCache.getInstance().getTopRanking() thay thế
     */
    @Deprecated
    public static void startBackgroundService() {
        Logger.log("TopService: Background service is disabled to reduce lag. Use TopRankingCache instead.");
    }
}
