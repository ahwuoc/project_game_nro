package Dragon.services.func;

import Dragon.consts.ConstMap;
import Dragon.consts.ConstPlayer;
import Dragon.consts.ConstTask;
import static Dragon.models.boss.BossID.LuyenTap;
import Dragon.models.boss.BossesData;
import Dragon.models.boss.list_boss.LuyenTap;
import Dragon.models.map.Map;
import Dragon.models.map.MapMaBu.MapMaBu;
import Dragon.models.map.WayPoint;
import Dragon.models.map.Zone;
import Dragon.models.map.blackball.BlackBallWar;
import Dragon.services.MapService;
import Dragon.models.mob.Mob;
import Dragon.models.player.Player;
import Dragon.models.matches.TYPE_LOSE_PVP;
import Dragon.models.matches.TYPE_PVP;
import Dragon.services.Service;
import Dragon.utils.Util;
import com.girlkun.network.io.Message;
import Dragon.server.Manager;
import Dragon.services.EffectSkillService;
import Dragon.services.PlayerService;
import Dragon.services.TaskService;
import Dragon.thuongnhanthanbi.Dungeon_Manager;
import Dragon.utils.Logger;
import Dragon.utils.TimeUtil;

import java.util.List;
import java.util.logging.Level;

public class ChangeMapService {

    private static final byte EFFECT_GO_TO_TUONG_LAI = 0;
    private static final byte EFFECT_GO_TO_DIA_NGUC = 55;
    private static final byte EFFECT_GO_TO_BDKB = 1;

    public static final byte AUTO_SPACE_SHIP = -1;
    public static final byte NON_SPACE_SHIP = 0;
    public static final byte DEFAULT_SPACE_SHIP = 1;
    public static final byte TELEPORT_YARDRAT = 2;
    public static final byte TENNIS_SPACE_SHIP = 3;

    private static ChangeMapService instance;

    public ChangeMapService() {

    }

    public static ChangeMapService gI() {
        if (instance == null) {
            instance = new ChangeMapService();
        }
        return instance;
    }

    public void openChangeMapTab(Player pl) {
        List<Zone> list = null;
        switch (pl.iDMark.getTypeChangeMap()) {
        }
        Message msg;
        try {
            msg = new Message(-91);
            switch (pl.iDMark.getTypeChangeMap()) {
                case ConstMap.CHANGE_CAPSULE:
                    list = (pl.mapCapsule = MapService.gI().getMapCapsule(pl));
                    msg.writer().writeByte(list.size());
                    for (int i = 0; i < pl.mapCapsule.size(); i++) {
                        Zone zone = pl.mapCapsule.get(i);
                        if (i == 0 && pl.mapBeforeCapsule != null) {
                            msg.writer().writeUTF("Về Chỗ Cũ: " + zone.map.mapName);
                        } else if (zone.map.mapName.equals("Ngôi Nhà")) {
                            msg.writer().writeUTF("Về Nhà");
                        } else {
                            msg.writer().writeUTF(zone.map.mapName);
                        }
                        msg.writer().writeUTF(zone.map.planetName);
                    }
                    if (pl.haveDuongTang) {
                        Service.getInstance().sendThongBao(pl, "Đang hộ tống Thỏ Ngọc không thể Capsun");
                        return;
                    }
                case ConstMap.CHANGE_BLACK_BALL:
                    list = (pl.mapBlackBall != null ? pl.mapBlackBall
                            : (pl.mapBlackBall = MapService.gI().getMapBlackBall()));
                    msg.writer().writeByte(list.size());
                    for (Zone zone : list) {
                        msg.writer().writeUTF(zone.map.mapName);
                        msg.writer().writeUTF(zone.map.planetName);
                    }
                    break;
                case ConstMap.CHANGE_MAP_MA_BU:
                    list = (pl.mapMaBu != null ? pl.mapMaBu
                            : (pl.mapMaBu = MapService.gI().getMapMaBu()));
                    msg.writer().writeByte(list.size());
                    for (Zone zone : list) {
                        msg.writer().writeUTF(zone.map.mapName);
                        msg.writer().writeUTF(zone.map.planetName);
                    }
                    break;
            }
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(UseItem.class, e);

        }
    }

