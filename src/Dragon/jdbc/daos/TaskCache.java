package Dragon.jdbc.daos;

import Dragon.utils.Logger;
import com.girlkun.database.GirlkunDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TaskCache {

    private static TaskCache instance;

    private Map<String, List<TaskRequirement>> requirementsCache = new ConcurrentHashMap<>();
    private Map<String, List<TaskReward>> rewardsCache = new ConcurrentHashMap<>();

    private boolean isInitialized = false;
    private long lastRefreshTime = 0;

    public static TaskCache getInstance() {
        if (instance == null) {
            instance = new TaskCache();
        }
        return instance;
    }
 
    public void initializeCache() {
        Logger.log("TaskCache: Starting cache initialization...");

        try {
            loadAllTaskRequirements();
            loadAllTaskRewards();
            isInitialized = true;
            lastRefreshTime = System.currentTimeMillis();
        } catch (Exception e) {
            Logger.logException(TaskCache.class, e);
        }
    }

    private void loadAllTaskRequirements() {
        Connection con = null;
        try {
            Logger.log("TaskCache: Loading task requirements from database...");
            con = GirlkunDB.getConnection();
            PreparedStatement ps = con.prepareStatement(
                    "SELECT task_main_id, task_sub_id, requirement_type, target_id, target_count, " +
                            "map_restriction, extra_data FROM task_requirements WHERE is_active = 1 " +
                            "ORDER BY task_main_id, task_sub_id");
            ResultSet rs = ps.executeQuery();

            Map<String, List<TaskRequirement>> tempRequirements = new HashMap<>();
            int reqCount = 0;

            while (rs.next()) {
                TaskRequirement req = new TaskRequirement();
                req.taskMainId = rs.getInt("task_main_id");
                req.taskSubId = rs.getInt("task_sub_id");
                req.requirementType = rs.getString("requirement_type");
                req.targetId = rs.getInt("target_id");
                req.targetCount = rs.getInt("target_count");
                req.mapRestriction = rs.getString("map_restriction");
                req.extraData = rs.getString("extra_data");

                String key = req.taskMainId + "_" + req.taskSubId;
                tempRequirements.computeIfAbsent(key, k -> new ArrayList<>()).add(req);
                reqCount++;
            }

            requirementsCache.clear();
            requirementsCache.putAll(tempRequirements);

            rs.close();
            ps.close();

        } catch (Exception e) {
            Logger.logException(TaskCache.class, e);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    Logger.logException(TaskCache.class, e);
                }
            }
        }
    }

    private void loadAllTaskRewards() {
        Connection con = null;
        try {
            Logger.log("TaskCache: Loading task rewards from database...");
            con = GirlkunDB.getConnection();
            PreparedStatement ps = con.prepareStatement(
                    "SELECT req.task_main_id, req.task_sub_id, tr.reward_type, tr.reward_id, tr.reward_quantity, tr.reward_description "
                            + "FROM task_rewards tr "
                            + "JOIN task_requirements req ON req.id = tr.requirement_id "
                            + "WHERE req.is_active = 1 "
                            + "ORDER BY req.task_main_id, req.task_sub_id");
            ResultSet rs = ps.executeQuery();

            Map<String, List<TaskReward>> tempRewards = new HashMap<>();
            int rewardCount = 0;

            while (rs.next()) {
                TaskReward reward = new TaskReward();
                reward.taskMainId = rs.getInt("task_main_id");
                reward.taskSubId = rs.getInt("task_sub_id");
                reward.rewardType = rs.getString("reward_type");
                reward.rewardId = rs.getInt("reward_id");
                reward.rewardQuantity = rs.getLong("reward_quantity");
                reward.rewardDescription = rs.getString("reward_description");

                String key = reward.taskMainId + "_" + reward.taskSubId;
                tempRewards.computeIfAbsent(key, k -> new ArrayList<>()).add(reward);
                rewardCount++;
            }

            rewardsCache.clear();
            rewardsCache.putAll(tempRewards);
            Logger.log("TaskCache: Successfully loaded " + rewardCount + " task rewards");

            rs.close();
            ps.close();

        } catch (Exception e) {
            Logger.logException(TaskCache.class, e);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    Logger.logException(TaskCache.class, e);
                }
            }
        }
    }

    /**
     * Get task requirements từ cache
     */
    public List<TaskRequirement> getTaskRequirements(int taskMainId, int taskSubId, String requirementType) {
        if (!isInitialized) {
            return new ArrayList<>();
        }

        String key = taskMainId + "_" + taskSubId;
        List<TaskRequirement> allReqs = requirementsCache.getOrDefault(key, new ArrayList<>());

        if (requirementType != null) {
            List<TaskRequirement> filteredReqs = new ArrayList<>();
            for (TaskRequirement req : allReqs) {
                if (req.requirementType.equals(requirementType)) {
                    filteredReqs.add(req);
                }
            }
            return filteredReqs;
        }

        return allReqs;
    }

    /**
     * Get task rewards từ cache
     */
    public List<TaskReward> getTaskRewards(int taskMainId, int taskSubId) {
        if (!isInitialized) {
            return new ArrayList<>();
        }

        String key = taskMainId + "_" + taskSubId;
        return rewardsCache.getOrDefault(key, new ArrayList<>());
    }

    /**
     * Refresh cache
     */
    public void refreshCache() {
        Logger.log("TaskCache: Refreshing cache...");
        initializeCache();
    }

    /**
     * Get cache statistics
     */
    public String getCacheStats() {
        if (!isInitialized) {
            return "Cache not initialized";
        }

        int totalReqs = requirementsCache.values().stream().mapToInt(List::size).sum();
        int totalRewards = rewardsCache.values().stream().mapToInt(List::size).sum();

        return String.format("TaskCache Stats - Requirement Groups: %d, Reward Groups: %d, " +
                "Total Requirements: %d, Total Rewards: %d, Last Refresh: %d ms ago",
                requirementsCache.size(), rewardsCache.size(), totalReqs, totalRewards,
                System.currentTimeMillis() - lastRefreshTime);
    }

    /**
     * Clear cache
     */
    public void clearCache() {
        requirementsCache.clear();
        rewardsCache.clear();
        isInitialized = false;
        Logger.log("TaskCache: Cache cleared");
    }

    // Inner classes
    public static class TaskRequirement {
        public int taskMainId;
        public int taskSubId;
        public String requirementType;
        public int targetId;
        public int targetCount;
        public String mapRestriction;
        public String extraData;

        @Override
        public String toString() {
            return String.format("TaskRequirement{task=%d_%d, type=%s, target=%d, count=%d, map=%s}",
                    taskMainId, taskSubId, requirementType, targetId, targetCount, mapRestriction);
        }
    }

    public static class TaskReward {
        public int taskMainId;
        public int taskSubId;
        public String rewardType;
        public int rewardId;
        public long rewardQuantity;
        public String rewardDescription;

        @Override
        public String toString() {
            if ("ITEM".equalsIgnoreCase(rewardType)) {
                return String.format("TaskReward{task=%d_%d, type=%s, id=%d, quantity=%d}",
                        taskMainId, taskSubId, rewardType, rewardId, rewardQuantity);
            } else {
                return String.format("TaskReward{task=%d_%d, type=%s, quantity=%d}",
                        taskMainId, taskSubId, rewardType, rewardQuantity);
            }
        }
    }
}
