package Dragon.services;

import Dragon.jdbc.daos.TopRankingCache;
import Dragon.models.matches.TOP;
import Dragon.utils.Logger;

import java.util.List;

public class TopRankingService {

    private static TopRankingService instance;

    public static TopRankingService getInstance() {
        if (instance == null) {
            instance = new TopRankingService();
        }
        return instance;
    }

    public List<TOP> getTopSucManh() {
        return TopRankingCache.getInstance().getTopRanking(TopRankingCache.TOP_SM);
    }

    public List<TOP> getTopSucDanh() {
        return TopRankingCache.getInstance().getTopRanking(TopRankingCache.TOP_SD);
    }

    public List<TOP> getTopHP() {
        return TopRankingCache.getInstance().getTopRanking(TopRankingCache.TOP_HP);
    }

    public List<TOP> getTopKI() {
        return TopRankingCache.getInstance().getTopRanking(TopRankingCache.TOP_KI);
    }

    public List<TOP> getTopNhiemVu() {
        return TopRankingCache.getInstance().getTopRanking(TopRankingCache.TOP_NV);
    }

    public List<TOP> getTopHongNgoc() {
        return TopRankingCache.getInstance().getTopRanking(TopRankingCache.TOP_RUBY);
    }

    /**
     * Lấy top ngũ hành sơn
     */
    public List<TOP> getTopNguHanhSon() {
        return TopRankingCache.getInstance().getTopRanking(TopRankingCache.TOP_NHS);
    }

    /**
     * Lấy top nạp
     */
    public List<TOP> getTopNap() {
        return TopRankingCache.getInstance().getTopRanking(TopRankingCache.TOP_NAP);
    }

    /**
     * Lấy top gấp thư
     */
    public List<TOP> getTopGapThu() {
        return TopRankingCache.getInstance().getTopRanking(TopRankingCache.TOP_GAP_THU);
    }

    /**
     * Lấy top trung thu
     */
    public List<TOP> getTopTrungThu() {
        return TopRankingCache.getInstance().getTopRanking(TopRankingCache.TOP_TRUNG_THU);
    }

    /**
     * Lấy top leo tháp
     */
    public List<TOP> getTopLeoThap() {
        return TopRankingCache.getInstance().getTopRanking(TopRankingCache.TOP_LEO_THAP);
    }

    /**
     * Force refresh tất cả cache
     */
    public void refreshAllCache() {
        Logger.log("TopRankingService: Refreshing all top ranking cache...");
        TopRankingCache.getInstance().refreshAllCache();
    }

    /**
     * Lấy thống kê cache
     */
    public String getCacheStats() {
        return TopRankingCache.getInstance().getCacheStats();
    }

    /**
     * Clear cache
     */
    public void clearCache() {
        Logger.log("TopRankingService: Clearing top ranking cache...");
        TopRankingCache.getInstance().clearCache();
    }
}