    public void openZoneUI(Player pl) {
        if (pl.zone == null) {
            Service.gI().sendThongBaoOK(pl, "Không Thể Đổi Khu Vực Trong Map Này");
            return;
        }
        if (pl.haveDuongTang) {
            Service.getInstance().sendThongBao(pl, "Đang Hộ tống Thỏ ngọc không thể đổi khu");
            return;
        }
        if (MapService.gI().isMapOffline(pl.zone.map.mapId)) {
            Service.gI().sendThongBaoOK(pl, "Không Thể Đổi Khu Vực Trong Map Này");
            return;
        }
        if (MapService.gI().isMapDoanhTrai(pl.zone.map.mapId)) {
            Service.gI().sendThongBaoOK(pl, "Không Thể Đổi Khu Vực Trong Map Này");
            return;
        }
        if (MapService.gI().isMapKhiGas(pl.zone.map.mapId)) {
            Service.getInstance().sendThongBaoOK(pl, "Không Thể Đổi Khu Vực Trong Map Này");
            return;
        }
        if (MapService.gI().isdiacung(pl.zone.map.mapId)) {
            Dungeon_Manager.gI().preventZoneChange(pl);
            return;
        }
        if (MapService.gI().isMapBanDoKhoBau(pl.zone.map.mapId)) {
            Service.gI().sendThongBaoOK(pl, "Không Thể Đổi Khu Vực Trong Map Này");
            return;
        }
        if (pl.zone.map.mapId == 51) {
            Service.gI().sendThongBaoOK(pl, "Không Thể Đổi Khu Vực Trong Map Này");
            return;
        }
        Message msg;
        try {
            msg = new Message(29);
            msg.writer().writeByte(pl.zone.map.zones.size());
            for (Zone zone : pl.zone.map.zones) {
                msg.writer().writeByte(zone.zoneId);
                int numPlayers = zone.getNumOfPlayers();
                msg.writer().writeByte((numPlayers < 5 ? 0 : (numPlayers < 8 ? 1 : 2)));
                msg.writer().writeByte(numPlayers);
                msg.writer().writeByte(zone.maxPlayer);
                msg.writer().writeByte(0);
            }
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {

        }
    }

    public void changeZone(Player pl, int zoneId) {
        if (pl.zone == null) {
            Service.gI().sendThongBaoOK(pl, "Không Thể Đổi Khu Vực Trong Map Này");
            return;
        }
        if (pl.haveDuongTang) {
            Service.getInstance().sendThongBao(pl, "Đang hộ tống Thỏ ngọc không thể đổi khu");
            return;
        }
        if (MapService.gI().isMapKhiGas(pl.zone.map.mapId)) {
            Service.getInstance().sendThongBaoOK(pl, "Không thể đổi khu vực trong map này");
            return;
        }
        if (MapService.gI().isMapOffline(pl.zone.map.mapId)) {
            Service.gI().sendThongBaoOK(pl, "Không Thể Đổi Khu Vực Trong Map Này");
            return;
        }
        if (MapService.gI().isMapDoanhTrai(pl.zone.map.mapId)) {
            Service.gI().sendThongBaoOK(pl, "Không Thể Đổi Khu Vực Trong Map Này");
            return;
        }
        if (MapService.gI().isdiacung(pl.zone.map.mapId)) {
            Dungeon_Manager.gI().preventZoneChange(pl);
            return;
        }
        if (MapService.gI().isMapBanDoKhoBau(pl.zone.map.mapId)) {
            Service.gI().sendThongBaoOK(pl, "Không Thể Đổi Khu Vực Trong Map Này");
            return;
        }
        if (pl.zone.map.mapId == 51) {
            Service.gI().sendThongBaoOK(pl, "Không Thể Đổi Khu Vực Trong Map Này");
            return;
        }
        if (pl.isAdmin() || pl.isBoss || Util.canDoWithTime(pl.iDMark.getLastTimeChangeZone(), 10000)) {
            pl.iDMark.setLastTimeChangeZone(System.currentTimeMillis());
            Map map = pl.zone.map;
            if (zoneId >= 0 && zoneId <= map.zones.size() - 1) {
                Zone zoneJoin = map.zones.get(zoneId);
                if (zoneJoin != null
                        && (zoneJoin.getNumOfPlayers() >= zoneJoin.maxPlayer && !pl.isAdmin() && !pl.isBoss)) {
                    Service.gI().sendThongBaoOK(pl, "Khu vực đã đầy");
                    return;
                }
                if (zoneJoin != null) {
                    changeMap(pl, zoneJoin, -1, -1, pl.location.x, pl.location.y, NON_SPACE_SHIP);
                }
            } else {
                Service.gI().sendThongBao(pl, "Không thể thực hiện");
            }
        } else {
            Service.gI().sendThongBaoOK(pl, "Không thể đổi khu vực lúc này, vui lòng đợi "
                    + TimeUtil.getTimeLeft(pl.iDMark.getLastTimeChangeZone(), 10));

            return;
        }
    }

    public void changeMapBySpaceShip(Player pl, int mapId, int zone, int x) {
        if (!pl.isAdmin() || !pl.isBoss) {
            if (pl.isDie()) {
                if (pl.haveTennisSpaceShip) {
                    Service.gI().hsChar(pl, pl.nPoint.hpMax, pl.nPoint.mpMax);
                } else {
                    Service.gI().hsChar(pl, 1, 1);
                }
            } else {
                if (pl.haveTennisSpaceShip) {
                    pl.nPoint.setFullHpMpDame();
                    PlayerService.gI().sendInfoHpMp(pl);
                }
            }
            if (pl.idNRNM != -1) {
                Service.gI().sendThongBaoOK(pl, "Không thể thực hiện khi có ngọc rồng namec");
                return;
            }
            changeMap(pl, null, mapId, zone, x, 5, AUTO_SPACE_SHIP);
        }
    }

    public void changeMapBySpaceShip(Player pl, Zone zoneJoin, int x) {
        if (pl.isDie()) {
            if (pl.haveTennisSpaceShip) {
                Service.gI().hsChar(pl, pl.nPoint.hpMax, pl.nPoint.mpMax);
            } else {
                Service.gI().hsChar(pl, 1, 1);
            }
        } else {
            if (pl.haveTennisSpaceShip) {
                pl.nPoint.setFullHpMpDame();
                PlayerService.gI().sendInfoHpMp(pl);
            }
        }
        if (pl.idNRNM != -1) {
            Service.gI().sendThongBaoOK(pl, "Không thể thực hiện khi có ngọc rồng namec");
            return;
        }
        changeMap(pl, zoneJoin, -1, -1, x, 5, AUTO_SPACE_SHIP);
    }

    public void changeMapInYard(Player pl, int mapId, int zoneId, int x) {
        Zone zoneJoin = MapService.gI().getMapCanJoin(pl, mapId, zoneId);
        if (zoneJoin != null) {
            x = x != -1 ? x : Util.nextInt(100, zoneJoin.map.mapWidth - 100);
            changeMap(pl, zoneJoin, -1, -1, x, zoneJoin.map.yPhysicInTop(x, 100), NON_SPACE_SHIP);
        }
    }

    // public void changeMapInYard(Player pl, Zone zoneJoin, int x) {
    // changeMap(pl, zoneJoin, -1, -1, x, zoneJoin.map.yPhysicInTop(x, 100),
    // NON_SPACE_SHIP);
    // }
    public void changeMap(Player pl, int mapId, int zone, int x, int y) {
        changeMap(pl, null, mapId, zone, x, y, NON_SPACE_SHIP);
    }

    public void changeMap(Player pl, Zone zoneJoin, int x, int y) {
        changeMap(pl, zoneJoin, -1, -1, x, y, NON_SPACE_SHIP);
    }

    public void changeMapYardrat(Player pl, Zone zoneJoin, int x, int y) {
        if (pl.idNRNM != -1) {
            Service.gI().sendThongBaoOK(pl, "Không thể thực hiện khi có ngọc rồng namec");
            return;
        }
        changeMap(pl, zoneJoin, -1, -1, x, y, TELEPORT_YARDRAT);
    }

    private void changeMap(Player pl, Zone zoneJoin, int mapId, int zoneId, int x, int y, byte typeSpace) {
        TransactionService.gI().cancelTrade(pl);
        if (zoneJoin == null) {
            if (mapId != -1) {
                zoneJoin = MapService.gI().getMapCanJoin(pl, mapId, zoneId);
            }
        }
        // Admin có thể đi full map không cần kiểm tra sức mạnh
        if (!pl.isAdmin()) {
            if (zoneJoin.map.mapId == 13 && pl.nPoint.power < 15000000L) {
                resetPoint(pl);
                Service.gI().sendThongBao(pl, "Sức Mạnh Của Bạn Chưa Đủ 15Tr Để Vào Bản Đồ Này!");
                return;
            }
            if (zoneJoin.map.mapId == 14 && pl.nPoint.power < 50000000L) {
                resetPoint(pl);
                Service.gI().sendThongBao(pl, "Sức Mạnh Của Bạn Chưa Đủ 50Tr Để Vào Bản Đồ Này!");
                return;
            }
            if (zoneJoin.map.mapId == 24 && pl.nPoint.power < 200000000L) {
                resetPoint(pl);
                Service.gI().sendThongBao(pl, "Sức Mạnh Của Bạn Chưa Đủ 200Tr Để Vào Bản Đồ Này!");
                return;
            }
            if (zoneJoin.map.mapId == 25 && pl.nPoint.power < 10000000000L) {
                resetPoint(pl);
                Service.gI().sendThongBao(pl, "Sức Mạnh Của Bạn Chưa Đủ 10 Tỷ Để Vào Bản Đồ Này!");
                return;
            }
            if (zoneJoin.map.mapId == 26 && pl.nPoint.power < 20000000000L) {
                resetPoint(pl);
                Service.gI().sendThongBao(pl, "Sức Mạnh Của Bạn Chưa Đủ 20 Tỷ Để Vào Bản Đồ Này!");
                return;
            }
            if (zoneJoin.map.mapId == 28 && pl.nPoint.power < 15000000000L) {
                resetPoint(pl);
                Service.gI().sendThongBao(pl, "Sức Mạnh Của Bạn Chưa Đủ 15 Tỷ Để Vào Bản Đồ Này!");
                return;
            }
        }
        if (!pl.isAdmin() && zoneJoin.map.mapId == 15 && pl.nPoint.power < 5000000000L) {
            resetPoint(pl);
            Service.gI().sendThongBao(pl, "Sức Mạnh Của Bạn Chưa Đủ 5 Tỷ Để Vào Bản Đồ Này!");
            return;
        }
        if ((zoneJoin.map.mapId == 179 || zoneJoin.map.mapId == 180 || zoneJoin.map.mapId == 181) && !pl.isBoss) {
            try {
                new LuyenTap(-(int) pl.id, BossesData.LuyenTap, zoneJoin);
            } catch (Exception e) {
                java.util.logging.Logger.getLogger(Service.class.getName()).log(Level.SEVERE, null, e);

            }
        }
        if (!pl.isBoss) {
            zoneJoin = checkMapCanJoin(pl, zoneJoin);
        }
        zoneJoin = checkMapCanJoin(pl, zoneJoin);
        if (zoneJoin != null) {
            boolean currMapIsCold = MapService.gI().isMapCold(pl.zone.map);
            boolean nextMapIsCold = MapService.gI().isMapCold(zoneJoin.map);
            if (typeSpace == AUTO_SPACE_SHIP) {
                spaceShipArrive(pl, (byte) 0, pl.haveTennisSpaceShip ? TENNIS_SPACE_SHIP : DEFAULT_SPACE_SHIP);
                pl.iDMark.setIdSpaceShip(pl.haveTennisSpaceShip ? TENNIS_SPACE_SHIP : DEFAULT_SPACE_SHIP);
            } else {
                pl.iDMark.setIdSpaceShip(typeSpace);
            }
            if (pl.effectSkill.isCharging) {
                EffectSkillService.gI().stopCharge(pl);
            }
            if (pl.effectSkill.useTroi) {
                EffectSkillService.gI().removeUseTroi(pl);
            }
            if (x != -1) {
                pl.location.x = x;
            } else {
                pl.location.x = Util.nextInt(100, zoneJoin.map.mapWidth - 100);
            }
            pl.location.y = y;
            this.goToMap(pl, zoneJoin);
            if (pl.pet != null) {
                pl.pet.joinMapMaster();
            }
            if (pl.clone != null) {
                pl.clone.joinMapMaster();
            }
            if (pl.TrieuHoipet != null) {
                pl.TrieuHoipet.joinMapMaster();
            }
            Service.gI().clearMap(pl);
            zoneJoin.mapInfo(pl); // -24
            pl.zone.load_Me_To_Another(pl);
            if (!pl.isBoss && !pl.isClone && !pl.isPet && !pl.isNewPet && !pl.isfake && !pl.isTrieuhoipet) {
                pl.timeChangeZone = System.currentTimeMillis();
                pl.zone.load_Another_To_Me(pl);
            }
            pl.iDMark.setIdSpaceShip(NON_SPACE_SHIP);
            if (currMapIsCold != nextMapIsCold) {
                if (!currMapIsCold && nextMapIsCold) {
                    Service.gI().sendThongBao(pl, "Lạnh Quá HuHu");
                    Service.gI().sendThongBao(pl, "Sức Tấn Công Và HP Của Bạn Bị Giảm 50% Vì Quá Lạnh");
                } else {
                    Service.gI().sendThongBao(pl, "Hết Lạnh Gòiii");
                    Service.gI().sendThongBao(pl, "Sức Tấn Công Và HP Của Bạn Đã Trở Lại Bình Thường");
                }
                Service.gI().point(pl);
                Service.gI().Send_Info_NV(pl);
            }
            checkJoinSpecialMap(pl);
            checkJoinMapMaBu(pl);
        } else {
            int plX = pl.location.x;
            if (pl.location.x >= pl.zone.map.mapWidth - 60) {
                plX = pl.zone.map.mapWidth - 60;
            } else if (pl.location.x <= 60) {
                plX = 60;
            }
            Service.gI().resetPoint(pl, plX, pl.location.y);
        }
    }

    // phước map thượng đế
    public void changeMapWaypoint(Player player) {
        Zone zoneJoin = null;
        WayPoint wp = null;
        int xGo = player.location.x;
        int yGo = player.location.y;
        if (player.zone.map.mapId == 29 || player.zone.map.mapId == 46) {
            int x = player.location.x;
            int y = player.location.y;
            if (x >= 35 && x <= 685 && y >= 550 && y <= 560) {
                xGo = player.zone.map.mapId == 29 ? 420 : 636;
                yGo = 150;
                zoneJoin = MapService.gI().getMapCanJoin(player, player.zone.map.mapId + 1, -1);
            }
        }
        if (zoneJoin == null) {
            wp = MapService.gI().getWaypointPlayerIn(player);
            if (wp != null) {
                zoneJoin = MapService.gI().getMapCanJoin(player, wp.goMap, -1);
                if (zoneJoin != null) {
                    xGo = wp.goX;
                    yGo = wp.goY;
                }
            }
        }
        if (zoneJoin != null) {
            if (player.idNRNM != -1 && !Util.canDoWithTime(player.lastTimePickNRNM, 6000)) {
                resetPoint(player);
                Service.gI().sendThongBao(player, "Ngọc rồng namec quá nặng vui lòng đợi một chút để qua map");
                return;
            }
            if (player.idNRNM != -1) {
                player.lastTimePickNRNM = System.currentTimeMillis();
            }
            changeMap(player, zoneJoin, -1, -1, xGo, yGo, NON_SPACE_SHIP);
        } else {
            resetPoint(player);
            Service.gI().sendThongBaoOK(player, "Không thể đến khu vực này");
        }

    }

    public void resetPoint(Player player) {
        int x = player.location.x;
        if (player.location.x >= player.zone.map.mapWidth - 60) {
            x = player.zone.map.mapWidth - 60;
        } else if (player.location.x <= 60) {
            x = 60;
        }
        Service.gI().resetPoint(player, x, player.location.y);
    }

    public void finishLoadMap(Player player) {
        sendEffectMapToMe(player);
        sendEffectMeToMap(player);
        TaskService.gI().checkDoneTaskGoToMap(player, player.zone);
        // Logger.log(Logger.CYAN, "Bạn " + player.name + " đang ở " +
        // player.zone.map.mapName + " khu " + player.zone.zoneId + "\n");
    }

    private void sendEffectMeToMap(Player player) {
        Message msg;
        try {
            if (player.effectSkill.isShielding) {
                msg = new Message(-124);
                msg.writer().writeByte(1);
                msg.writer().writeByte(0);
                msg.writer().writeByte(33);
                msg.writer().writeInt((int) player.id);
                Service.gI().sendMessAnotherNotMeInMap(player, msg);
                msg.cleanup();
            }

            if (player.mobMe != null) {
                msg = new Message(-95);
                msg.writer().writeByte(0);// type
                msg.writer().writeInt((int) player.id);
                msg.writer().writeShort(player.mobMe.tempId);
                msg.writer().writeDouble(Dragon.utils.Util.limitDouble(player.mobMe.point.gethp()));// hp mob
                Service.gI().sendMessAnotherNotMeInMap(player, msg);
                msg.cleanup();
            }
            if (player.pet != null && player.pet.mobMe != null) {
                msg = new Message(-95);
                msg.writer().writeByte(0);// type
                msg.writer().writeInt((int) player.pet.mobMe.id);
                msg.writer().writeShort(player.pet.mobMe.tempId);
                msg.writer().writeDouble(Dragon.utils.Util.limitDouble(player.pet.mobMe.point.gethp()));// hp mob
                Service.gI().sendMessAnotherNotMeInMap(player, msg);
                msg.cleanup();
            }
        } catch (Exception e) {

        }
    }

    private void sendEffectMapToMe(Player player) {
        Message msg;
        try {
            for (Mob mob : player.zone.mobs) {
                if (mob.isDie()) {
                    continue;
                }
                if (mob.effectSkill.isThoiMien) {
                    msg = new Message(-124);
                    msg.writer().writeByte(1); // b5
                    msg.writer().writeByte(1); // b6
                    msg.writer().writeByte(41); // num6
                    msg.writer().writeByte(mob.id); // b7
                    player.sendMessage(msg);
                    msg.cleanup();
                }
                if (mob.effectSkill.isSocola) {
                    msg = new Message(-112);
                    msg.writer().writeByte(1);
                    msg.writer().writeByte(mob.id); // b4
                    msg.writer().writeShort(4133);// b5
                    player.sendMessage(msg);
                    msg.cleanup();
                }
                if (mob.effectSkill.isStun || mob.effectSkill.isBlindDCTT) {
                    msg = new Message(-124);
                    msg.writer().writeByte(1);
                    msg.writer().writeByte(1);
                    msg.writer().writeByte(40);
                    msg.writer().writeByte(mob.id);
                    player.sendMessage(msg);
                    msg.cleanup();
                }
            }
        } catch (Exception e) {

        }
        try {
            List<Player> players = player.zone.getHumanoids();
            for (Player pl : players) {
                if (!player.equals(pl)) {
                    if (pl != null && pl.effectSkill != null) {
                        if (pl.effectSkill.isShielding) {
                            msg = new Message(-124);
                            msg.writer().writeByte(1);
                            msg.writer().writeByte(0);
                            msg.writer().writeByte(33);
                            msg.writer().writeInt((int) pl.id);
                            player.sendMessage(msg);
                            msg.cleanup();
                        }
                        if (pl.effectSkill.isThoiMien) {
                            msg = new Message(-124);
                            msg.writer().writeByte(1); // b5
                            msg.writer().writeByte(0); // b6
                            msg.writer().writeByte(41); // num3
                            msg.writer().writeInt((int) pl.id); // num4
                            player.sendMessage(msg);
                            msg.cleanup();
                        }
                        if (pl.effectSkill.isBlindDCTT || pl.effectSkill.isStun) {
                            msg = new Message(-124);
                            msg.writer().writeByte(1);
                            msg.writer().writeByte(0);
                            msg.writer().writeByte(40);
                            msg.writer().writeInt((int) pl.id);
                            msg.writer().writeByte(0);
                            msg.writer().writeByte(32);
                            player.sendMessage(msg);
                            msg.cleanup();
                        }
                        if (pl.effectSkill.useTroi) {
                            if (pl.effectSkill.plAnTroi != null) {
                                msg = new Message(-124);
                                msg.writer().writeByte(1); // b5
                                msg.writer().writeByte(0);// b6
                                msg.writer().writeByte(32);// num3
                                msg.writer().writeInt((int) pl.effectSkill.plAnTroi.id);// num4
                                msg.writer().writeInt((int) pl.id);// num9
                                player.sendMessage(msg);
                                msg.cleanup();
                            }
                            if (pl.effectSkill.mobAnTroi != null) {
                                msg = new Message(-124);
                                msg.writer().writeByte(1); // b4
                                msg.writer().writeByte(1);// b5
                                msg.writer().writeByte(32);// num8
                                msg.writer().writeByte(pl.effectSkill.mobAnTroi.id);// b6
                                msg.writer().writeInt((int) pl.id);// num9
                                player.sendMessage(msg);
                                msg.cleanup();
                            }
                        }
                        if (pl.mobMe != null) {
                            msg = new Message(-95);
                            msg.writer().writeByte(0);// type
                            msg.writer().writeInt((int) pl.id);
                            msg.writer().writeShort(pl.mobMe.tempId);
                            msg.writer().writeDouble(Dragon.utils.Util.limitDouble(pl.mobMe.point.gethp()));// hp mob
                            player.sendMessage(msg);
                            msg.cleanup();
                        }
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    public void spaceShipArrive(Player player, byte typeSendMSG, byte typeSpace) {
        Message msg;
        try {
            msg = new Message(-65);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeByte(typeSpace);
            switch (typeSendMSG) {
                case 0: // cho tất cả
                    Service.gI().sendMessAllPlayerInMap(player, msg);
                    break;
                case 1: // cho bản thân
                    player.sendMessage(msg);
                    break;
                case 2: // cho người chơi trong map
                    Service.gI().sendMessAnotherNotMeInMap(player, msg);
                    break;
            }
            msg.cleanup();
        } catch (Exception e) {

        }
    }

    public void goToMap(Player player, Zone zoneJoin) {
        if (zoneJoin == null || player == null) {
            return;
        }
        Zone oldZone = player.zone;
        if (oldZone != null) {
            this.exitMap(player);
            if (player.mobMe != null) {
                player.mobMe.goToMap(zoneJoin);
            }
        }
        player.zone = zoneJoin;
        player.zone.addPlayer(player);

        // Voice Chat Zone Integration
        // Voice chat zone change is now handled by VoiceChatService automatically
        // No need to manually notify voice chat service
    }

    public void exitMap(Player player) {
        if (player.zone != null) {
            // xử thua pvp
            if (player.pvp != null) {
                player.pvp.lose(player, TYPE_LOSE_PVP.RUNS_AWAY);
            }
            if (MapService.gI().isdiacung(player.zone.map.mapId)) {
                Dragon.thuongnhanthanbi.Dungeon_Manager.gI().onPlayerLeaveDungeon(player);
            }
            BlackBallWar.gI().dropBlackBall(player);
            if (player.effectSkill.useTroi) {
                EffectSkillService.gI().removeUseTroi(player);
            }
            if (player.effectSkin.xHPKI > 1) {
                player.effectSkin.xHPKI = 1;
                Service.gI().point(player);
            }
            player.zone.removePlayer(player);
            if (!MapService.gI().isMapOffline(player.zone.map.mapId)) {
                Message msg;
                try {
                    msg = new Message(-6);
                    msg.writer().writeInt((int) player.id);
                    Service.gI().sendMessAnotherNotMeInMap(player, msg);
                    msg.cleanup();
                    player.zone = null;
                } catch (Exception e) {
                    Logger.logException(MapService.class, e);
                }
            }
        }
    }

    public void goToTuongLai(Player player) {
        if (!player.iDMark.isGotoFuture()) {
            player.iDMark.setLastTimeGoToFuture(System.currentTimeMillis());
            player.iDMark.setGotoFuture(true);
            spaceShipArrive(player, (byte) 1, TELEPORT_YARDRAT);
            // effectChangeMap(player, 60, EFFECT_GO_TO_TUONG_LAI);
            ChangeMapService.this.changeMapInYard(player, 102, -1, -1);
        }
    }

    public void goToDiaNguc(Player player) {
        if (!player.iDMark.isGotoFuture()) {
            player.iDMark.setLastTimeGoToFuture(System.currentTimeMillis());
            player.iDMark.setGotoFuture(true);
            spaceShipArrive(player, (byte) 1, DEFAULT_SPACE_SHIP);
            effectChangeMap(player, 60, EFFECT_GO_TO_DIA_NGUC);
        }
    }

    public void goToGas(Player player) {
        if (!player.iDMark.isGoToGas()) {
            player.iDMark.setLastTimeGotoGas(System.currentTimeMillis());
            player.iDMark.setGoToGas(true);
            spaceShipArrive(player, (byte) 1, DEFAULT_SPACE_SHIP);
            effectChangeMap(player, 60, TELEPORT_YARDRAT);
        }
    }

    public void goToDBKB(Player player) {
        if (!player.iDMark.isGoToBDKB()) {
            player.iDMark.setLastTimeGoToBDKB(System.currentTimeMillis());
            player.iDMark.setGoToBDKB(false);
            spaceShipArrive(player, (byte) 1, TELEPORT_YARDRAT);
            // effectChangeMap(player, 60, EFFECT_GO_TO_BDKB);
            ChangeMapService.this.changeMapInYard(player, 135, -1, -1);
        }
    }

    public void goToQuaKhu(Player player) {
        ChangeMapService.this.changeMapBySpaceShip(player, 24, -1, -1);
    }

    public void goToPotaufeu(Player player) {
        ChangeMapService.this.changeMapBySpaceShip(player, 139, -1, Util.nextInt(60, 200));
    }

    private void effectChangeMap(Player player, int seconds, byte type) {
        Message msg;
        try {
            msg = new Message(-105);
            msg.writer().writeShort(seconds);
            msg.writer().writeByte(type);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {

        }
    }

    public Zone checkMapCanJoin(Player player, Zone zoneJoin) {
        if (zoneJoin == null) {
            return null;
        }
        if (zoneJoin.map.mapId == -1 || zoneJoin.map.mapId == -1) {
            return null;
        }
        if (player.isPet || player.isClone || player.isBoss || player.getSession() != null && player.isAdmin()
                || player.allowFullMapAccess) {
            return zoneJoin;
        }
        if (zoneJoin != null) {
            switch (zoneJoin.map.mapId) {
                case 1: // đồi hoa cúc
                case 8: // đồi nấm tím
                case 15: // đồi hoang
                    if (TaskService.gI().getIdTask(player) < ConstTask.TASK_2_0) {
                        Service.gI().sendThongBao(player, "Vui lòng hoàn thành nhiệm vụ trước khi tới đây!");
                        return null;
                    }
                    break;
                case 42: // vách aru
                case 43: // vách moori
                case 44: // vách kakarot
                    if (TaskService.gI().getIdTask(player) < ConstTask.TASK_3_1) {
                        Service.gI().sendThongBao(player, "Vui lòng hoàn thành nhiệm vụ trước khi tới đây!");
                        return null;
                    }
                    break;
                case 2: // thung lũng tre
                case 9: // thị trấn moori
                case 16: // làng plane
                    if (TaskService.gI().getIdTask(player) < ConstTask.TASK_4_0) {
                        Service.gI().sendThongBao(player, "Vui lòng hoàn thành nhiệm vụ trước khi tới đây!");
                        return null;
                    }
                    break;
                case 24: // trạm tàu vũ trụ trái đất
                case 25: // trạm tàu vũ trụ namếc
                case 26: // trạm tàu vũ trụ xayda
                    if (TaskService.gI().getIdTask(player) < ConstTask.TASK_6_0) {
                        Service.gI().sendThongBao(player, "Vui lòng hoàn thành nhiệm vụ trước khi tới đây!");
                        return null;
                    }
                    break;
                case 3: // rừng nấm
                case 11: // thung lũng maima
                case 17: // rừng nguyên sinh
                    if (TaskService.gI().getIdTask(player) < ConstTask.TASK_7_0) {
                        Service.gI().sendThongBao(player, "Vui lòng hoàn thành nhiệm vụ trước khi tới đây!");
                        return null;
                    }
                    break;
                case 27: // rừng bamboo
                case 28: // rừng dương xỉ
                case 31: // núi hoa vàng
                case 32: // núi hoa tím
                case 35: // rừng cọ
                case 36: // rừng đá
                    if (TaskService.gI().getIdTask(player) < ConstTask.TASK_14_0) {
                        Service.gI().sendThongBao(player, "Vui lòng hoàn thành nhiệm vụ trước khi tới đây!");
                        return null;
                    }
                    break;
                case 30: // đảo bulong
                case 34: // đông nam guru
                case 38: // bờ vực đen
                    if (TaskService.gI().getIdTask(player) < ConstTask.TASK_15_0) {
                        Service.gI().sendThongBao(player, "Vui lòng hoàn thành nhiệm vụ trước khi tới đây!");
                        return null;
                    }
                    break;
                case 6: // đông karin
                case 10: // thung lũng namếc
                case 19: // thành phố vegeta
                    if (TaskService.gI().getIdTask(player) < ConstTask.TASK_16_0) {
                        Service.gI().sendThongBao(player, "Vui lòng hoàn thành nhiệm vụ trước khi tới đây!");
                        return null;
                    }
                    break;
                case 68: // thung lũng nappa
                case 69: // vực cấm
                case 70: // núi appule
                case 71: // căn cứ rasphery
                case 72: // thung lũng rasphery
                case 64: // núi dây leo
                case 65: // núi cây quỷ
                    if (TaskService.gI().getIdTask(player) < ConstTask.TASK_18_0) {
                        Service.gI().sendThongBao(player, "Vui lòng hoàn thành nhiệm vụ trước khi tới đây!");
                        return null;
                    }
                    break;
                case 63: // trại lính fide
                case 66: // trại quỷ già
                case 67: // vực chết
                case 73: // thung lũng chết
                case 74: // đồi cây fide
                case 75: // khe núi tử thần
                case 76: // núi đá
                case 77: // rừng đá
                    if (TaskService.gI().getIdTask(player) < ConstTask.TASK_19_0) {
                        Service.gI().sendThongBao(player, "Vui lòng hoàn thành nhiệm vụ trước khi tới đây!");
                        return null;
                    }
                    break;
                case 81: // hang quỷ chim
                case 82: // núi khỉ đen
                case 83: // hang khỉ đen
                case 79: // núi khỉ đỏ
                    if (TaskService.gI().getIdTask(player) < ConstTask.TASK_20_1) {
                        Service.gI().sendThongBao(player, "Vui lòng hoàn thành nhiệm vụ trước khi tới đây!");
                        return null;
                    }
                    break;
                case 80: // núi khỉ vàng
                    if (TaskService.gI().getIdTask(player) < ConstTask.TASK_21_1) {
                        Service.gI().sendThongBao(player, "Vui lòng hoàn thành nhiệm vụ trước khi tới đây!");
                        return null;
                    }
                    break;
                case 105: // cánh đồng tuyết
                case 106: // rừng tuyết
                case 107: // núi tuyết
                case 108: // dòng sông băng
                case 109: // rừng băng
                case 110: // hang băng
                    if (TaskService.gI().getIdTask(player) < ConstTask.TASK_21_4) {
                        Service.gI().sendThongBao(player, "Vui lòng hoàn thành nhiệm vụ trước khi tới đây!");
                        return null;
                    }
                    break;
                case 102: // nhà bunma
                case 92: // thành phố phía đông
                case 93: // thành phố phía nam
                case 94: // đảo balê
                case 96: // cao nguyên
                case 97: // thành phố phía bắc
                case 98: // ngọn núi phía bắc
                case 99: // thung lũng phía bắc
                case 100: // thị trấn ginder
                    if (TaskService.gI().getIdTask(player) < ConstTask.TASK_20_0) {
                        Service.gI().sendThongBao(player, "Vui lòng hoàn thành nhiệm vụ trước khi tới đây!");
                        return null;
                    }
                    break;
                case 103: // võ đài xên
                    if (TaskService.gI().getIdTask(player) < ConstTask.TASK_25_0) {
                        Service.gI().sendThongBao(player, "Vui lòng hoàn thành nhiệm vụ trước khi tới đây!");
                        return null;
                    }
                    break;
                case 170: // Đảo SkyPiea
                    if (player.getSession().player.nPoint.power >= 50000L) {
                        Service.gI().sendThongBao(player, "Bạn chưa đủ sức mạnh để tới được khu vực này!");
                        return null;
                    }
                    break;

            }
        }
        if (zoneJoin != null) {
            switch (player.gender) {
                case ConstPlayer.TRAI_DAT:
                    if (zoneJoin.map.mapId == 99 || zoneJoin.map.mapId == 99) {
                        zoneJoin = null;
                    }
                    break;
                case ConstPlayer.NAMEC:
                    if (zoneJoin.map.mapId == 99 || zoneJoin.map.mapId == 99) {
                        zoneJoin = null;
                    }
                    break;
                case ConstPlayer.XAYDA:
                    if (zoneJoin.map.mapId == 99 || zoneJoin.map.mapId == 99) {
                        zoneJoin = null;
                    }
                    break;
            }
        }
        return zoneJoin;
    }

    private void checkJoinSpecialMap(Player player) {
        if (player != null && player.zone != null) {
            switch (player.zone.map.mapId) {
                // map ngọc rồng đen
                case 16:
                case 17:
                case 18:
                case 19:
                case 20:
                case 21:
                case 22:
                    BlackBallWar.gI().joinMapBlackBallWar(player);
                    break;
                case 36:
                    if (!Dungeon_Manager.gI().canPlayerJoinDungeon(player)) {
                        Service.gI().sendThongBao(player,
                                "Bạn đã hết lượt tham gia Địa Cung hôm nay! Vui lòng quay lại vào ngày mai.");
                        ChangeMapService.gI().changeMapBySpaceShip(player, 2, -1, 164);
                        return;
                    }
                    // Hiển thị thông tin số lần tham gia
                    String participationInfo = Dungeon_Manager.gI().getPlayerParticipationInfo(player);
                    Service.gI().sendThongBao(player, "Thông tin tham gia: " + participationInfo);
                    break;
            }
        }
    }

    private void checkJoinMapMaBu(Player player) {
        if (player != null && player.zone != null) {
            switch (player.zone.map.mapId) {
                // map mabu
                case 114:
                case 115:
                case 117:
                case 118:
                case 119:
                case 120:
                    MapMaBu.gI().joinMapMabu(player);
                    break;
            }
        }
    }

    public void changeMapNonSpaceship(Player player, int mapid, int x, int y) {
        Zone zone = getMapCanJoin(player, mapid);
        ChangeMapService.gI().changeMap(player, zone, -1, -1, x, y, NON_SPACE_SHIP);
    }

    public Zone getMapCanJoin(Player player, int mapId) {
        if (MapService.gI().isMapOffline(player.zone.map.mapId)) {
            return getZoneJoinByMapIdAndZoneId(player, mapId, 0);
        }
        Zone mapJoin = null;
        Map map = getMapById(mapId);
        for (Zone zone : map.zones) {
            if (zone.getNumOfPlayers() < Zone.PLAYERS_TIEU_CHUAN_TRONG_MAP) {
                mapJoin = zone;
                break;
            }
        }
        return mapJoin;
    }

    public Zone getZoneJoinByMapIdAndZoneId(Player player, int mapId, int zoneId) {
        Map map = getMapById(mapId);
        Zone zoneJoin = null;
        try {
            if (map != null) {
                zoneJoin = map.zones.get(zoneId);
            }
        } catch (Exception e) {

        }
        return zoneJoin;
    }

    public Map getMapById(int mapId) {
        for (Map map : Manager.MAPS) {
            if (map.mapId == mapId) {
                return map;
            }
        }
        return null;
    }
}
