package Dragon.jdbc.daos;

import Dragon.card.Card;
import Dragon.card.OptionCard;
import com.girlkun.database.GirlkunDB;
import com.girlkun.result.GirlkunResultSet;
import Dragon.consts.ConstPlayer;
import Dragon.data.DataGame;
import Dragon.models.Template.ArchivementTemplate;
//import Dragon.models.ThanhTich.ThanhTich;
//import Dragon.models.ThanhTich.ThanhTichPlayer;
import Dragon.models.clan.Clan;
import Dragon.models.clan.ClanMember;
import Dragon.models.item.Item;
import Dragon.models.item.ItemTime;
import Dragon.models.npc.specialnpc.MabuEgg;
import Dragon.models.npc.specialnpc.BillEgg;
import Dragon.models.npc.specialnpc.MagicTree;
import Dragon.models.player.Enemy;
import Dragon.models.player.Friend;
import Dragon.models.player.Fusion;
import Dragon.models.player.Pet;
import Dragon.models.player.Player;
import Dragon.models.skill.Skill;
import Dragon.models.task.TaskMain;
import com.girlkun.network.server.GirlkunSessionManager;
import com.girlkun.network.session.ISession;
import Dragon.server.Client;
import Dragon.server.Manager;
import Dragon.server.ServerManager;
import Dragon.server.ServerNotify;
import Dragon.server.io.MySession;
import Dragon.server.model.AntiLogin;
import Dragon.services.ClanService;
import Dragon.services.IntrinsicService;
import Dragon.services.ItemService;
import Dragon.services.MapService;
import Dragon.services.Service;
import Dragon.services.TaskService;
import Dragon.utils.Logger;
import Dragon.utils.SkillUtil;
import Dragon.utils.TimeUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import Dragon.utils.Util;
import java.util.Calendar;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class GodGK {

    public static List<OptionCard> loadOptionCard(JSONArray json) {
        List<OptionCard> ops = new ArrayList<>();
        try {
            for (int i = 0; i < json.size(); i++) {
                JSONObject ob = (JSONObject) json.get(i);
                if (ob != null) {
                    ops.add(new OptionCard(Integer.parseInt(ob.get("id").toString()),
                            Integer.parseInt(ob.get("param").toString()), Byte.parseByte(ob.get("active").toString())));
                }
            }
        } catch (Exception e) {

        }
        return ops;
    }

    public static Boolean baotri = false;

    public static synchronized Player login(MySession session, AntiLogin al) {
        Player player = null;
        GirlkunResultSet rs = null;
        try {
            session.userId = Math.abs(session.uu.hashCode()) % 1000000 + 1; // Generate unique ID
            session.isAdmin = true;
            session.lastTimeLogout = System.currentTimeMillis() - 86400000; // 1 day ago
            session.lastTimeOff = System.currentTimeMillis() - 86400000;
            session.actived = true;
            session.mtvgtd = false;
            session.vip1d = false;
            session.vip2d = false;
            session.vip3d = false;
            session.vip4d = false;
            session.vip5d = false;
            session.vip6d = false;
            session.tongnap = 0;
            session.vnd = 0;
            session.mocnap = 0;
            session.gioithieu = 0;
            session.goldBar = 0;
            session.bdPlayer = 0.0;

            // Skip ban check and login time check for testing
            al.reset();

            // Check if player is already online by userId (chính xác hơn username)
            Player existingPlayer = Client.gI().getPlayerByUser(session.userId);
            if (existingPlayer != null) {
                Logger.log("LOGIN: userId=" + session.userId
                        + " đã online (GodGK), tiến hành kick session cũ (playerName=" + existingPlayer.name + ")");
                existingPlayer.getSession().disconnect();
                // Wait a bit for cleanup
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // Try to load player data
            rs = GirlkunDB.executeQuery("select * from player where account_id = ? limit 1", session.userId);
            if (!rs.first()) {
                Service.gI().switchToCreateChar(session);
                DataGame.sendDataItemBG(session);
                DataGame.sendVersionGame(session);
                DataGame.sendTileSetInfo(session);
                Service.gI().sendMessage(session, -93, "1630679752231_-93_r");
                DataGame.updateData(session);
            } else {
                // Load existing player
                player = PlayerDataLoader.loadPlayer(rs, PlayerDataLoader.LoadType.FULL_LOGIN);

                // Set offline time
                long now = System.currentTimeMillis();
                long thoiGianOffline = now - session.lastTimeOff;
                player.timeoff = thoiGianOffline /= 60000;
                player.totalPlayerViolate = 0;

                // Update login time (skip database update for testing)
                // GirlkunDB.executeUpdate("update account set last_time_login = '" + new
                // Timestamp(System.currentTimeMillis()) + "', ip_address = '" +
                // session.ipAddress + "' where id = " + session.userId);
            }
            // } else {
            // Service.gI().sendThongBaoOK(session, "Thông tin tài khoản hoặc mật khẩu không
            // chính xác");
            // al.wrong();
            // }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error(session.uu);
            player.dispose();
            player = null;
            Logger.logException(GodGK.class, e);
        } finally {
            if (rs != null) {
                rs.dispose();
            }
        }
        return player;
    }

    public static void SetPlayer(Player pl) {
        // if(pl == null)
        // {
        // return;
        // }
        // if(!pl.getSession().isAdmin)
        // { if(pl.nPoint.limitPower > 11)
        // {
        // pl.nPoint.limitPower = 11;
        // }
        // if(pl.nPoint.power > 130000000000L)
        // {
        // pl.nPoint.power = 130000000000L;
        // }
        // if(pl.nPoint.dameg > 27500)
        // {
        // pl.nPoint.power = 27500;
        // }
        // if(pl.nPoint.hpg > 630000)
        // {
        // pl.nPoint.hpg = 630000;
        // }
        // if(pl.nPoint.mpg > 630000)
        // {
        // pl.nPoint.mpg = 630000;
        // }}
    }

    public static Player loadById(int id) {
        Player player = null;
        GirlkunResultSet rs = null;
        if (Client.gI().getPlayer(id) != null) {
            player = Client.gI().getPlayer(id);
            return player;
        }
        try {
            rs = GirlkunDB.executeQuery("select * from player where id = ? limit 1", id);
            if (rs.first()) {
                // Sử dụng PlayerDataLoader thay thế toàn bộ duplicate code
                player = PlayerDataLoader.loadPlayer(rs, PlayerDataLoader.LoadType.FULL_BY_ID);
            }
        } catch (Exception e) {

            player.dispose();
            player = null;
            Logger.logException(GodGK.class, e);
        } finally {
            if (rs != null) {
                rs.dispose();
            }
        }
        return player;
    }

    public static Player loadByIdSieuHang(int id) {
        Player player = null;
        GirlkunResultSet rs = null;
        if (Client.gI().getPlayer(id) != null) {
            player = Client.gI().getPlayer(id);
            return player;
        }
        try {
            rs = GirlkunDB.executeQuery("select * from player where id = ? limit 1", id);
            if (rs.first()) {
                player = PlayerDataLoader.loadPlayer(rs, PlayerDataLoader.LoadType.SIEU_HANG_ONLY);
            }
        } catch (Exception e) {
            e.printStackTrace();
            player.dispose();
            player = null;
            Logger.logException(GodGK.class, e);
        } finally {
            if (rs != null) {
                rs.dispose();
            }
        }
        return player;
    }
}
