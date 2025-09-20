package Dragon.server;

import Dragon.consts.ConstPlayer;
import com.girlkun.database.GirlkunDB;
import Dragon.jdbc.daos.PlayerDAO;
import Dragon.models.item.Item;
import Dragon.models.map.ItemMap;
import Dragon.models.player.Inventory;
import Dragon.models.player.Player;
import Dragon.models.skill.Skill;
import com.girlkun.network.server.GirlkunSessionManager;
import com.girlkun.network.session.ISession;
import Dragon.server.io.MySession;
import Dragon.services.ItemTimeService;
import Dragon.services.Service;
import Dragon.services.func.ChangeMapService;
import Dragon.services.func.SummonDragon;
import Dragon.services.func.TransactionService;
import Dragon.services.InventoryServiceNew;
import Dragon.services.ItemService;
import Dragon.services.MapService;
//import Dragon.services.NgocRongNamecService;
import Dragon.utils.Logger;
import Dragon.utils.SkillUtil;
import Dragon.utils.Util;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import Dragon.models.matches.pvp.DaiHoiVoThuat;
//import Dragon.models.matches.pvp.DaiHoiVoThuatService;

public class Client implements Runnable {

    private static Client i;

    private final Map<Long, Player> players_id = new HashMap<Long, Player>();
    private final Map<Integer, Player> players_userId = new HashMap<Integer, Player>();
    private final Map<String, Player> players_name = new HashMap<String, Player>();
    private final List<Player> players = new ArrayList<>();
    public int id = 1_000_000_000;

    public List<Player> getPlayers() {
        return this.players;
    }

    public static Client gI() {
        if (i == null) {
            i = new Client();
        }
        return i;
    }

    public synchronized void put(Player player) {
        int uid = player.getSession().userId;
        Player existing = this.players_userId.get(uid);
        if (existing != null && existing != player) {
            Logger.log("LOGIN: phát hiện userId=" + uid + " đã có phiên online (playerName=" + existing.name + "), tiến hành kick phiên cũ trước khi thêm phiên mới");
            try {
                kickSession(existing.getSession());
            } catch (Exception ignored) {
            }
        }

        if (!players_id.containsKey(player.id)) {
            this.players_id.put(player.id, player);
        }
        if (!players_name.containsValue(player)) {
            this.players_name.put(player.name, player);
        }
        if (!players_userId.containsKey(uid) || this.players_userId.get(uid) != player) {
            this.players_userId.put(uid, player);
        }
        if (!players.contains(player)) {
            this.players.add(player);
        }
    }

    private void remove(MySession session) {
        if (session.player != null) {
            this.remove(session.player);
            session.player.dispose();
        }
        if (session.joinedGame) {
            session.joinedGame = false;
            try {
                // Skip account table updates for testing - table doesn't exist
                // GirlkunDB.executeUpdate("update account set last_time_logout = ? where id =
                // ?", new Timestamp(System.currentTimeMillis()), session.userId);
                // GirlkunDB.executeUpdate("update account set last_time_off = ? where id = ?",
                // new Timestamp(System.currentTimeMillis()), session.userId);
            } catch (Exception e) {

            }
        }
        ServerManager.gI().disconnect(session);
    }

