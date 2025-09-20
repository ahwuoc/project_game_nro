package Dragon.jdbc.daos;

import Dragon.utils.Logger;
import com.girlkun.database.GirlkunDB;
import Dragon.server.Manager;
import Dragon.models.matches.TOP;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TopRankingCache {

    private static TopRankingCache instance;

    private Map<String, List<TOP>> topCache = new ConcurrentHashMap<>();
    private Map<String, Long> lastUpdateTime = new ConcurrentHashMap<>();

    private static final long CACHE_DURATION = 5 * 60 * 1000;

    // Các loại top
    public static final String TOP_SM = "topSM";
    public static final String TOP_SD = "topSD";
    public static final String TOP_HP = "topHP";
    public static final String TOP_KI = "topKI";
    public static final String TOP_NV = "topNV";
    public static final String TOP_SK = "topSK";
    public static final String TOP_RUBY = "topRUBY";
    public static final String TOP_NHS = "topNHS";
    public static final String TOP_NAP = "topNap";
    public static final String TOP_GAP_THU = "topGapThu";
    public static final String TOP_TRUNG_THU = "topTrungThu";
    public static final String TOP_LEO_THAP = "topLeoThap";

    public static TopRankingCache getInstance() {
        if (instance == null) {
            instance = new TopRankingCache();
        }
        return instance;
    }

    /**
     * Lấy top ranking - tự động load nếu cache hết hạn
     */
    public List<TOP> getTopRanking(String topType) {
        long currentTime = System.currentTimeMillis();
        Long lastUpdate = lastUpdateTime.get(topType);

        // Kiểm tra cache có hết hạn không
        if (lastUpdate == null || (currentTime - lastUpdate) > CACHE_DURATION) {
            Logger.log("TopRankingCache: Loading " + topType + " from database...");
            loadTopFromDatabase(topType);
        }

        return topCache.getOrDefault(topType, new ArrayList<>());
    }

    /**
     * Load top từ database
     */
    private void loadTopFromDatabase(String topType) {
        Connection con = null;
        try {
            con = GirlkunDB.getConnection();
            String query = getQueryForTopType(topType);

            if (query == null) {
                Logger.log("TopRankingCache: Unknown top type: " + topType);
                return;
            }

            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            List<TOP> tops = new ArrayList<>();
            int count = 0;

            while (rs.next()) {
                TOP top = createTopFromResultSet(rs, topType);
                if (top != null) {
                    tops.add(top);
                    count++;
                }
            }

            // Cập nhật cache
            topCache.put(topType, tops);
            lastUpdateTime.put(topType, System.currentTimeMillis());

            Logger.log("TopRankingCache: Successfully loaded " + count + " entries for " + topType);

            rs.close();
            ps.close();

        } catch (Exception e) {
            Logger.logException(TopRankingCache.class, e);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    Logger.logException(TopRankingCache.class, e);
                }
            }
        }
    }

    /**
     * Lấy query SQL cho từng loại top
     */
    private String getQueryForTopType(String topType) {
        switch (topType) {
            case TOP_SM:
                return Manager.queryTopSM;
            case TOP_SD:
                return Manager.queryTopSD;
            case TOP_HP:
                return Manager.queryTopHP;
            case TOP_KI:
                return Manager.queryTopKI;
            case TOP_NV:
                return Manager.queryTopNV;
            case TOP_SK:
                return Manager.queryTopSK;
            case TOP_RUBY:
                return Manager.queryTopRUBY;
            case TOP_NHS:
                return Manager.queryTopNHS;
            case TOP_NAP:
                return Manager.queryTopNap;
            case TOP_GAP_THU:
                return Manager.queryTopGapThu;
            case TOP_TRUNG_THU:
                return Manager.queryTopTrungThu;
            case TOP_LEO_THAP:
                return Manager.queryTopLeoThap;
            default:
                return null;
        }
    }

    /**
     * Tạo TOP object từ ResultSet
     */
    private TOP createTopFromResultSet(ResultSet rs, String topType) {
        try {
            TOP top = TOP.builder()
                    .id_player(rs.getInt("id"))
                    .build();

            // Set thông tin cụ thể theo loại top
            switch (topType) {
                case TOP_SM:
                    top.setPower(rs.getLong("sm"));
                    top.setInfo1("" + rs.getLong("sm") + " Sức Mạnh");
                    top.setInfo2("" + rs.getLong("sm") + " Sức Mạnh");
                    break;
                case TOP_SD:
                    top.setPower(rs.getLong("sd"));
                    top.setInfo1("" + rs.getLong("sd") + " Sức Đánh");
                    top.setInfo2("" + rs.getLong("sd") + " Sức Đánh");
                    break;
                case TOP_HP:
                    top.setPower(rs.getLong("hp"));
                    top.setInfo1("" + rs.getLong("hp") + " HP");
                    top.setInfo2("" + rs.getLong("hp") + " HP");
                    break;
                case TOP_KI:
                    top.setPower(rs.getLong("ki"));
                    top.setInfo1("" + rs.getLong("ki") + " KI");
                    top.setInfo2("" + rs.getLong("ki") + " KI");
                    break;
                case TOP_RUBY:
                    top.setPower(rs.getLong("HONGNGOC"));
                    top.setInfo1("" + rs.getLong("HONGNGOC") + " Hồng Ngọc");
                    top.setInfo2("" + rs.getLong("HONGNGOC") + " Hồng Ngọc");
                    break;
                case TOP_NHS:
                    top.setPower(rs.getLong("nhs"));
                    top.setInfo1("" + rs.getLong("nhs") + " Ngũ Hành Sơn");
                    top.setInfo2("" + rs.getLong("nhs") + " Ngũ Hành Sơn");
                    break;
                case TOP_NAP:
                    top.setPower(rs.getLong("tongnap"));
                    top.setInfo1("" + rs.getLong("tongnap") + " VNĐ");
                    top.setInfo2("" + rs.getLong("tongnap") + " VNĐ");
                    break;
                case TOP_GAP_THU:
                    top.setPower(rs.getLong("p_thu"));
                    top.setInfo1("" + rs.getLong("p_thu") + " Gấp Thư");
                    top.setInfo2("" + rs.getLong("p_thu") + " Gấp Thư");
                    break;
                case TOP_TRUNG_THU:
                    top.setPower(rs.getLong("vua"));
                    top.setInfo1("" + rs.getLong("vua") + " Trung Thu");
                    top.setInfo2("" + rs.getLong("vua") + " Trung Thu");
                    break;
                case TOP_LEO_THAP:
                    top.setPower(rs.getLong("leothap"));
                    top.setInfo1("" + rs.getLong("leothap") + " Leo Tháp");
                    top.setInfo2("" + rs.getLong("leothap") + " Leo Tháp");
                    break;
            }

            // Set thông tin outfit
            top.setHead(rs.getShort("head"));
            top.setBody(rs.getShort("body"));
            top.setLeg(rs.getShort("leg"));
            top.setName(rs.getString("name"));

            return top;

        } catch (Exception e) {
            Logger.logException(TopRankingCache.class, e);
            return null;
        }
    }

    /**
     * Force refresh cache cho một loại top cụ thể
     */
    public void refreshTop(String topType) {
        Logger.log("TopRankingCache: Force refreshing " + topType);
        loadTopFromDatabase(topType);
    }

    /**
     * Refresh tất cả cache
     */
    public void refreshAllCache() {
        Logger.log("TopRankingCache: Refreshing all cache...");
        for (String topType : topCache.keySet()) {
            loadTopFromDatabase(topType);
        }
    }

    /**
     * Lấy thống kê cache
     */
    public String getCacheStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("TopRankingCache Stats:\n");

        for (Map.Entry<String, List<TOP>> entry : topCache.entrySet()) {
            String topType = entry.getKey();
            int size = entry.getValue().size();
            Long lastUpdate = lastUpdateTime.get(topType);
            long timeSinceUpdate = lastUpdate != null ? (System.currentTimeMillis() - lastUpdate) / 1000 : -1;

            stats.append(String.format("- %s: %d entries, updated %d seconds ago\n",
                    topType, size, timeSinceUpdate));
        }

        return stats.toString();
    }

    /**
     * Clear cache
     */
    public void clearCache() {
        topCache.clear();
        lastUpdateTime.clear();
        Logger.log("TopRankingCache: Cache cleared");
    }
}
