package Dragon.server;

import Dragon.card.OptionCard;
import Dragon.card.RadarCard;
import Dragon.card.RadarService;
import com.girlkun.database.GirlkunDB;
import Dragon.consts.ConstPlayer;
import Dragon.consts.ConstMap;
import Dragon.data.DataGame;
import Dragon.jdbc.daos.GodGK;
import Dragon.jdbc.daos.ShopDAO;
import Dragon.kygui.ItemKyGui;
import Dragon.kygui.ShopKyGuiManager;
import Dragon.kygui.ShopKyGuiService;
import Dragon.models.Template.*;
import Dragon.models.clan.Clan;
import Dragon.models.clan.ClanMember;
import Dragon.models.intrinsic.Intrinsic;
import Dragon.models.item.Item;
import Dragon.models.map.WayPoint;
import Dragon.models.map.gas.TopGas;
import Dragon.models.matches.TOP;
//import Dragon.models.matches.pvp.DaiHoiVoThuat;
import Dragon.models.npc.Npc;
import Dragon.models.npc.NpcFactory;
import Dragon.models.player.Fusion;
import Dragon.models.player.Pet;
import Dragon.models.player.Player;
import Dragon.models.player.Referee;
import Dragon.models.player.Referee1;
//import Dragon.models.player.Yajiro;
//import Dragon.models.player.Admin;
import Dragon.models.reward.ItemMobReward;
import Dragon.models.reward.ItemOptionMobReward;
import Dragon.models.reward.MobReward;
import Dragon.models.shop.Shop;
import Dragon.models.skill.NClass;
import Dragon.models.skill.Skill;
import Dragon.models.task.SideTaskTemplate;
import Dragon.models.task.SubTaskMain;
import Dragon.models.task.TaskMain;
import Dragon.services.ItemService;
import Dragon.services.MapService;
import Dragon.services.Service;
import Dragon.services.TaskService;
import Dragon.services.func.TopService;
import Dragon.utils.Logger;
import Dragon.utils.TimeUtil;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import Dragon.utils.Util;
import com.girlkun.result.GirlkunResultSet;
import java.time.Instant;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Manager {

    private static Manager i;

    public static byte SERVER = 1;
    public static byte SECOND_WAIT_LOGIN = 0;
    public static int MAX_PER_IP = 2;
    public static int MAX_PLAYER = 10000;
    public static byte RATE_EXP_SERVER = 1;
    public static boolean LOCAL = false;
    public static int TotalPet = 0;
    public static int TotalCaiTrang = 0;
    public static int TotalFlag = 0;
    public static MapTemplate[] MAP_TEMPLATES;
    public static String DOMAIN = "";
    public static final List<ArchivementTemplate> Archivement_TEMPLATES = new ArrayList<>();
    public static final List<Dragon.models.map.Map> MAPS = new ArrayList<>();
    public static final List<ItemOptionTemplate> ITEM_OPTION_TEMPLATES = new ArrayList<>();
    public static final Map<Integer, MobReward> MOB_REWARDS = new HashMap<>();
    public static final List<ItemLuckyRound> LUCKY_ROUND_REWARDS = new ArrayList();
    public static final Map<String, Byte> IMAGES_BY_NAME = new HashMap<String, Byte>();
    public static final List<ItemTemplate> ITEM_TEMPLATES = new ArrayList<>();
    public static final List<MobTemplate> MOB_TEMPLATES = new ArrayList<>();
    public static final List<NpcTemplate> NPC_TEMPLATES = new ArrayList<>();
    public static final List<String> CAPTIONS = new ArrayList<>();
    public static final List<TaskMain> TASKS_TEMPLATE = new ArrayList<>();
    public static final List<SideTaskTemplate> SIDE_TASKS_TEMPLATE = new ArrayList<>();
    public static final List<Intrinsic> INTRINSICS = new ArrayList<>();
    public static final List<Intrinsic> INTRINSIC_TD = new ArrayList<>();
    public static final List<Intrinsic> INTRINSIC_NM = new ArrayList<>();
    public static final List<Intrinsic> INTRINSIC_XD = new ArrayList<>();
    public static final List<HeadAvatar> HEAD_AVATARS = new ArrayList<>();
    public static final List<FlagBag> FLAGS_BAGS = new ArrayList<>();
    public static final List<NClass> NCLASS = new ArrayList<>();
    public static final List<Npc> NPCS = new ArrayList<>();
    public static List<Shop> SHOPS = new ArrayList<>();
    public static final List<Clan> CLANS = new ArrayList<>();
    public static final List<TopGas> TopGas = new ArrayList<>();
    public static final List<String> NOTIFY = new ArrayList<>();
    public static final List<Item> RUBY_REWARDS = new ArrayList<>();
    public static final List<ArrHead2Frames> ARR_HEAD_2_FRAMES = new ArrayList<>();
    public static final List<Item> CAITRANG = new ArrayList<>();
    public static final String queryTopSM = "SELECT *, CAST(JSON_EXTRACT(data_point, '$[1]') AS UNSIGNED) AS sm FROM player ORDER BY sm DESC LIMIT 20;";
    public static final String queryTopSD = "SELECT id, CAST( split_str(data_point,',',8)  AS UNSIGNED) AS sd FROM player ORDER BY CAST( split_str(data_point,',',8)  AS UNSIGNED) DESC LIMIT 20;";
    public static final String queryTopHP = "SELECT id, CAST( split_str(data_point,',',14) AS UNSIGNED) AS hp FROM player ORDER BY CAST( split_str(data_point,',',14) AS UNSIGNED) DESC LIMIT 20;";
    public static final String queryTopKI = "SELECT id, CAST( split_str(data_point,',',7) AS UNSIGNED) AS ki FROM player ORDER BY CAST( split_str(data_point,',',7) AS UNSIGNED) DESC LIMIT 20;";
    public static final String queryTopNV = "SELECT id, JSON_EXTRACT(data_task, '$[0]') AS second_value\n"
            + "						FROM player\n"
            + "						ORDER BY CAST(JSON_EXTRACT(data_task, '$[0]') AS UNSIGNED) DESC\n"
            + "						LIMIT 20;";
    public static final String queryTopSK = "SELECT id, CAST( split_str( data_inventory,',',5) AS UNSIGNED) AS event FROM player ORDER BY CAST( split_str( data_inventory,',',5) AS UNSIGNED) DESC LIMIT 20;";
    public static final String queryTopRUBY = "SELECT id, CAST( split_str( data_inventory,',',3) AS UNSIGNED) AS HONGNGOC FROM player ORDER BY CAST( split_str( data_inventory,',',3) AS UNSIGNED) DESC LIMIT 10;";
    public static final String queryTopNHS = "SELECT id, CAST( NguHanhSonPoint AS UNSIGNED) AS nhs FROM player ORDER BY CAST( NguHanhSonPoint AS UNSIGNED) DESC LIMIT 20;";
    public static final String queryTopGapThu = "SELECT id, CAST( point_gapthu AS UNSIGNED) AS p_thu FROM player ORDER BY CAST( point_gapthu AS UNSIGNED) DESC LIMIT 20";
    public static final String queryTopTrungThu = "SELECT id, CAST( point_vnd AS UNSIGNED) AS vua FROM player ORDER BY CAST( point_vnd AS UNSIGNED) DESC LIMIT 20";
    public static final String queryTopLeoThap = "SELECT id, CAST( capboss AS UNSIGNED) AS leothap FROM player ORDER BY CAST( capboss AS UNSIGNED) DESC LIMIT 20";
    public static final String queryTopNap = "SELECT p.id, a.tongnap FROM player p JOIN account a ON p.account_id = a.id ORDER BY a.tongnap DESC LIMIT 20;";
    public static List<TOP> topSM;
    public static List<TOP> topSD;
    public static List<TOP> topHP;
    public static List<TOP> topKI;
    public static List<TOP> topNV;
    public static List<TOP> topSK;
    public static List<TOP> topRUBY;
    public static List<TOP> topNHS;
    public static List<TOP> topNap;
    public static List<TOP> TopGapThu;
    public static List<TOP> TopTrungThu;
    public static List<TOP> TopLeoThap;
    public static List<TOP> topSieuHang;
    public static long timeRealTop = 0;
    public static final short[] itemIds_TL = { 555, 557, 559, 556, 558, 560, 562, 564, 566, 563, 565, 567, 561, 555,
            557, 559, 556, 558, 560, 563, 565, 567, 555, 557, 559, 556, 558, 560, 563, 565, 567 };
    public static final short[] itemIds_HuyDiet = { 650, 651, 652, 653, 654, 655, 656, 657, 658, 659, 660, 661, 662 };
    public static final short[] itemAVATAR_BLACK = { 1124, 1125 };
    public static final short[] itemIds_NR_SB = { 16, 17, 934, 934 };
    public static final short[] itemIds_CB = { 1991, 1992 };
    public static final short[] itemDC12 = { 233, 237, 241, 245, 249, 253, 257, 261, 265, 269, 273, 277 };
    public static final short[] itemmabu = { 1545, 568 };

    // SKH THƯỜNG
    public static final short[] aotdt = { 0 };
    public static final short[] quantdt = { 6 };
    public static final short[] gangtdt = { 21 };
    public static final short[] giaytdt = { 27 };
    public static final short[] aoxdt = { 2 };
    public static final short[] quanxdt = { 8 };
    public static final short[] gangxdt = { 23 };
    public static final short[] giayxdt = { 29 };
    public static final short[] aonmt = { 1 };
    public static final short[] quannmt = { 7 };
    public static final short[] gangnmt = { 22 };
    public static final short[] giaynmt = { 28 };
    public static final short[] radaSKH = { 12 };
    // SKH VIP
    public static final short[] aotd = { 138, 139, 230, 231, 232, 233, 555 };
    public static final short[] quantd = { 142, 143, 242, 243, 244, 245, 556 };
    public static final short[] gangtd = { 146, 147, 254, 255, 256, 257, 562 };
    public static final short[] giaytd = { 150, 151, 266, 267, 268, 269, 563 };
    public static final short[] aoxd = { 170, 171, 238, 239, 240, 241, 559 };
    public static final short[] quanxd = { 174, 175, 250, 251, 252, 253, 560 };
    public static final short[] gangxd = { 178, 179, 262, 263, 264, 265, 566 };
    public static final short[] giayxd = { 182, 183, 274, 275, 276, 277, 567 };
    public static final short[] aonm = { 154, 155, 234, 235, 236, 237, 557 };
    public static final short[] quannm = { 158, 159, 246, 247, 248, 249, 558 };
    public static final short[] gangnm = { 162, 163, 258, 259, 260, 261, 564 };
    public static final short[] giaynm = { 166, 167, 270, 271, 272, 273, 565 };
    public static final short[] radaSKHVip = { 186, 187, 278, 279, 280, 281, 561 };
    public static final short[][][] doSKH = { { aotdt, quantdt, gangtdt, giaytdt, radaSKH },
            { aonmt, quannmt, gangnmt, giaynmt, radaSKH }, { aoxdt, quanxdt, gangxdt, giayxdt, radaSKH } };

    public static final short[][][] doSKHVip = { { aotd, quantd, gangtd, giaytd }, { aonm, quannm, gangnm, giaynm },
            { aoxd, quanxd, gangxd, giayxd } };

    public static Manager gI() {
        if (i == null) {
            i = new Manager();
        }
        return i;
    }

    private Manager() {
        try {
            loadProperties();
        } catch (IOException e) {
            System.err.print("\nError at 298\n");
            e.printStackTrace();
            System.exit(0);

        }
        this.loadDatabase();

        Dragon.utils.Logger.log("Manager: Initializing Mob Reward Cache...");
        Dragon.jdbc.daos.MobRewardCache.getInstance().initializeCache();

        Dragon.utils.Logger.log("Manager: Initializing Boss Reward Cache...");
        Dragon.jdbc.daos.BossRewardCache.getInstance().initializeCache();

        Dragon.utils.Logger.log("Manager: Initializing Gift Code Cache...");
        Dragon.jdbc.daos.GiftCodeCache.getInstance().initializeCache();

        Dragon.utils.Logger.log("Manager: Initializing Task Cache...");
        Dragon.jdbc.daos.TaskCache.getInstance().initializeCache();

        Dragon.utils.Logger.log("Manager: Initializing Top Ranking Cache...");
        Dragon.jdbc.daos.TopRankingCache.getInstance();

        NpcFactory.createNpcConMeo();
        Dragon.nam.TamBaoService.loadItem();
        Dragon.nam.GachaService.loadItem();
        NpcFactory.createNpcRongThieng();
        this.initMap();

        // TopService đã được tối ưu - không còn chạy background thread
        Dragon.utils.Logger.log("Manager: TopService background thread disabled to reduce lag");
    }

    public boolean HoTroNhiemVu() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        if (dayOfWeek == Calendar.TUESDAY || dayOfWeek == Calendar.THURSDAY
                || dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            if ((hourOfDay >= 8 && hourOfDay <= 11) || (hourOfDay >= 20 && hourOfDay <= 23)) {
                return true;
            }
        }
        return false;
    }

    private void initMap() {
        int[][] tileTyleTop = readTileIndexTileType(ConstMap.TILE_TOP);
        for (MapTemplate mapTemp : MAP_TEMPLATES) {
            int[][] tileMap = readTileMap(mapTemp.id);
            int[] tileTop = tileTyleTop[mapTemp.tileId - 1];
            Dragon.models.map.Map map = new Dragon.models.map.Map(mapTemp.id,
                    mapTemp.name, mapTemp.planetId, mapTemp.tileId, mapTemp.bgId,
                    mapTemp.bgType, mapTemp.type, tileMap, tileTop,
                    mapTemp.zones,
                    mapTemp.maxPlayerPerZone, mapTemp.wayPoints);
            MAPS.add(map);
            map.initMob(mapTemp.mobTemp, mapTemp.mobLevel, mapTemp.mobHp, mapTemp.mobX, mapTemp.mobY);
            map.initNpc(mapTemp.npcId, mapTemp.npcX, mapTemp.npcY);

            // KHÔNG tạo thread riêng cho mỗi map nữa - sử dụng GameLoopManager
            // new Thread(map, "Update map " + map.mapName).start();
            // ServerManager.gI().threadMap++;
        }

        // Khởi tạo GameLoopManager để update tất cả maps
        Logger.log("Manager: Initializing GameLoopManager...");
        GameLoopManager.getInstance().initialize();
        GameLoopManager.getInstance().start();
        Logger.log("Manager: GameLoopManager started - Maps will be updated by thread pool");

        Referee r = new Referee();
        r.initReferee();
        Referee1 vodai = new Referee1();
        vodai.initReferee1();
    }

    public static void main(String[] args) {
        loadPart();
    }

    public static void loadPart() {
        JSONValue jv = new JSONValue();
        JSONArray dataArray = null;
        JSONObject dataObject = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try (Connection con = GirlkunDB.getConnection();) {
            // load part
            ps = con.prepareStatement("select * from part");
            rs = ps.executeQuery();
            List<Part> parts = new ArrayList<>();
            while (rs.next()) {
                Part part = new Part(rs.getShort("id"), rs.getByte("type"));

                dataArray = (JSONArray) jv.parse(rs.getString("data").replaceAll("\\\"", ""));

                for (int j = 0; j < dataArray.size(); j++) {
                    JSONArray pd = (JSONArray) jv.parse(String.valueOf(dataArray.get(j)));
                    part.pi[j] = (new PartDetail(Short.parseShort(String.valueOf(pd.get(0))),
                            Byte.parseByte(String.valueOf(pd.get(1))),
                            Byte.parseByte(String.valueOf(pd.get(2)))));
                    pd.clear();
                }
                parts.add(part);
                dataArray.clear();
            }
            DataOutputStream dos = new DataOutputStream(new FileOutputStream("data/girlkun/update_data/part"));
            dos.writeShort(parts.size());
            for (int i = 0; i < parts.size(); i++) {
                final Part part = parts.get(i);
                // Controller.debug(i + " =>> LOAD PARTT >>> " + part.id + ", part.type: " +
                // part.type);
                dos.writeByte(part.type);
                for (PartDetail partDetail : part.pi) {
                    dos.writeShort(partDetail.iconId);
                    dos.writeByte(partDetail.dx);
                    dos.writeByte(partDetail.dy);
                }
            }
            dos.flush();
            dos.close();
        } catch (Exception e) {
            System.err.print("\nError at 299\n");
            e.printStackTrace();
        }
    }

    private void loadDatabase() {
        long st = System.currentTimeMillis();
        JSONValue jv = new JSONValue();
        JSONArray dataArray = null;
        JSONObject dataObject = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try (Connection con = GirlkunDB.getConnection();) {
            ps = con.prepareStatement("select * from part");
            rs = ps.executeQuery();
            List<Part> parts = new ArrayList<>();
            while (rs.next()) {
                Part part = new Part(rs.getShort("id"), rs.getByte("type"));

                dataArray = (JSONArray) jv.parse(rs.getString("data").replaceAll("\\\"", ""));

                for (int j = 0; j < dataArray.size(); j++) {
                    JSONArray pd = (JSONArray) jv.parse(String.valueOf(dataArray.get(j)));
                    part.pi[j] = (new PartDetail(Short.parseShort(String.valueOf(pd.get(0))),
                            Byte.parseByte(String.valueOf(pd.get(1))),
                            Byte.parseByte(String.valueOf(pd.get(2)))));
                    pd.clear();
                }
                parts.add(part);
                dataArray.clear();
            }
            DataOutputStream dos = new DataOutputStream(new FileOutputStream("data/girlkun/update_data/part"));
            dos.writeShort(parts.size());
            for (Part part : parts) {
                dos.writeByte(part.type);
                for (PartDetail partDetail : part.pi) {
                    dos.writeShort(partDetail.iconId);
                    dos.writeByte(partDetail.dx);
                    dos.writeByte(partDetail.dy);
                }
            }
            dos.flush();
            dos.close();
            Logger.log(Logger.GREEN, "[DONE] Part[" + parts.size() + "]\n");

            // load small version
            ps = con.prepareStatement("select count(id) from small_version");
            rs = ps.executeQuery();
            List<byte[]> smallVersion = new ArrayList<>();
            if (rs.first()) {
                int size = rs.getInt(1);
                for (int i = 0; i < 4; i++) {
                    smallVersion.add(new byte[size]);
                }
            }
            ps = con.prepareStatement("select * from small_version order by id");
            rs = ps.executeQuery();
            int index = 0;
            while (rs.next()) {
                smallVersion.get(0)[index] = rs.getByte("x1");
                smallVersion.get(1)[index] = rs.getByte("x2");
                smallVersion.get(2)[index] = rs.getByte("x3");
                smallVersion.get(3)[index] = rs.getByte("x4");
                index++;
            }
            for (int i = 0; i < 4; i++) {
                dos = new DataOutputStream(
                        new FileOutputStream("data/girlkun/data_img_version/x" + (i + 1) + "/img_version"));
                dos.writeShort(smallVersion.get(i).length);
                for (int j = 0; j < smallVersion.get(i).length; j++) {
                    dos.writeByte(smallVersion.get(i)[j]);
                }
                dos.flush();
                dos.close();
            }

            // load clan
            ps = con.prepareStatement("select * from clan_sv" + SERVER);
            rs = ps.executeQuery();
            while (rs.next()) {
                Clan clan = new Clan();
                clan.id = rs.getInt("id");
                clan.name = rs.getString("name");
                clan.slogan = rs.getString("slogan");
                clan.imgId = rs.getByte("img_id");
                clan.powerPoint = rs.getLong("power_point");
                clan.maxMember = rs.getByte("max_member");
                clan.capsuleClan = rs.getInt("clan_point");
                clan.level = rs.getByte("level");
                clan.createTime = (int) (rs.getTimestamp("create_time").getTime() / 1000);
                dataArray = (JSONArray) jv.parse(rs.getString("members"));
                for (int i = 0; i < dataArray.size(); i++) {
                    dataObject = (JSONObject) jv.parse(String.valueOf(dataArray.get(i)));
                    ClanMember cm = new ClanMember();
                    cm.clan = clan;
                    cm.id = Integer.parseInt(String.valueOf(dataObject.get("id")));
                    cm.name = String.valueOf(dataObject.get("name"));
                    cm.head = Short.parseShort(String.valueOf(dataObject.get("head")));
                    cm.body = Short.parseShort(String.valueOf(dataObject.get("body")));
                    cm.leg = Short.parseShort(String.valueOf(dataObject.get("leg")));
                    cm.role = Byte.parseByte(String.valueOf(dataObject.get("role")));
                    cm.donate = Integer.parseInt(String.valueOf(dataObject.get("donate")));
                    cm.receiveDonate = Integer.parseInt(String.valueOf(dataObject.get("receive_donate")));
                    cm.memberPoint = Integer.parseInt(String.valueOf(dataObject.get("member_point")));
                    cm.clanPoint = Integer.parseInt(String.valueOf(dataObject.get("clan_point")));
                    cm.joinTime = Integer.parseInt(String.valueOf(dataObject.get("join_time")));
                    cm.timeAskPea = Long.parseLong(String.valueOf(dataObject.get("ask_pea_time")));
                    try {
                        cm.powerPoint = Long.parseLong(String.valueOf(dataObject.get("power")));
                    } catch (Exception e) {
                        System.err.print("\nError at 300\n");
                        e.printStackTrace();
                    }
                    clan.addClanMember(cm);
                }
                dataArray = (JSONArray) jv.parse(rs.getString("createtimeLong"));
                clan.createTimeLong = Long.parseLong(String.valueOf(dataArray.get(0)));
                clan.lastTimeOpenDoanhTrai = Long.parseLong(String.valueOf(dataArray.get(1)));
                dataArray.clear();
                dataArray = (JSONArray) jv.parse(rs.getString("DataGas"));
                clan.levelKhiGas = Integer.parseInt(String.valueOf(dataArray.get(0)));
                clan.TimeDoneKhiGas = Long.parseLong(String.valueOf(dataArray.get(1)));
                clan.SoLanDiKhiGas = Integer.parseInt(String.valueOf(dataArray.get(2)));
                CLANS.add(clan);
                TopGas.add(new TopGas(clan));
                dataArray.clear();
                dataObject.clear();
            }

            ps = con.prepareStatement("select id from clan_sv" + SERVER + " order by id desc limit 1");
            rs = ps.executeQuery();
            if (rs.first()) {
                Clan.NEXT_ID = rs.getInt("id") + 1;
            }

            Logger.log(Logger.GREEN, "[DONE] CLAN(" + CLANS.size() + ") (" + Clan.NEXT_ID + ")\n");

            ps = con.prepareStatement("select * from dhvt_template");
            rs = ps.executeQuery();

            // load skill
            ps = con.prepareStatement("select * from skill_template order by nclass_id, slot");
            rs = ps.executeQuery();
            byte nClassId = -1;
            NClass nClass = null;
            while (rs.next()) {
                byte id = rs.getByte("nclass_id");
                if (id != nClassId) {
                    nClassId = id;
                    nClass = new NClass();
                    nClass.name = id == ConstPlayer.TRAI_DAT ? "Trái Đất" : id == ConstPlayer.NAMEC ? "Namếc" : "Xayda";
                    nClass.classId = nClassId;
                    NCLASS.add(nClass);
                }
                SkillTemplate skillTemplate = new SkillTemplate();
                skillTemplate.classId = nClassId;
                skillTemplate.id = rs.getByte("id");
                skillTemplate.name = rs.getString("name");
                skillTemplate.maxPoint = rs.getByte("max_point");
                skillTemplate.manaUseType = rs.getByte("mana_use_type");
                skillTemplate.type = rs.getByte("type");
                skillTemplate.iconId = rs.getShort("icon_id");
                skillTemplate.damInfo = rs.getString("dam_info");
                nClass.skillTemplatess.add(skillTemplate);

                dataArray = (JSONArray) jv.parse(
                        rs.getString("skills")
                                .replaceAll("\\[\"", "[")
                                .replaceAll("\"\\[", "[")
                                .replaceAll("\"\\]", "]")
                                .replaceAll("\\]\"", "]")
                                .replaceAll("\\}\",\"\\{", "},{"));
                for (int j = 0; j < dataArray.size(); j++) {
                    JSONObject dts = (JSONObject) jv.parse(String.valueOf(dataArray.get(j)));
                    Skill skill = new Skill();
                    skill.template = skillTemplate;
                    skill.skillId = Short.parseShort(String.valueOf(dts.get("id")));
                    skill.point = Byte.parseByte(String.valueOf(dts.get("point")));
                    skill.powRequire = Long.parseLong(String.valueOf(dts.get("power_require")));
                    skill.manaUse = Integer.parseInt(String.valueOf(dts.get("mana_use")));
                    skill.coolDown = Integer.parseInt(String.valueOf(dts.get("cool_down")));
                    skill.dx = Integer.parseInt(String.valueOf(dts.get("dx")));
                    skill.dy = Integer.parseInt(String.valueOf(dts.get("dy")));
                    skill.maxFight = Integer.parseInt(String.valueOf(dts.get("max_fight")));
                    skill.damage = Short.parseShort(String.valueOf(dts.get("damage")));
                    skill.price = Short.parseShort(String.valueOf(dts.get("price")));
                    skill.moreInfo = String.valueOf(dts.get("info"));
                    skillTemplate.skillss.add(skill);
                }
            }
            Logger.log(Logger.GREEN, "[DONE] SKILL(" + NCLASS.size() + ")\n");

            // load head avatar
            ps = con.prepareStatement("select * from head_avatar");
            rs = ps.executeQuery();
            while (rs.next()) {
                HeadAvatar headAvatar = new HeadAvatar(rs.getInt("head_id"), rs.getInt("avatar_id"));
                HEAD_AVATARS.add(headAvatar);
            }
            Logger.log(Logger.GREEN, "[DONE] HEAD AVATAR(" + HEAD_AVATARS.size() + ")\n");

            // load flag bag
            ps = con.prepareStatement("select * from flag_bag");
            rs = ps.executeQuery();
            while (rs.next()) {
                FlagBag flagBag = new FlagBag();
                flagBag.id = rs.getInt("id");
                flagBag.name = rs.getString("name");
                flagBag.gold = rs.getInt("gold");
                flagBag.gem = rs.getInt("gem");
                flagBag.iconId = rs.getShort("icon_id");
                String[] iconData = rs.getString("icon_data").split(",");
                flagBag.iconEffect = new short[iconData.length];
                for (int j = 0; j < iconData.length; j++) {
                    flagBag.iconEffect[j] = Short.parseShort(iconData[j].trim());
                }
                FLAGS_BAGS.add(flagBag);
            }
            Logger.log(Logger.GREEN, "[DONE] FLAGBAG(" + FLAGS_BAGS.size() + ")\n");

            // load intrinsic
            ps = con.prepareStatement("select * from intrinsic");
            rs = ps.executeQuery();
            while (rs.next()) {
                Intrinsic intrinsic = new Intrinsic();
                intrinsic.id = rs.getByte("id");
                intrinsic.name = rs.getString("name");
                intrinsic.paramFrom1 = rs.getShort("param_from_1");
                intrinsic.paramTo1 = rs.getShort("param_to_1");
                intrinsic.paramFrom2 = rs.getShort("param_from_2");
                intrinsic.paramTo2 = rs.getShort("param_to_2");
                intrinsic.icon = rs.getShort("icon");
                intrinsic.gender = rs.getByte("gender");
                switch (intrinsic.gender) {
                    case ConstPlayer.TRAI_DAT:
                        INTRINSIC_TD.add(intrinsic);
                        break;
                    case ConstPlayer.NAMEC:
                        INTRINSIC_NM.add(intrinsic);
                        break;
                    case ConstPlayer.XAYDA:
                        INTRINSIC_XD.add(intrinsic);
                        break;
                    default:
                        INTRINSIC_TD.add(intrinsic);
                        INTRINSIC_NM.add(intrinsic);
                        INTRINSIC_XD.add(intrinsic);
                }
                INTRINSICS.add(intrinsic);
            }
            Logger.log(Logger.GREEN, "[DONE] INTRINSIC(" + INTRINSICS.size() + ")\n");
            ps = con.prepareStatement("SELECT id, NAME, detail FROM task_main_template ORDER BY id");
            rs = ps.executeQuery();
            while (rs.next()) {
                TaskMain task = new TaskMain();
                task.id = rs.getInt("id");
                task.name = rs.getString("NAME");
                task.detail = rs.getString("detail");
                TASKS_TEMPLATE.add(task);
            }
            ps = con.prepareStatement(
                    "SELECT task_main_id, task_sub_id, MAX(target_count) AS max_target " +
                            "FROM task_requirements WHERE is_active = TRUE " +
                            "GROUP BY task_main_id, task_sub_id ORDER BY task_main_id, task_sub_id");
            rs = ps.executeQuery();
            while (rs.next()) {
                int mainIdReq = rs.getInt("task_main_id");
                int maxTarget = rs.getInt("max_target");
                TaskMain tmFound = null;
                for (TaskMain tm : TASKS_TEMPLATE) {
                    if (tm.id == mainIdReq) {
                        tmFound = tm;
                        break;
                    }
                }
                if (tmFound == null)
                    continue;

                SubTaskMain subTask = new SubTaskMain();
                subTask.name = "Nhiệm vụ";
                subTask.notify = "";
                subTask.npcId = (byte) -1;
                subTask.mapId = (short) -1;
                subTask.maxCount = (short) Math.max(0, maxTarget);
                tmFound.subTasks.add(subTask);
            }
            Logger.log(Logger.GREEN,
                    "[DONE] TASK(" + TASKS_TEMPLATE.size() + ") built from task_main_template + task_requirements\n");

            // load side task
            ps = con.prepareStatement("select * from side_task_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                SideTaskTemplate sideTask = new SideTaskTemplate();
                sideTask.id = rs.getInt("id");
                sideTask.name = rs.getString("name");
                String[] mc1 = rs.getString("max_count_lv1").split("-");
                String[] mc2 = rs.getString("max_count_lv2").split("-");
                String[] mc3 = rs.getString("max_count_lv3").split("-");
                String[] mc4 = rs.getString("max_count_lv4").split("-");
                String[] mc5 = rs.getString("max_count_lv5").split("-");
                sideTask.count[0][0] = Integer.parseInt(mc1[0]);
                sideTask.count[0][1] = Integer.parseInt(mc1[1]);
                sideTask.count[1][0] = Integer.parseInt(mc2[0]);
                sideTask.count[1][1] = Integer.parseInt(mc2[1]);
                sideTask.count[2][0] = Integer.parseInt(mc3[0]);
                sideTask.count[2][1] = Integer.parseInt(mc3[1]);
                sideTask.count[3][0] = Integer.parseInt(mc4[0]);
                sideTask.count[3][1] = Integer.parseInt(mc4[1]);
                sideTask.count[4][0] = Integer.parseInt(mc5[0]);
                sideTask.count[4][1] = Integer.parseInt(mc5[1]);
                SIDE_TASKS_TEMPLATE.add(sideTask);
            }
            Logger.log(Logger.GREEN, "[DONE] SIDETASK(" + SIDE_TASKS_TEMPLATE.size() + ")\n");

            // load item template
            ps = con.prepareStatement("select * from item_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                ItemTemplate itemTemp = new ItemTemplate();
                itemTemp.id = rs.getShort("id");
                itemTemp.type = rs.getByte("type");
                itemTemp.gender = rs.getByte("gender");
                itemTemp.name = rs.getString("name");
                itemTemp.description = rs.getString("description");
                itemTemp.iconID = rs.getShort("icon_id");
                itemTemp.part = rs.getShort("part");
                itemTemp.isUpToUp = rs.getBoolean("is_up_to_up");
                itemTemp.strRequire = rs.getInt("power_require");
                itemTemp.gold = rs.getInt("gold");
                itemTemp.gem = rs.getInt("gem");
                itemTemp.head = rs.getInt("head");
                itemTemp.body = rs.getInt("body");
                itemTemp.leg = rs.getInt("leg");
                ITEM_TEMPLATES.add(itemTemp);
            }
            Logger.log(Logger.GREEN, "[DONE] ITEM(" + ITEM_TEMPLATES.size() + ")\n");

            for (int i = 0; i < ITEM_TEMPLATES.size(); i++) {
                if (ITEM_TEMPLATES.get(i).type == 5 && ITEM_TEMPLATES.get(i).id >= 1018
                        && ITEM_TEMPLATES.get(i).id <= 2049) {
                    CAITRANG.add(ItemService.gI().createNewItem(((short) ITEM_TEMPLATES.get(i).id)));
                }
            }

            // load array head 2 frames
            ps = con.prepareStatement("select * from array_head_2_frames");
            rs = ps.executeQuery();
            while (rs.next()) {
                ArrHead2Frames arrHead2Frames = new ArrHead2Frames();
                dataArray = (JSONArray) JSONValue.parse(rs.getString("data"));
                for (int i = 0; i < dataArray.size(); i++) {
                    arrHead2Frames.frames.add(Integer.valueOf(dataArray.get(i).toString()));
                }
                ARR_HEAD_2_FRAMES.add(arrHead2Frames);
            }
            Logger.log(Logger.GREEN, "[DONE] ARRHEAD(" + ARR_HEAD_2_FRAMES.size() + ")\n");

            // load item option template
            ps = con.prepareStatement("select id, name from item_option_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                ItemOptionTemplate optionTemp = new ItemOptionTemplate();
                optionTemp.id = rs.getInt("id");
                optionTemp.name = rs.getString("name");
                ITEM_OPTION_TEMPLATES.add(optionTemp);
            }
            Logger.log(Logger.GREEN, "[DONE] MAPITEMOPTION(" + ITEM_OPTION_TEMPLATES.size() + ")\n");

            // load shop
            SHOPS = ShopDAO.getShops(con);
            Logger.log(Logger.GREEN, "[DONE] SHOP(" + SHOPS.size() + ")\n");

            // load reward lucky round
            File folder = new File("data/girlkun/data_lucky_round_reward");
            for (File fileEntry : folder.listFiles()) {
                if (!fileEntry.isDirectory()) {
                    BufferedReader br = new BufferedReader(new FileReader(fileEntry));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        line = line.replaceAll("[{}\\[\\]]", "");
                        String[] arrSub = line.split("\\|");
                        String[] data1 = arrSub[0].split(":");
                        ItemLuckyRound item = new ItemLuckyRound();
                        item.temp = ItemService.gI().getTemplate(Integer.parseInt(data1[0]));
                        item.ratio = Integer.parseInt(data1[1]);
                        item.typeRatio = Integer.parseInt(data1[2]);
                        if (arrSub.length > 1) {
                            String[] data2 = arrSub[1].split(";");
                            for (String str : data2) {
                                String[] data = str.split(":");
                                ItemOptionLuckyRound io = new ItemOptionLuckyRound();
                                Item.ItemOption itemOption = new Item.ItemOption(Integer.parseInt(data[0]), 0);
                                io.itemOption = itemOption;
                                String[] param = data[1].split("-");
                                io.param1 = Integer.parseInt(param[0]);
                                if (param.length == 2) {
                                    io.param2 = Integer.parseInt(param[1]);
                                }
                                item.itemOptions.add(io);
                            }
                        }
                        LUCKY_ROUND_REWARDS.add(item);
                    }
                }
            }
            Logger.log(Logger.GREEN, "[DONE] LUCKYROUND(" + LUCKY_ROUND_REWARDS.size() + ")\n");
            // load reward mob
            folder = new File("data/girlkun/mob_reward");
            String DatamobreWard = "";
            for (File fileEntry : folder.listFiles()) {
                if (!fileEntry.isDirectory()) {
                    DataInputStream dis = new DataInputStream(new FileInputStream(fileEntry));
                    int size = dis.readInt();
                    for (int i = 0; i < size; i++) {
                        int mobId = dis.readInt();
                        DatamobreWard += "\nFileName:" + fileEntry.getName();
                        DatamobreWard += "\nMobID:" + mobId;
                        MobReward mobReward = MOB_REWARDS.get(mobId);
                        if (mobReward == null) {
                            mobReward = new MobReward(mobId);
                            MOB_REWARDS.put(mobId, mobReward);
                        }
                        int itemId = dis.readInt();
                        String[] quantity = dis.readUTF().split("-");
                        String[] ratio = dis.readUTF().split("-");
                        int gender = dis.readInt();
                        String map = dis.readUTF();
                        String[] arrMap = map.replaceAll("[\\]\\[]", "").split(",");
                        int[] mapDrop = new int[arrMap.length];
                        for (int g = 0; g < mapDrop.length; g++) {
                            mapDrop[g] = Integer.parseInt(arrMap[g]);
                            DatamobreWard += "\nMapDrop:" + mapDrop[g];
                        }
                        ItemMobReward item = new ItemMobReward(itemId, mapDrop,
                                new int[] { Integer.parseInt(quantity[0]), Integer.parseInt(quantity[1]) },
                                new int[] { Integer.parseInt(ratio[0]), Integer.parseInt(ratio[1]) }, gender);
                        DatamobreWard += "\nItemName:" + item.getTemp().name;
                        DatamobreWard += "\nRatio:" + Integer.parseInt(ratio[0]) + "to" + Integer.parseInt(ratio[1]);
                        if (item.getTemp().type == 30) { // sao pha lê
                            item.setRatio(new int[] { 20, Integer.parseInt(ratio[1]) });
                        }
                        if (item.getTemp().type == 14) { // 14 đá nâng cấp
                            item.setRatio(new int[] { 20, Integer.parseInt(ratio[1]) });
                        }
                        if (item.getTemp().type < 5) {
                            item.setRatio(new int[] { Integer.parseInt(ratio[0]), Integer.parseInt(ratio[1]) });
                        }
                        if (item.getTemp().type == 9) { // vàng
                            mobReward.getGoldReward().add(item);
                        } else {
                            boolean flag = false;
                            for (int z = 0; z < Manager.itemIds_TL.length; z++) {
                                if (Manager.itemIds_TL[z] == item.getTemp().id) {
                                    item.setRatio(new int[] { 1, 5000000 });
                                }
                            }
                            if (!flag) {
                                mobReward.getItemReward().add(item);
                            }
                        }
                        int sizeOption = dis.readInt();
                        for (int j = 0; j < sizeOption; j++) {
                            int optionId = dis.readInt();
                            String[] param = dis.readUTF().split("-");
                            String[] ratioOption = dis.readUTF().split("-");
                            ItemOptionMobReward option = new ItemOptionMobReward(optionId,
                                    new int[] { Integer.parseInt(param[0]), Integer.parseInt(param[1]) },
                                    new int[] { Integer.parseInt(ratioOption[0]), Integer.parseInt(ratioOption[1]) });
                            item.getOption().add(option);
                        }
                    }

                }
            }
            Service.gI().DataMobReward += DatamobreWard;
            Logger.log(Logger.GREEN, "[DONE] REWARDMOD(" + MOB_REWARDS.size() + ")\n");

            // load notify
            folder = new File("data/girlkun/notify");
            for (File fileEntry : folder.listFiles()) {
                if (!fileEntry.isDirectory()) {
                    StringBuffer notify = new StringBuffer(
                            fileEntry.getName().substring(0, fileEntry.getName().lastIndexOf("."))).append("<>");
                    BufferedReader br = new BufferedReader(new FileReader(fileEntry));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        notify.append(line + "\n");
                    }
                    NOTIFY.add(notify.toString());
                }
            }
            Logger.log(Logger.GREEN, "[DONE] NOTIFY(" + NOTIFY.size() + ")\n");

            // load caption
            ps = con.prepareStatement("select * from caption");
            rs = ps.executeQuery();
            while (rs.next()) {
                CAPTIONS.add(rs.getString("name"));
            }
            Logger.log(Logger.GREEN, "[DONE] CAPTION(" + CAPTIONS.size() + ")\n");

            // load image by name
            ps = con.prepareStatement("select name, n_frame from img_by_name");
            rs = ps.executeQuery();
            while (rs.next()) {
                IMAGES_BY_NAME.put(rs.getString("name"), rs.getByte("n_frame"));
            }
            Logger.log(Logger.GREEN, "[DONE] IMAGEBYNAME(" + IMAGES_BY_NAME.size() + ")\n");

            // load mob template
            ps = con.prepareStatement("select * from mob_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                MobTemplate mobTemp = new MobTemplate();
                mobTemp.id = rs.getByte("id");
                mobTemp.type = rs.getByte("type");
                mobTemp.name = rs.getString("name");
                mobTemp.hp = rs.getInt("hp");
                mobTemp.rangeMove = rs.getByte("range_move");
                mobTemp.speed = rs.getByte("speed");
                mobTemp.dartType = rs.getByte("dart_type");
                mobTemp.percentDame = rs.getByte("percent_dame");
                mobTemp.percentTiemNang = rs.getByte("percent_tiem_nang");
                MOB_TEMPLATES.add(mobTemp);
            }
            Logger.log(Logger.GREEN, "[DONE] MOBTEMPLATE(" + MOB_TEMPLATES.size() + ")\n");

            ps = con.prepareStatement("SELECT * FROM shop_ky_gui");
            rs = ps.executeQuery();
            while (rs.next()) {
                int i = rs.getInt("id");
                int idPl = rs.getInt("player_id");
                short itemId = rs.getShort("item_id");
                int ruby = rs.getInt("ruby");
                int gem = rs.getInt("gem");
                int quantity = rs.getInt("quantity");
                byte isUp = rs.getByte("isUpTop");
                byte isBuy = rs.getByte("isBuy");
                long createTime = rs.getLong("createTime");
                List<Item.ItemOption> op = new ArrayList<>();
                JSONArray jsa2 = (JSONArray) JSONValue.parse(rs.getString("itemOption"));
                for (int j = 0; j < jsa2.size(); ++j) {
                    JSONObject jso2 = (JSONObject) jsa2.get(j);
                    int idOptions = Integer.parseInt(jso2.get("id").toString());
                    int param = Integer.parseInt(jso2.get("param").toString());
                    op.add(new Item.ItemOption(idOptions, param));
                }
                Item item = ItemService.gI().createNewItem(itemId);
                byte tab = ShopKyGuiService.gI().getTabKiGui(item);
                ShopKyGuiManager.gI().listItem
                        .add(new ItemKyGui(i, itemId, idPl, tab, ruby, gem, quantity, isUp, op, isBuy, createTime));
            }
            Logger.log(Logger.GREEN, "[DONE] KYGUI[" + ShopKyGuiManager.gI().listItem.size() + "]!\n");

            // load npc template
            ps = con.prepareStatement("select * from npc_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                NpcTemplate npcTemp = new NpcTemplate();
                npcTemp.id = rs.getByte("id");
                npcTemp.name = rs.getString("name");
                npcTemp.head = rs.getShort("head");
                npcTemp.body = rs.getShort("body");
                npcTemp.leg = rs.getShort("leg");
                npcTemp.avatar = rs.getInt("avatar");
                NPC_TEMPLATES.add(npcTemp);
            }
            Logger.log(Logger.GREEN, "[DONE] NPCTEMPLATE(" + NPC_TEMPLATES.size() + ")\n");
            // // Thanh Tuu
            ps = con.prepareStatement("select * from archive_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                ArchivementTemplate a = new ArchivementTemplate();
                a.id = rs.getInt("id");
                a.info1 = rs.getString("info1");
                a.info2 = rs.getString("info2");
                a.money = rs.getInt("money");
                Archivement_TEMPLATES.add(a);
            }
            Logger.log(Logger.GREEN, "[DONE] ARCHIVE(" + Archivement_TEMPLATES.size() + ")\n");
            // load map template
            ps = con.prepareStatement("select count(id) from map_template");
            rs = ps.executeQuery();
            if (rs.first()) {
                int countRow = rs.getShort(1);
                MAP_TEMPLATES = new MapTemplate[countRow];
                ps = con.prepareStatement("select * from map_template");
                rs = ps.executeQuery();
                short i = 0;
                while (rs.next()) {
                    MapTemplate mapTemplate = new MapTemplate();
                    int mapId = rs.getInt("id");
                    String mapName = rs.getString("name");
                    mapTemplate.id = mapId;
                    mapTemplate.name = mapName;
                    // load data

                    dataArray = (JSONArray) jv.parse(rs.getString("data"));
                    mapTemplate.type = Byte.parseByte(String.valueOf(dataArray.get(0)));
                    mapTemplate.planetId = Byte.parseByte(String.valueOf(dataArray.get(1)));
                    mapTemplate.bgType = Byte.parseByte(String.valueOf(dataArray.get(2)));
                    mapTemplate.tileId = Byte.parseByte(String.valueOf(dataArray.get(3)));
                    mapTemplate.bgId = Byte.parseByte(String.valueOf(dataArray.get(4)));
                    dataArray.clear();
                    ///////////////////////////////////////////////////////////////////
                    mapTemplate.type = rs.getByte("type");
                    mapTemplate.planetId = rs.getByte("planet_id");
                    mapTemplate.bgType = rs.getByte("bg_type");
                    mapTemplate.tileId = rs.getByte("tile_id");
                    mapTemplate.bgId = rs.getByte("bg_id");
                    mapTemplate.zones = rs.getByte("zones");
                    mapTemplate.maxPlayerPerZone = rs.getByte("max_player");
                    // load waypoints
                    dataArray = (JSONArray) jv.parse(rs.getString("waypoints")
                            .replaceAll("\\[\"\\[", "[[")
                            .replaceAll("\\]\"\\]", "]]")
                            .replaceAll("\",\"", ","));
                    for (int j = 0; j < dataArray.size(); j++) {
                        WayPoint wp = new WayPoint();
                        JSONArray dtwp = (JSONArray) jv.parse(String.valueOf(dataArray.get(j)));
                        wp.name = String.valueOf(dtwp.get(0));
                        wp.minX = Short.parseShort(String.valueOf(dtwp.get(1)));
                        wp.minY = Short.parseShort(String.valueOf(dtwp.get(2)));
                        wp.maxX = Short.parseShort(String.valueOf(dtwp.get(3)));
                        wp.maxY = Short.parseShort(String.valueOf(dtwp.get(4)));
                        wp.isEnter = Byte.parseByte(String.valueOf(dtwp.get(5))) == 1;
                        wp.isOffline = Byte.parseByte(String.valueOf(dtwp.get(6))) == 1;
                        wp.goMap = Short.parseShort(String.valueOf(dtwp.get(7)));
                        wp.goX = Short.parseShort(String.valueOf(dtwp.get(8)));
                        wp.goY = Short.parseShort(String.valueOf(dtwp.get(9)));
                        mapTemplate.wayPoints.add(wp);
                        dtwp.clear();
                    }
                    dataArray.clear();
                    // load mobs
                    dataArray = (JSONArray) jv.parse(rs.getString("mobs").replaceAll("\\\"", ""));
                    mapTemplate.mobTemp = new byte[dataArray.size()];
                    mapTemplate.mobLevel = new byte[dataArray.size()];
                    mapTemplate.mobHp = new double[dataArray.size()];
                    mapTemplate.mobX = new short[dataArray.size()];
                    mapTemplate.mobY = new short[dataArray.size()];
                    for (int j = 0; j < dataArray.size(); j++) {
                        JSONArray dtm = (JSONArray) jv.parse(String.valueOf(dataArray.get(j)));
                        mapTemplate.mobTemp[j] = Byte.parseByte(String.valueOf(dtm.get(0)));
                        mapTemplate.mobLevel[j] = Byte.parseByte(String.valueOf(dtm.get(1)));
                        // phước, chỉnh máu quái lên chục K tỷ
                        mapTemplate.mobHp[j] = (long) Long.parseLong(String.valueOf(dtm.get(2)));
                        mapTemplate.mobX[j] = Short.parseShort(String.valueOf(dtm.get(3)));
                        mapTemplate.mobY[j] = Short.parseShort(String.valueOf(dtm.get(4)));
                        dtm.clear();
                    }
                    dataArray.clear();
                    // load npcs
                    dataArray = (JSONArray) jv.parse(rs.getString("npcs").replaceAll("\\\"", ""));
                    mapTemplate.npcId = new byte[dataArray.size()];
                    mapTemplate.npcX = new short[dataArray.size()];
                    mapTemplate.npcY = new short[dataArray.size()];
                    for (int j = 0; j < dataArray.size(); j++) {
                        JSONArray dtn = (JSONArray) jv.parse(String.valueOf(dataArray.get(j)));
                        mapTemplate.npcId[j] = Byte.parseByte(String.valueOf(dtn.get(0)));
                        mapTemplate.npcX[j] = Short.parseShort(String.valueOf(dtn.get(1)));
                        mapTemplate.npcY[j] = Short.parseShort(String.valueOf(dtn.get(2)));
                        dtn.clear();
                    }
                    dataArray.clear();
                    MAP_TEMPLATES[i++] = mapTemplate;
                }
                Logger.log(Logger.GREEN, "[DONE] MAPTEMPLATE(" + MAP_TEMPLATES.length + ")\n");
                RUBY_REWARDS.add(Util.sendDo(861, 0, new ArrayList<>()));
            }
            ps = con.prepareStatement("select * from radar");
            rs = ps.executeQuery();
            while (rs.next()) {
                RadarCard rd = new RadarCard();
                rd.Id = rs.getShort("id");
                rd.IconId = rs.getShort("iconId");
                rd.Rank = rs.getByte("rank");
                rd.Max = rs.getByte("max");
                rd.Type = rs.getByte("type");
                rd.Template = rs.getShort("template");
                rd.Name = rs.getString("name");
                rd.Info = rs.getString("info");
                JSONArray arr = (JSONArray) JSONValue.parse(rs.getString("body"));
                for (int i = 0; i < arr.size(); i++) {
                    JSONObject ob = (JSONObject) arr.get(i);
                    if (ob != null) {
                        rd.Head = Short.parseShort(ob.get("head").toString());
                        rd.Body = Short.parseShort(ob.get("body").toString());
                        rd.Leg = Short.parseShort(ob.get("leg").toString());
                        rd.Bag = Short.parseShort(ob.get("bag").toString());
                    }
                }
                rd.Options.clear();
                arr = (JSONArray) JSONValue.parse(rs.getString("options"));
                for (int i = 0; i < arr.size(); i++) {
                    JSONObject ob = (JSONObject) arr.get(i);
                    if (ob != null) {
                        rd.Options.add(new OptionCard(Integer.parseInt(ob.get("id").toString()),
                                Short.parseShort(ob.get("param").toString()),
                                Byte.parseByte(ob.get("activeCard").toString())));
                    }
                }
                rd.Require = rs.getShort("require");
                rd.RequireLevel = rs.getShort("require_level");
                rd.AuraId = rs.getShort("aura_id");
                RadarService.gI().RADAR_TEMPLATE.add(rd);
            }
            Logger.log(Logger.GREEN, "[DONE] RADARTEMPLATE(" + RadarService.gI().RADAR_TEMPLATE.size() + ")\n");
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException e) {
                System.err.print("\nError at 301\n");
                e.printStackTrace();

            }
        } catch (Exception e) {
            System.err.print("\nError at 302\n");
            e.printStackTrace();
            System.exit(0);

        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException e) {
                System.err.print("\nError at 303\n");
                e.printStackTrace();
            }
        }
        Logger.log(Logger.YELLOW, "[DONE] " + (System.currentTimeMillis() - st) + "(MS)\n");
    }

    public static List<TOP> realTopSieuHang(Player pl) {
        List<TOP> tops = new ArrayList<>();
        try {
            GirlkunResultSet rs = GirlkunDB
                    .executeQuery("SELECT id, rank_sieu_hang AS rank FROM player WHERE rank_sieu_hang <= "
                            + pl.rankSieuHang + " ORDER BY rank_sieu_hang DESC LIMIT 10");
            while (rs.next()) {
                int rank = rs.getInt("rank_sieu_hang");
                if (Math.abs(rank - pl.rankSieuHang) <= 10) {
                    TOP top = TOP.builder().id_player(rs.getInt("id")).build();
                    String phanthuong = "";

                    if (rank == 1) {
                        phanthuong = "50k hồng ngọc/ngày";
                    } else if (rank <= 5) {
                        phanthuong = "30k hồng ngọc/ngày";
                    } else if (rank <= 10) {
                        phanthuong = "10k hồng ngọc/ngày";
                    } else if (rank <= 50) {
                        phanthuong = "5k hồng ngọc/ngày";
                    } else {
                        phanthuong = "1k hồng ngọc/ngày";
                    }
                    top.setInfo1("");
                    top.setInfo2(phanthuong);
                    top.setRank(rank);
                    tops.add(top);
                }
            }
            Collections.reverse(tops);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tops;
    }

    public static List<TOP> realTopSieuHang(Connection con) {
        List<TOP> tops = new ArrayList<>();
        try {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT id, rank_sieu_hang AS rank FROM player ORDER BY rank_sieu_hang ASC LIMIT 100");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                long rank = rs.getLong("rank");
                if (rank > 0 && rank <= 100) {
                    TOP top = TOP.builder().id_player(rs.getInt("id")).build();
                    String phanthuong = "";
                    if (rank == 1) {
                        phanthuong = "50k hồng ngọc/ngày";
                    } else if (rank <= 5) {
                        phanthuong = "30k hồng ngọc/ngày";
                    } else if (rank <= 10) {
                        phanthuong = "10k hồng ngọc/ngày";
                    } else if (rank <= 50) {
                        phanthuong = "5k hồng ngọc/ngày";
                    } else {
                        phanthuong = "1k hồng ngọc/ngày";
                    }
                    top.setInfo1("1");
                    top.setInfo2(phanthuong);
                    tops.add(top);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tops;
    }

    public static List<TOP> realTop(String query, Connection con) {
        int i = 0;
        List<TOP> tops = new ArrayList<>();
        JSONArray dataArray;
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                short head = Util.getHead((byte) rs.getInt("gender"));
                short body = (short) (rs.getInt("gender") == 1 ? 59 : 57);
                short leg = (short) (rs.getInt("gender") == 1 ? 60 : 58);
                dataArray = (JSONArray) JSONValue.parse(rs.getString("items_body"));
                JSONArray dataItem = (JSONArray) JSONValue.parse(dataArray.get(0).toString());
                if (dataItem != null && dataItem.get(0) != null) {
                    Item item;
                    short tempId = Short.parseShort(String.valueOf(dataItem.get(0)));
                    if (tempId != -1) {
                        item = ItemService.gI().createNewItem(tempId,
                                Integer.parseInt(String.valueOf(dataItem.get(1))));
                        body = (short) item.template.part;
                    }
                }
                dataItem = (JSONArray) JSONValue.parse(dataArray.get(1).toString());
                if (dataItem != null && dataItem.get(0) != null) {
                    Item item;
                    short tempId = Short.parseShort(String.valueOf(dataItem.get(0)));
                    if (tempId != -1) {
                        item = ItemService.gI().createNewItem(tempId,
                                Integer.parseInt(String.valueOf(dataItem.get(1))));
                        leg = (short) item.template.part;
                    }
                }
                dataItem = (JSONArray) JSONValue.parse(dataArray.get(5).toString());
                if (dataItem != null && dataItem.get(0) != null) {
                    Item item;
                    short tempId = Short.parseShort(String.valueOf(dataItem.get(0)));
                    if (tempId != -1) {
                        item = ItemService.gI().createNewItem(tempId,
                                Integer.parseInt(String.valueOf(dataItem.get(1))));
                        if (item.template.head != -1) {
                            head = (short) item.template.head;
                        }
                        if (item.template.body != -1) {
                            body = (short) item.template.body;
                        }
                        if (item.template.leg != -1) {
                            leg = (short) item.template.leg;
                        }
                    }
                }
                dataArray.clear();
                TOP top = TOP.builder().name(rs.getString("name")).gender(rs.getByte("gender")).head(head).body(body)
                        .leg(leg).build();
                top.setPower(rs.getLong("sm"));
                tops.add(top);
            }
        } catch (NumberFormatException | SQLException e) {
            e.printStackTrace();
        }
        return tops;
    }

    public void loadProperties() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("data/girlkun/girlkun.properties"));
        Object value = null;
        // ###Config sv
        if ((value = properties.get("server.girlkun.port")) != null) {
            ServerManager.PORT = Integer.parseInt(String.valueOf(value));
        }
        if ((value = properties.get("server.girlkun.name")) != null) {
            ServerManager.NAME = String.valueOf(value);
        }
        if ((value = properties.get("server.girlkun.sv")) != null) {
            SERVER = Byte.parseByte(String.valueOf(value));
        }
        String linkServer = "";
        for (int i = 1; i <= 10; i++) {
            value = properties.get("server.girlkun.sv" + i);
            if (value != null) {
                linkServer += String.valueOf(value) + ":0,";
            }
        }
        DataGame.LINK_IP_PORT = linkServer.substring(0, linkServer.length() - 1);
        if ((value = properties.get("server.girlkun.maxperip")) != null) {
            MAX_PER_IP = Integer.parseInt(String.valueOf(value));
        }
        if ((value = properties.get("server.girlkun.maxplayer")) != null) {
            MAX_PLAYER = Integer.parseInt(String.valueOf(value));
        }
        if ((value = properties.get("server.girlkun.expserver")) != null) {
            RATE_EXP_SERVER = Byte.parseByte(String.valueOf(value));
        }
        if ((value = properties.get("server.girlkun.local")) != null) {
            LOCAL = String.valueOf(value).toLowerCase().equals("true");
        }
    }

    /**
     * @param tileTypeFocus tile type: top, bot, left, right...
     * @return [tileMapId][tileType]
     */
    private int[][] readTileIndexTileType(int tileTypeFocus) {
        int[][] tileIndexTileType = null;
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream("data/girlkun/map/tile_set_info"));
            int numTileMap = dis.readByte();
            tileIndexTileType = new int[numTileMap][];
            for (int i = 0; i < numTileMap; i++) {
                int numTileOfMap = dis.readByte();
                for (int j = 0; j < numTileOfMap; j++) {
                    int tileType = dis.readInt();
                    int numIndex = dis.readByte();
                    if (tileType == tileTypeFocus) {
                        tileIndexTileType[i] = new int[numIndex];
                    }
                    for (int k = 0; k < numIndex; k++) {
                        int typeIndex = dis.readByte();
                        if (tileType == tileTypeFocus) {
                            tileIndexTileType[i][k] = typeIndex;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.print("\nError at 305\n");
            e.printStackTrace();
        }
        return tileIndexTileType;
    }

    /**
     * @param mapId mapId
     * @return tile map for paint
     */
    private int[][] readTileMap(int mapId) {
        int[][] tileMap = null;
        try {
            File file = new File("data/girlkun/map/tile_map_data/" + mapId);
            if (file.exists()) {
                DataInputStream dis = new DataInputStream(
                        new FileInputStream("data/girlkun/map/tile_map_data/" + mapId));
                int w = dis.readByte();
                int h = dis.readByte();
                tileMap = new int[h][w];
                for (int i = 0; i < tileMap.length; i++) {
                    for (int j = 0; j < tileMap[i].length; j++) {
                        tileMap[i][j] = dis.readByte();
                    }
                }
                dis.close();
            }
        } catch (Exception e) {
            System.err.print("\nError at 306\n");
            e.printStackTrace();
        }
        return tileMap;
    }

    // service*******************************************************************
    public static Clan getClanById(int id) throws Exception {
        for (Clan clan : CLANS) {
            if (clan.id == id) {
                return clan;
            }
        }
        throw new Exception("Không tìm thấy id bang hội: " + id);
    }

    public static void addClan(Clan clan) {
        CLANS.add(clan);
    }

    public static int getNumClan() {
        return CLANS.size();

    }

    public static MobTemplate getMobTemplateByTemp(int mobTempId) {
        for (MobTemplate mobTemp : MOB_TEMPLATES) {
            if (mobTemp.id == mobTempId) {
                return mobTemp;
            }
        }
        return null;
    }

    public static byte getNFrameImageByName(String name) {
        Object n = IMAGES_BY_NAME.get(name);
        if (n != null) {
            return Byte.parseByte(String.valueOf(n));
        } else {
            return 0;
        }
    }

}