    private void remove(Player player) {
        this.players_id.remove(player.id);
        this.players_name.remove(player.name);
        this.players_userId.remove(player.getSession().userId);
        this.players.remove(player);
        if (!player.beforeDispose) {
            // DaiHoiVoThuatService.gI(DaiHoiVoThuat.gI().getDaiHoiNow()).removePlayerWait(player);
            // DaiHoiVoThuatService.gI(DaiHoiVoThuat.gI().getDaiHoiNow()).removePlayer(player);
            player.beforeDispose = true;
            player.mapIdBeforeLogout = player.zone.map.mapId;
            // if (player.idNRNM != -1) {
            // ItemMap itemMap = new ItemMap(player.zone, player.idNRNM, 1,
            // player.location.x, player.location.y, -1);
            // Service.gI().dropItemMap(player.zone, itemMap);
            // NgocRongNamecService.gI().pNrNamec[player.idNRNM - 353] = "";
            // NgocRongNamecService.gI().idpNrNamec[player.idNRNM - 353] = -1;
            // player.idNRNM = -1;
            // }
            ChangeMapService.gI().exitMap(player);
            TransactionService.gI().cancelTrade(player);
            if (player.clan != null) {
                player.clan.removeMemberOnline(null, player);
            }
            if (player.itemTime != null && player.itemTime.isUseTDLT) {
                Item tdlt = null;
                try {
                    tdlt = InventoryServiceNew.gI().findItemBag(player, 521);
                } catch (Exception e) {

                }
                if (tdlt != null) {
                    ItemTimeService.gI().turnOffTDLT(player, tdlt);
                }
            }
            if (SummonDragon.gI().playerSummonShenron != null
                    && SummonDragon.gI().playerSummonShenron.id == player.id) {
                SummonDragon.gI().isPlayerDisconnect = true;
            }
            if (player.mobMe != null) {
                player.mobMe.mobMeDie();
            }
            if (player.pet != null) {
                if (player.pet.mobMe != null) {
                    player.pet.mobMe.mobMeDie();
                }
                ChangeMapService.gI().exitMap(player.pet);
            }
            if (player.isClone) {
                ChangeMapService.gI().exitMap(player);
                player = null;
            }
        }
        // Throttle player saves to prevent spam
        if (player != null && !player.isBot) {
            long now = System.currentTimeMillis();
            if (player.lastSaveTime == 0 || (now - player.lastSaveTime) >= 5000) { // Save max every 5 seconds
                PlayerDAO.updatePlayer(player);
                player.lastSaveTime = now;
            }
        }
    }

    public void kickSession(MySession session) {
        if (session != null) {
            try {
                String user = session.uu != null ? session.uu : "<unknown>";
                String ip = session.ipAddress;
                String pid = (session.player != null) ? String.valueOf(session.player.id) : "-1";
                Logger.log("LOGIN: kickSession user=" + user + ", playerId=" + pid + ", ip=" + ip + ", sessionId=" + session.id);
            } catch (Exception ignored) {
            }
            this.remove(session);
            session.disconnect();
        }
    }

    public Player getPlayer(long playerId) {
        return this.players_id.get(playerId);
    }

    public Player getPlayerByUser(int userId) {
        return this.players_userId.get(userId);
    }

    public Player getPlayer(String name) {
        return this.players_name.get(name);
    }

    public void close() {
        Logger.log(Logger.BLACK, "Hệ thống tiến hành lưu dữ liệu người chơi và đăng xuất người chơi khỏi server."
                + players.size() + "\n");
        // while(!GirlkunSessionManager.gI().getSessions().isEmpty()){
        // Logger.error("LEFT PLAYER: " + this.players.size() +
        // ".........................\n");
        // this.kickSession((MySession)
        // GirlkunSessionManager.gI().getSessions().remove(0));
        // }
        while (!players.isEmpty()) {
            this.kickSession((MySession) players.remove(0).getSession());
        }
        Logger.error("Hệ thống lỗi đăng xuất người ch\n");
    }

    public void cloneMySessionNotConnect() {
        Logger.error("BEGIN KICK OUT MySession Not Connect...............................\n");
        Logger.error("COUNT: " + GirlkunSessionManager.gI().getSessions().size());
        if (!GirlkunSessionManager.gI().getSessions().isEmpty()) {
            for (int j = 0; j < GirlkunSessionManager.gI().getSessions().size(); j++) {
                MySession m = (MySession) GirlkunSessionManager.gI().getSessions().get(j);
                if (m.player == null) {
                    this.kickSession((MySession) GirlkunSessionManager.gI().getSessions().remove(j));
                }
            }
        }
        Logger.error("..........................................................SUCCESSFUL\n");
    }

    @Override
    public void run() {
        while (ServerManager.isRunning) {
            try {
                long st = System.currentTimeMillis();
                update();
                Thread.sleep(800 - (System.currentTimeMillis() - st));
            } catch (Exception e) {

            }
        }
    }

