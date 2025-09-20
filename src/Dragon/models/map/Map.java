package Dragon.models.map;

import Dragon.consts.ConstMap;
import Dragon.models.Template;
import Dragon.models.boss.Boss;
import Dragon.models.boss.BossID;
import Dragon.models.boss.BossManager;
import Dragon.models.map.BDKB.BanDoKhoBau;
import Dragon.models.map.MapMaBu.MapMaBu;
import Dragon.models.map.blackball.BlackBallWar;
import Dragon.models.map.doanhtrai.DoanhTrai;
import Dragon.models.map.doanhtrai.DoanhTraiService;
import Dragon.models.map.gas.Gas;
import Dragon.models.mob.Mob;
import Dragon.models.npc.Npc;
import Dragon.models.npc.NpcFactory;
import Dragon.models.player.Player;
import Dragon.server.Manager;
import Dragon.services.Service;
import Dragon.services.func.TopService;
import Dragon.utils.Util;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class Map {

    public static final byte T_EMPTY = 0;
    public static final byte T_TOP = 2;
    private static final int SIZE = 24;

    public int mapId;
    public String mapName;

    public byte planetId;
    public String planetName;

    public byte tileId;
    public byte bgId;
    public byte bgType;
    public byte type;

    private int[][] tileMap;
    public int[] tileTop;
    public int mapWidth;
    public int mapHeight;

    public List<Zone> zones;
    public List<WayPoint> wayPoints;
    public List<Npc> npcs;

    public Map(int mapId, String mapName, byte planetId,
            byte tileId, byte bgId, byte bgType, byte type, int[][] tileMap,
            int[] tileTop, int zones, int maxPlayer, List<WayPoint> wayPoints) {
        this.mapId = mapId;
        this.mapName = mapName;
        this.planetId = planetId;
        this.planetName = Service.getInstance().get_HanhTinh(planetId);
        this.tileId = tileId;
        this.bgId = bgId;
        this.bgType = bgType;
        this.type = type;
        this.tileMap = tileMap;
        this.tileTop = tileTop;
        this.zones = new ArrayList<>();
        this.wayPoints = wayPoints;
        try {
            this.mapHeight = tileMap.length * SIZE;
            this.mapWidth = tileMap[0].length * SIZE;
        } catch (Exception e) {

        }
        this.initZone(zones, maxPlayer);
        this.initItem();
        this.initTrapMap();
    }

    private void initZone(int nZone, int maxPlayer) {
        switch (this.type) {
            case ConstMap.MAP_OFFLINE:
                nZone = 1;
                break;
            case ConstMap.MAP_BLACK_BALL_WAR:
                nZone = BlackBallWar.AVAILABLE;
                break;
            case ConstMap.MAP_MA_BU:
                nZone = MapMaBu.AVAILABLE;
                break;
            case ConstMap.MAP_DOANH_TRAI:
                nZone = DoanhTrai.AVAILABLE;
                break;
            case ConstMap.MAP_BAN_DO_KHO_BAU:
                nZone = BanDoKhoBau.MAX_AVAILABLE;
                break;
            case ConstMap.MAP_KHI_GAS:
                nZone = Gas.MAX_AVAILABLE;
                break;
        }

        for (int i = 0; i < nZone; i++) {
            Zone zone = new Zone(this, i, maxPlayer);
            this.zones.add(zone);
            switch (this.type) {
                case ConstMap.MAP_DOANH_TRAI:
                    DoanhTraiService.gI().addMapDoanhTrai(i, zone);
                    break;
                case ConstMap.MAP_BAN_DO_KHO_BAU:
                    BanDoKhoBau.addZone(i, zone);
                    break;
                case ConstMap.MAP_KHI_GAS:
                    Gas.addZone(i, zone);
                    break;
            }
        }
    }

    public void initNpc(byte[] npcId, short[] npcX, short[] npcY) {
        this.npcs = new ArrayList<>();
        for (int i = 0; i < npcId.length; i++) {
            this.npcs.add(NpcFactory.createNPC(this.mapId, 1, npcX[i], npcY[i], npcId[i]));
        }
    }

    /**
     * Update method được gọi bởi GameLoopManager
     * Không còn implement Runnable nữa
     */
    public void update() {
        try {
            for (Zone zone : this.zones) {
                if (zone != null) {
                    zone.update();
                }
            }
        } catch (ConcurrentModificationException e) {
            // Ignore concurrent modification
        } catch (ArrayIndexOutOfBoundsException e) {
            // Ignore array bounds
        } catch (NoSuchElementException e) {
            // Ignore no such element
        } catch (Exception e) {
            // Log other exceptions
            Dragon.utils.Logger.logException(Map.class, e);
        }
    }

    public void initMob(byte[] mobTemp, byte[] mobLevel, double[] mobHp, short[] mobX, short[] mobY) {
        for (int i = 0; i < mobTemp.length; i++) {
            int mobTempId = mobTemp[i];
            Template.MobTemplate temp = Manager.getMobTemplateByTemp(mobTempId);
            if (temp != null) {
                Mob mob = new Mob();
                mob.id = i;
                mob.tempId = mobTemp[i];
                mob.level = mobLevel[i];
                mob.point.setHpFull(mobHp[i]);
                mob.location.x = mobX[i];
                mob.location.y = mobY[i];
                mob.point.sethp(mob.point.getHpFull());
                mob.pDame = temp.percentDame;
                mob.pTiemNang = temp.percentTiemNang;
                mob.setTiemNang();
                for (Zone zone : this.zones) {
                    Mob mobZone = new Mob(mob);
                    mobZone.zone = zone;
                    zone.mobs.add(mobZone);
                }
            }
        }
    }

    public void initMob(List<Mob> mobs) {
        for (Zone zone : zones) {
            for (Mob m : mobs) {
                Mob mob = new Mob(m);
                mob.zone = zone;
                zone.mobs.add(mob);
            }
        }
    }

    private void initTrapMap() {
        for (Zone zone : zones) {
            TrapMap trap = null;
            switch (this.mapId) {
                case 135:
                    trap = new TrapMap();
                    trap.x = 260;
                    trap.y = 960;
                    trap.w = 740;
                    trap.h = 72;
                    trap.effectId = 49; // xiên
                    zone.trapMaps.add(trap);
                    break;

            }
        }
    }

    private void initItem() {
        for (Zone zone : zones) {
            ItemMap itemMap = null;
            switch (this.mapId) {// phước ôm đồ từng map
                case 23:
                    itemMap = new ItemMap(zone, 74, 1, 633, 320, -1);
                    break;
                case 42:
                    itemMap = new ItemMap(zone, 78, 1, 70, 288, -1);
                    break;
                case 43:
                    itemMap = new ItemMap(zone, 78, 1, 70, 264, -1);
                    break;
                case 44:
                    itemMap = new ItemMap(zone, 78, 1, 70, 288, -1);
                    break;
                case 16: // 1 sao đen
                    itemMap = new ItemMap(zone, 372, 1, 0, 0, -1);
                    break;
                case 17: // 2 sao đen
                    itemMap = new ItemMap(zone, 373, 1, 0, 0, -1);
                    break;
                case 18: // 3 sao đen
                    itemMap = new ItemMap(zone, 374, 1, 0, 0, -1);
                    break;
                case 19: // 4 sao đen
                    itemMap = new ItemMap(zone, 375, 1, 0, 0, -1);
                    break;
                case 20: // 5 sao đen
                    itemMap = new ItemMap(zone, 376, 1, 0, 0, -1);
                    break;
                case 21: // 6 sao đen
                    itemMap = new ItemMap(zone, 377, 1, 0, 0, -1);
                    break;
                case 22: // 7 sao đen
                    itemMap = new ItemMap(zone, 378, 1, 0, 0, -1);
                    break;
            }
        }

    }

    public void initBoss() {
        // Boss từ SQL đã được load bởi RefactoredBossManager
        // Không cần load boss cũ từ hardcode nữa
        return;

        /*
         * for (Zone zone : zones) {
         * short bossId = -1;
         * switch (this.mapId) {
         * case 114:
         * bossId = BossID.DRABURA;
         * break;
         * case 115:
         * bossId = BossID.DRABURA_2;
         * break;
         * case 117:
         * bossId = BossID.BUI_BUI;
         * break;
         * case 118:
         * bossId = BossID.BUI_BUI_2;
         * break;
         * case 119:
         * bossId = BossID.YA_CON;
         * break;
         * case 120:
         * bossId = BossID.MABU_12H;
         * break;
         * case 46:
         * bossId = BossID.YARI;
         * break;
         * case 48:
         * bossId = BossID.BUBBLES;
         * }
         * if (bossId != -1) {
         * Boss boss = BossManager.gI().createBoss(bossId);
         * boss.zoneFinal = zone;
         * boss.joinMapByZone(zone);
         * }
         * }
         */
    }

    public short mapIdNextMabu(short mapId) {
        switch (mapId) {
            case 114:
                return 115;
            case 115:
                return 117;
            case 117:
                return 118;
            case 118:
                return 119;
            case 119:
                return 120;
            default:
                return -1;
        }
    }

    public Npc getNpc(Player player, int tempId) {
        for (Npc npc : npcs) {
            if (npc.tempId == tempId && Util.getDistance(player, npc) <= 60) {
                return npc;
            }
        }
        return null;
    }

    // --------------------------------------------------------------------------
    public int yPhysicInTop(int x, int y) {
        try {
            int rX = (int) x / SIZE;
            int rY = 0;
            if (isTileTop(tileMap[y / SIZE][rX])) {
                return y;
            }
            for (int i = y / SIZE; i < tileMap.length; i++) {
                if (isTileTop(tileMap[i][rX])) {
                    rY = i * SIZE;
                    break;
                }
            }
            return rY;
        } catch (Exception e) {

            return y;
        }
    }

    private boolean isTileTop(int tileMap) {
        for (int i = 0; i < tileTop.length; i++) {
            if (tileTop[i] == tileMap) {
                return true;
            }
        }
        return false;
    }
}