    private void update() {
        if (GirlkunSessionManager.gI().getSessions() != null) {
            for (ISession s : GirlkunSessionManager.gI().getSessions()) {
                MySession session = (MySession) s;
                if (session.timeWait > 0) {
                    session.timeWait--;
                    if (session.timeWait == 0) {
                        kickSession(session);
                    }
                }
            }
        }
    }

    public void show(Player player) {
        String txt = "";
        txt += "sessions: " + GirlkunSessionManager.gI().getSessions().size() + "\n";
        txt += "players_id: " + players_id.size() + "\n";
        txt += "players_userId: " + players_userId.size() + "\n";
        txt += "players_name: " + players_name.size() + "\n";
        txt += "players: " + players.size() + "\n";
        Service.gI().sendThongBao(player, txt);
    }

    public void clear() {
        List<Player> z = players;
        for (Player pl : z) {
            if (pl != null) {
                if (pl.isBot) {
                    remove(pl);
                }
            }
        }
    }

    public void createBot(MySession s) {
        String[] name1 = { "le", "hai", "lan", "anh", "long", "hehe" };
        String[] name2 = { "dz", "xinh", "deth", "cute", "cuto", "cutie" };
        String[] name3 = { "vip", "pro", "ga", "top1", "sc1", "vodich" };
        Player pl = new Player();
        Player temp = Client.gI().getPlayerByUser(1);// GodGK.loadById(2275);
        pl.setSession(s);
        s.userId = id;
        pl.id = id;
        id++;
        pl.name = name1[Util.nextInt(name1.length)] + name2[Util.nextInt(name2.length)]
                + name3[Util.nextInt(name3.length)];
        pl.gender = (byte) Util.nextInt(2);
        pl.isBot = true;
        pl.isBoss = false;
        pl.isPet = false;
        pl.nPoint.power = Util.nextInt(200000, 200000000);
        pl.nPoint.power *= Util.nextInt(1, 40);
        pl.nPoint.hpg = 100000;
        pl.nPoint.hpMax = Util.nextInt(200000, 20000000);
        pl.nPoint.hp = pl.nPoint.hpMax / 2;
        pl.nPoint.mpMax = Util.nextInt(2000, 2000000000);
        pl.nPoint.dame = Util.nextInt(2000, 2000000);
        pl.nPoint.stamina = 32000;
        pl.itemTime.isUseTDLT = true;
        pl.typePk = ConstPlayer.NON_PK;
        if (pl.nPoint.hp == 0) {
            Service.gI().hsChar(pl, pl.nPoint.hpMax, pl.nPoint.mpMax);
        }
        // skill
        int[] skillsArr = pl.gender == 0 ? new int[] { 0, 1, 19 }
                : pl.gender == 1 ? new int[] { 12, 17 }
                        : new int[] { 4, 8, 13, 19 };
        for (int j = 0; j < skillsArr.length; j++) {
            Skill skill = SkillUtil.createSkill(skillsArr[j], 7);
            pl.playerSkill.skills.add(skill);
        }
        pl.inventory = new Inventory();
        for (int i = 0; i < 12; i++) {
            pl.inventory.itemsBody.add(ItemService.gI().createItemNull());
        }
        pl.inventory.gold = 2000000000;
        pl.inventory.itemsBody.set(5, Manager.CAITRANG.get(Util.nextInt(0, Manager.CAITRANG.size() - 1)));
        pl.location.y = 300;
        pl.zone = MapService.gI().getMapCanJoin(pl, (Util.nextInt(0, 215)), (Util.nextInt(0, 10)));
        if (pl.zone == null) {
            return;
        }
        if (pl.zone.map == null) {
            return;
        }
        pl.location.x = 200;// temp.location.x + Util.nextInt(-400,400);
        pl.zone.addPlayer(pl);
        pl.zone.load_Me_To_Another(pl);
        Client.gI().put(pl);
    }
}
