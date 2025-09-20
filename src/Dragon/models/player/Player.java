package Dragon.models.player;

import Dragon.De2.Thu_TrieuHoi;
import Dragon.card.Card;
import Dragon.models.map.doanhtrai.DoanhTraiService;
import Dragon.models.map.MapMaBu.MapMaBu;
import Dragon.models.skill.PlayerSkill;

import java.util.List;

import Dragon.models.clan.Clan;
import Dragon.models.intrinsic.IntrinsicPlayer;
import Dragon.models.item.Item;
import Dragon.models.item.ItemTime;
import Dragon.models.npc.specialnpc.MagicTree;
import Dragon.consts.ConstPlayer;
import Dragon.consts.ConstTask;
import Dragon.models.npc.specialnpc.MabuEgg;
import Dragon.models.mob.MobMe;
import Dragon.data.DataGame;
//import Dragon.models.Event.CauCa;
//import Dragon.models.ThanhTich.ThanhTich;
import Dragon.models.clan.ClanMember;
import static Dragon.models.item.ItemTime.PHUOC_COUNT_NHIEM_VU;
import static Dragon.models.item.ItemTime.TEXT_NHAN_BUA_MIEN_PHI;
import Dragon.models.map.BDKB.BanDoKhoBauService;
import Dragon.models.map.TrapMap;
import Dragon.models.map.Zone;
//import Dragon.models.yadat.Yadat;
import Dragon.models.map.blackball.BlackBallWar;
import Dragon.thuongnhanthanbi.Dungeon_Manager;
import Dragon.models.map.gas.GasService;
import Dragon.models.matches.IPVP;
import Dragon.models.matches.TYPE_LOSE_PVP;
import Dragon.models.matches.TYPE_PVP;
import Dragon.models.mob.Mob;
//import Dragon.models.matches.pvp.DaiHoiVoThuat;
import Dragon.models.npc.specialnpc.BillEgg;
import Dragon.models.skill.Skill;
import Dragon.services.Service;
import Dragon.server.io.MySession;
import Dragon.models.task.TaskPlayer;
import com.girlkun.network.io.Message;
import Dragon.server.Client;
import Dragon.services.EffectSkillService;
import Dragon.services.FriendAndEnemyService;
import Dragon.services.ItemTimeService;
import Dragon.services.NpcService;
import Dragon.services.PetService;
import Dragon.services.PlayerService;
import Dragon.services.SkillService;
import Dragon.services.TaskService;
import Dragon.services.func.ChangeMapService;
import Dragon.services.func.ChonAiDay;
import Dragon.services.func.CombineNew;
import Dragon.services.func.SummonDragon;
import Dragon.utils.SkillUtil;
import Dragon.utils.Util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;

public class Player {

    public long rankSieuHang;
    public long numKillSieuHang;

    public boolean haveThienSu;
    public boolean haveDuongTang;
    public int mapCongDuc;

    public boolean isBot;
    public Clone clone; // Keep for backward compatibility
    public List<Clone> clones; // New list for multiple clones
    public boolean isClone;

    public long lastTimeTranformation;
    public int isbienhinh;
    public boolean lockPK;
    public Timer timerDHVT;
    public Player _friendGiaoDich;
    public int kemtraicay = 0;
    public int nuocmia = 0;

    public Date firstTimeLogin;
    public int luotNhanBuaMienPhi = 1;
    public int capboss = 0;
    public long lastTimeRevived1;
    public String TrieuHoiNamePlayer;
    public int CapBacThan = -1;
    public String TenThan;
    public int ThucAnThan;
    public long DameThan;
    public long MauThan;
    public long ThanLastTimeThucan;
    public int ThanLevel;
    public long ExpThan;
    public Player TrieuHoiPlayerAttack;
    public double DameThanthanmeo;
    public Thu_TrieuHoi TrieuHoipet;
    public boolean isTrieuhoipet;
    public boolean justRevived1;
    public long Autothucan;
    public boolean trangthai = false;

    public int ChuyenSinh;
    public long[] DauLaDaiLuc = new long[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    public long Exp_Tu_Ma = 0;
    public int Ma_Hoa = 0;
    public long BktLasttimeMaHoa;
    public byte PhiPham = 10;
    public long BktLasttimeMaLenh;
    public int Bkt_Tu_Ma = 0;
    public int Captutien = 0;
    public long Exptutien = 0;
    public int Ma_cot;
    public long[] TUTIEN = new long[] { 0, 0, 0 };
    public double dametong = 0;
    public String Hppl = "\n";
    public boolean resetdame = false;
    public long lastTimeDame;
    public long timevip;
    public long tutien;
    public byte vip;
    public byte CheckDayOnl;

    public int DuaHau;

    public boolean isdem = false;
    public long timeoff = 5;
    public boolean isTitleUse;
    public long lastTimeTitle1;
    public byte typetrain;
    public int expoff;
    public boolean istrain;
    public boolean istry;
    public boolean istry1;
    public boolean isfight;
    public boolean isfight1;
    public boolean isfake;
    public boolean seebossnotnpc;
    public boolean SetStart;
    public long LastStart;
    public int goldTai;
    public int goldXiu;
    public MySession session;
    public boolean beforeDispose;
    public Gift gift;
    // public List<ThanhTich> Archivement = new ArrayList<>();
    public boolean isPet;
    public boolean isNewPet;
    public int TimeOnline = 0;
    public boolean DoneDTDN = false;
    public boolean DoneDKB = false;
    public boolean JoinNRSD = false;
    public boolean DoneNRSD = false;
    public int TickCauCa = 0;
    public int NapNgay = 0;
    public int point_gapthu = 0;
    public int pointSb;
    public int topnv;
    public int topsm;
    public int topnap;
    public int sm;
    public long LastTimeOnline = System.currentTimeMillis() + 30000;
    public boolean tickxanh = false;
    public boolean isBoss;
    public boolean isYadat;
    public int NguHanhSonPoint = 0;
    public IPVP pvp;
    public int pointPvp;
    public long lastTimeCauCa;
    public int SuperAura;
    public int PointBoss;
    public int point_vnd;
    public int thankhi;
    public int blackballdata;
    public byte maxTime = 30;
    public byte type = 0;
    public int ResetSkill = 0;
    public int mapIdBeforeLogout;
    // public boolean tickxanh = false;
    public List<Zone> mapBlackBall;
    public List<Zone> mapdiacung;
    public List<Zone> mapMaBu;
    public long limitgold = 0;
    public long LastDoanhTrai = 0;
    public Zone zone;
    public Zone mapBeforeCapsule;
    public List<Zone> mapCapsule;
    public Pet pet;
    public NewPet newpet;
    public MobMe mobMe;
    public Location location;
    public SetClothes setClothes;
    public EffectSkill effectSkill;
    public MabuEgg mabuEgg;
    public BillEgg billEgg;
    public TaskPlayer playerTask;
    public ItemTime itemTime;
    public Fusion fusion;
    public MagicTree magicTree;
    public IntrinsicPlayer playerIntrinsic;
    public Inventory inventory;
    public PlayerSkill playerSkill;
    public CombineNew combineNew;
    public IDMark iDMark;
    public Charms charms;
    public EffectSkin effectSkin;
    public NPoint nPoint;
    public RewardBlackBall rewardBlackBall;
    public EffectFlagBag effectFlagBag;
    public FightMabu fightMabu;
    public SkillSpecial skillSpecial;
    public Clan clan;
    public ClanMember clanMember;
    public List<Friend> friends;
    public List<Enemy> enemies;

    public long id;
    public String name;
    public byte gender;
    public boolean isNewMember = true;
    public short head;
    public byte typePk;
    public byte cFlag;
    public boolean haveTennisSpaceShip;
    public boolean justRevived;
    public long lastTimeRevived;
    public boolean banv = false;
    public boolean muav = false;
    public long timeudbv = 0;
    public long timeudmv = 0;
    public long lasttimebanv;
    public long lasttimemuav;

    public int violate;
    public byte totalPlayerViolate;
    public long timeChangeZone;
    public long lastTimeUseOption;

    public short idNRNM = -1;
    public short idGo = -1;
    public long lastTimePickNRNM;
    public int goldNormar;
    public int goldVIP;
    public long lastTimeWin;
    public boolean isWin;
    public List<Card> Cards = new ArrayList<>();
    public short idAura = -1;
    public int vnd;
    public long diemdanh;
    public long diemdanhsk;
    public long leothap = 0;
    public int gioithieu;
    public int VND;
    public int tongnap;
    public int TONGNAP;
    public int levelWoodChest;
    public boolean receivedWoodChest;
    public int goldChallenge;
    public boolean bdkb_isJoinBdkb;
    public int rateCauCa;
    public int data_task;
    public List<Integer> idEffChar = new ArrayList<>();
    public int rateModifier;
    public boolean allowFullMapAccess = true;

    // GameLoop optimization - throttle player updates
    public long lastUpdateTime = 0;
    public long lastSaveTime = 0;

    public Player() {
        lastTimeUseOption = System.currentTimeMillis();
        location = new Location();
        nPoint = new NPoint(this);
        inventory = new Inventory();
        playerSkill = new PlayerSkill(this);
        setClothes = new SetClothes(this);
        effectSkill = new EffectSkill(this);
        fusion = new Fusion(this);
        playerIntrinsic = new IntrinsicPlayer();
        rewardBlackBall = new RewardBlackBall(this);
        effectFlagBag = new EffectFlagBag();
        fightMabu = new FightMabu(this);
        // ----------------------------------------------------------------------
        iDMark = new IDMark();
        combineNew = new CombineNew();
        playerTask = new TaskPlayer();
        friends = new ArrayList<>();
        enemies = new ArrayList<>();
        clones = new ArrayList<>(); // Initialize clones list
        itemTime = new ItemTime(this);
        charms = new Charms();
        effectSkin = new EffectSkin(this);
        skillSpecial = new SkillSpecial(this);
        gift = new Gift(this);
        // cauca = new CauCa(this);
    }

    public void CreatePet(String NamePet) {
        this.TenThan = NamePet;
        this.ThanLastTimeThucan = System.currentTimeMillis();
        this.ThucAnThan = 100;
        this.ThanLevel = 0;
        this.ExpThan = 0;
        this.CapBacThan = 0; // Util.nextInt(0, Util.nextInt(3, 10));
        this.DameThan = Util.GioiHannext(10000, 10000L + ((this.CapBacThan + 1) * 10000L));
        this.MauThan = Util.GioiHannext(100000, 100000L + ((this.CapBacThan + 1) * 50000L));
    }

    // --------------------------------------------------------------------------
    public boolean isDie() {
        if (this.nPoint != null) {
            return this.nPoint.hp <= 0;
        }
        return true;
    }

    // --------------------------------------------------------------------------
    public void setSession(MySession session) {
        this.session = session;
    }

    public void sendMessage(Message msg) {
        if (this.session != null) {
            session.sendMessage(msg);
        }
    }

    public MySession getSession() {
        return this.session;
    }

    public void chat(String text) {
        Service.gI().chat(this, text);
    }

    public boolean isPl() {
        return !isPet && !isBoss && !isNewPet && !isTrieuhoipet && !isClone && !isBot;
    }

    public void update() {
        if (this.isBot) {
            active();
        }
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (!this.beforeDispose && this != null && !this.isBot) {
            try {
                if (!iDMark.isBan()) {
                    if (this != null) {
                        if (nPoint != null) {
                            nPoint.update();
                        }
                        if (fusion != null) {
                            fusion.update();
                        }
                        if (this.isPl() && this.clan != null && this.clan.khiGas != null) {
                            GasService.gI().update(this);
                        }
                        if (this.isPl() && !this.isDie() && this.Captutien >= 50 && this.TUTIEN[2] >= 1
                                && this.TUTIEN[0] >= BktDieukiencanhgioi(
                                        Util.Ahwuocdz(this.TUTIEN[1]))) {
                            if (Util.isTrue(Bkttilecanhgioi(Util.Ahwuocdz(this.TUTIEN[1])), 100)) {
                                this.TUTIEN[0] -= BktDieukiencanhgioi(
                                        Util.Ahwuocdz(this.TUTIEN[1]));
                                this.TUTIEN[1]++;
                                Service.gI().sendThongBao(this, "Bạn đã thăng cảnh giới thành công lên: "
                                        + this.CapBacTuTien(Util.Ahwuocdz(this.TUTIEN[1])));
                            } else {
                                this.TUTIEN[0] -= BktDieukiencanhgioi(
                                        Util.Ahwuocdz(this.TUTIEN[1]));
                                if (Util.isTrue(20f, 100)) {
                                    this.Ma_cot += Util.nextInt(1, Util.nextInt(1, Util.nextInt(1, 5)));
                                    Service.gI().sendThongBaoOK(this, "Trong lúc tu tiên thất bại bạn nhận đc ma cốt");
                                }
                                Service.gI().sendThongBao(this,
                                        "Bạn đã thăng cảnh giới thất bại và bị mất Exp tu tiên, cảnh giới bạn vẫn ở: "
                                                + this.CapBacTuTien(Util.Ahwuocdz(this.TUTIEN[1])));
                                this.setDie(this);
                            }
                        }
                        if (effectSkin != null) {
                            effectSkill.update();
                        }
                        if (mobMe != null) {
                            mobMe.update();
                        }
                        if (effectSkin != null) {
                            effectSkin.update();
                        }
                        if (gift != null) {
                            gift.dispose();
                            gift = null;
                        }
                        if (pet != null) {
                            pet.update();
                        }
                        if (TrieuHoipet != null) {
                            TrieuHoipet.update();
                        }
                        if (newpet != null) {
                            newpet.update();
                        }
                        if (clone != null) {
                            clone.update();
                        }
                        if (magicTree != null) {
                            magicTree.update();
                        }
                        if (itemTime != null) {
                            itemTime.update();
                            if (this.itemTime.isdkhi = false) {
                                // Service.gI().setNotMonkey(this);
                                Service.gI().Send_Caitrang(this);
                                Service.gI().point(this);
                                PlayerService.gI().sendInfoHpMp(this);
                                Service.gI().Send_Info_NV(this);
                                Service.gI().sendInfoPlayerEatPea(this);
                            }
                        }
                        if (this.timevip != 0 && Util.canDoWithTime(this.timevip, 1000)) {
                            timevip = 0;
                            vip = 0;
                        }

                        if (this.isPl() && this.getSession().tongnap == 441) {
                            Service.gI().sendTitle(this, 214);
                        }

                        if (this.lastTimeTitle1 != 0 && Util.canDoWithTime(this.lastTimeTitle1, 6000)) {
                            lastTimeTitle1 = 0;
                            isTitleUse = false;
                        }
                        if (this.zone.map.mapId == 30) { // Nếu mapId là 30, luôn active day
                            SummonDragon.gI().activeDay(this);
                            isdem = false;
                        } else if (!isdem && (hour > 18 || hour < 5)) {
                            SummonDragon.gI().activeNight(this);
                            isdem = true;
                        } else if (isdem && (hour >= 5 && hour <= 18)) {
                            SummonDragon.gI().activeDay(this);
                            isdem = false;
                        }

                        // Thêm thông báo từ 17h đến 18h
                        if (hour >= 17 && hour < 18 && !hasSentDiacungNotification) {
                            sendThongBaoBenDuoi11("Phó Bản Địa Cung Đang Bắt Đầu!");
                            hasSentDiacungNotification = true; // Đánh dấu đã gửi thông báo
                        }

                        // Thêm thông báo từ 20h đến 21h
                        if (hour >= 20 && hour < 21 && !hasSentNgocRongNotification) {
                            sendThongBaoBenDuoi11("Ngọc Rồng Sao Đen Đang Mở!");
                            hasSentNgocRongNotification = true; // Đánh dấu đã gửi thông báo
                        }

                        // Reset cờ khi ra ngoài khoảng thời gian
                        if (hour < 17 || hour >= 18) {
                            hasSentDiacungNotification = false;
                        }
                        if (hour < 20 || hour >= 21) {
                            hasSentNgocRongNotification = false;
                        }

                        if (this.isPl()) {
                            Dungeon_Manager.gI().update(this);
                        }
                        if (this.isPl()) {
                            BlackBallWar.gI().update(this);
                            MapMaBu.gI().update(this);
                        }
                        if (this.isPl()) {
                            nhiemvuphuoc(this);
                        }
                        if (this.isPl()) {
                            updateEff(this);
                        }
                        if (this.isPl() && this.iDMark.isGoToGas()
                                && Util.canDoWithTime(this.iDMark.getLastTimeGotoGas(), 3000)) {
                            ChangeMapService.gI().changeMapBySpaceShip(this, 149, -1, 163);
                            this.iDMark.setGoToGas(false);
                        }

                        if (this != null && this.iDMark != null && !isBoss && this.iDMark.isGotoFuture()
                                && Util.canDoWithTime(this.iDMark.getLastTimeGoToFuture(), 6000)) {
                            ChangeMapService.gI().changeMapBySpaceShip(this, 102, -1, Util.nextInt(60, 200));
                            this.iDMark.setGotoFuture(false);
                        }
                        if (this.isPl() && this.iDMark != null && this.iDMark.isGoToBDKB()
                                && Util.canDoWithTime(this.iDMark.getLastTimeGoToBDKB(), 6000)) {
                            ChangeMapService.gI().changeMapBySpaceShip(this, 135, -1, 35);
                            this.iDMark.setGoToBDKB(true);
                        }
                        if (this.isPl() && this.TrieuHoipet == null && this.CapBacThan >= 0
                                && this.CapBacThan <= 10) {
                            PetService.Thu_TrieuHoi(this);
                        } else if (this.isPl() && this.TrieuHoipet != null && this.CapBacThan < 0
                                && this.CapBacThan > 10) {
                            ChangeMapService.gI().exitMap(this.TrieuHoipet);
                            TrieuHoipet.dispose();
                            TrieuHoipet = null;
                        }
                        if (this.isPl() && this.zone != null) {
                            TrapMap trap = this.zone.isInTrap(this);
                            if (trap != null) {
                                trap.doPlayer(this);
                            }
                        }
                        if (this.isPl() && this.inventory.itemsBody.get(7) != null) {
                            Item it = this.inventory.itemsBody.get(7);
                            if (it != null && it.isNotNullItem() && this.newpet == null) {
                                PetService.Pet2(this, it.template.head, it.template.body, it.template.leg);
                                Service.getInstance().point(this);
                            }
                        } else if (this.isPl() && newpet != null && !this.inventory.itemsBody.get(7).isNotNullItem()) {
                            newpet.dispose();
                            newpet = null;
                        }
                        if (this.isPl() && isWin && this.zone.map.mapId == 51
                                && Util.canDoWithTime(lastTimeWin, 2000)) {
                            ChangeMapService.gI().changeMapBySpaceShip(this, 52, 0, -1);
                            isWin = false;
                        }
                    } else {
                        if (Util.canDoWithTime(iDMark.getLastTimeBan(), 5000)) {
                            Client.gI().kickSession(session);
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    public void sendThongBaoBenDuoi11(String text) {
        Message msg;
        try {
            msg = new Message(93);
            msg.writer().writeUTF(text);
            Service.gI().sendMessAllPlayer(msg);
            msg.cleanup();
        } catch (Exception e) {

        }
    }

    public long lastTimeSendTextTime;

    public void nhiemvuphuoc(Player player) {
        if (Util.canDoWithTime(lastTimeSendTextTime, 60000)) {
            if (player.playerTask.sideTask.template != null) {
                ItemTimeService.gI().sendTextTime(this, PHUOC_COUNT_NHIEM_VU,
                        "Nhiệm Vụ: " + player.playerTask.sideTask.getName(), 86400);
            } else {
                return;
            }
            lastTimeSendTextTime = System.currentTimeMillis();
        }
    }

    public void updateEff(Player player) {
        try {
            if (player.nPoint != null && player.inventory.itemsBody.size() >= 17) {
                for (int i = 12; i <= 16; i++) {
                    Item item = player.inventory.itemsBody.get(i);
                    if (item.isNotNullItem()) {
                        Service.gI().addEffectChar(player, (short) item.template.part, 1, -1, -1, 1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void upSkill(byte id, short cost) {

        int tempId = this.playerSkill.skills.get(id).template.id;
        int level = this.playerSkill.skills.get(id).point + 1;
        if (level > 7) {
            Service.gI().sendThongBao((Player) this, "Kĩ năng đã đạt cấp tối đa!");
        } else if (((Player) this).inventory.gem < cost) {
            Service.gI().sendThongBao((Player) this, "Bạn không đủ ngọc để nâng cấp!");
        } else {
            Skill skill = null;
            try {
                skill = SkillUtil.nClassTD.getSkillTemplate(tempId).skillss.get(level - 1);
            } catch (Exception e) {
                try {
                    skill = SkillUtil.nClassNM.getSkillTemplate(tempId).skillss.get(level - 1);
                } catch (Exception ex) {
                    skill = SkillUtil.nClassXD.getSkillTemplate(tempId).skillss.get(level - 1);
                }
            }
            skill = new Skill(skill);
            if (id == 1) {
                skill.coolDown = 1000;
            }
            this.playerSkill.skills.set(id, skill);
            ((Player) this).inventory.gem -= cost;
            Service.gI().sendMoney((Player) this);
            Service.gI().player((Player) this);

        }

    }

    // --------------------------------------------------------------------------
    /*
     * {380, 381, 382}: ht lưỡng long nhất thể xayda trái đất
     * {383, 384, 385}: ht porata xayda trái đất
     * {391, 392, 393}: ht namếc
     * {870, 871, 872}: ht c2 trái đất
     * {873, 874, 875}: ht c2 namếc
     * {867, 878, 869}: ht c2 xayda
     */
    private static final short[][] idOutfitFusion = {
            /* 0 */{ 380, 381, 382 }, // luong long
            /* 1 */ { 383, 384, 385 }, // porata
            /* 2 */ { 391, 392, 393 }, // hop the chung namec
            /* 3 */ { 870, 871, 872 }, // trai dat c2
            /* 4 */ { 873, 874, 875 }, // namec c2
            /* 5 */ { 867, 868, 869 }, // xayda c2
            //
            /* 6 */ { 1264, 1265, 1266 }, // td 3
            /* 7 */ { 1270, 1271, 1272 }, // nm 3
            /* 8 */ { 1267, 1268, 1269 }, // xd 3
            //
            /* 9 */ { 2104, 2105, 2106 }, // td 4
            /* 10 */ { 2101, 2102, 2103 }, // nm 4
            /* 11 */ { 2098, 2099, 2100 }, // xd 4
            //
            /* 12 */ { 1738, 1741, 1742 }
    };

    private boolean hasSentMysteryNotification = false;
    private boolean hasSentDiacungNotification = false;
    private boolean hasSentNgocRongNotification = false;

    public byte getEffFront() {
        if (this.inventory.itemsBody.isEmpty() || this.inventory.itemsBody.size() < 10) {
            return -1;
        }
        int levelAo = 0;
        Item.ItemOption optionLevelAo = null;
        int levelQuan = 0;
        Item.ItemOption optionLevelQuan = null;
        int levelGang = 0;
        Item.ItemOption optionLevelGang = null;
        int levelGiay = 0;
        Item.ItemOption optionLevelGiay = null;
        int levelNhan = 0;
        Item.ItemOption optionLevelNhan = null;
        Item itemAo = this.inventory.itemsBody.get(0);
        Item itemQuan = this.inventory.itemsBody.get(1);
        Item itemGang = this.inventory.itemsBody.get(2);
        Item itemGiay = this.inventory.itemsBody.get(3);
        Item itemNhan = this.inventory.itemsBody.get(4);
        for (Item.ItemOption io : itemAo.itemOptions) {
            if (io.optionTemplate.id == 72) {
                levelAo = io.param;
                optionLevelAo = io;
                break;
            }
        }
        for (Item.ItemOption io : itemQuan.itemOptions) {
            if (io.optionTemplate.id == 72) {
                levelQuan = io.param;
                optionLevelQuan = io;
                break;
            }
        }
        for (Item.ItemOption io : itemGang.itemOptions) {
            if (io.optionTemplate.id == 72) {
                levelGang = io.param;
                optionLevelGang = io;
                break;
            }
        }
        for (Item.ItemOption io : itemGiay.itemOptions) {
            if (io.optionTemplate.id == 72) {
                levelGiay = io.param;
                optionLevelGiay = io;
                break;
            }
        }
        for (Item.ItemOption io : itemNhan.itemOptions) {
            if (io.optionTemplate.id == 72) {
                levelNhan = io.param;
                optionLevelNhan = io;
                break;
            }
        }
        if (optionLevelAo != null && optionLevelQuan != null && optionLevelGang != null && optionLevelGiay != null
                && optionLevelNhan != null
                && levelAo >= 8 && levelQuan >= 8 && levelGang >= 8 && levelGiay >= 8 && levelNhan >= 8) {
            return 8;
        } // else if (optionLevelAo != null && optionLevelQuan != null && optionLevelGang
          // != null && optionLevelGiay != null && optionLevelNhan != null
          // && levelAo >= 7 && levelQuan >= 7 && levelGang >= 7 && levelGiay >= 7 &&
          // levelNhan >= 7) {
          // return 7;
          // } else if (optionLevelAo != null && optionLevelQuan != null &&
          // optionLevelGang != null && optionLevelGiay != null && optionLevelNhan != null
          // && levelAo >= 6 && levelQuan >= 6 && levelGang >= 6 && levelGiay >= 6 &&
          // levelNhan >= 6) {
          // return 6;
          // } else if (optionLevelAo != null && optionLevelQuan != null &&
          // optionLevelGang != null && optionLevelGiay != null && optionLevelNhan != null
          // && levelAo >= 5 && levelQuan >= 5 && levelGang >= 5 && levelGiay >= 5 &&
          // levelNhan >= 5) {
          // return 5;
          // } else if (optionLevelAo != null && optionLevelQuan != null &&
          // optionLevelGang != null && optionLevelGiay != null && optionLevelNhan != null
          // && levelAo >= 4 && levelQuan >= 4 && levelGang >= 4 && levelGiay >= 4 &&
          // levelNhan >= 4) {
          // return 4;
          // }
        else {
            return -1;
        }
    }

    public short getHead() {
        if (effectSkill != null && effectSkill.isMonkey) {
            return (short) ConstPlayer.HEADMONKEY[effectSkill.levelMonkey - 1];
        }
        if (effectSkill != null) {
            if (effectSkill.isTranformation) {
                switch (this.gender) {
                    case 0:
                        ItemTimeService.gI().sendItemTime(this, 20958, this.effectSkill.timeTranformation / 1000);
                        return 1764;
                    case 1:
                        ItemTimeService.gI().sendItemTime(this, 20964, this.effectSkill.timeTranformation / 1000);
                        return 1781;
                    case 2:
                        ItemTimeService.gI().sendItemTime(this, 20952, this.effectSkill.timeTranformation / 1000);
                        return 1801;
                    default:
                }
            }
        }
        if (effectSkill != null) {
            if (effectSkill.isEvolution) {
                switch (this.gender) {
                    case 0:
                        switch (this.isbienhinh) {
                            case 1:
                                ItemTimeService.gI().removeItemTime(this, 20958);
                                ItemTimeService.gI().sendItemTime(this, 20959,
                                        this.effectSkill.timeTranformation / 1000);
                                return 1764;
                            case 2:
                                ItemTimeService.gI().removeItemTime(this, 20959);
                                ItemTimeService.gI().sendItemTime(this, 20960,
                                        this.effectSkill.timeTranformation / 1000);
                                return 1767;
                            case 3:
                                ItemTimeService.gI().removeItemTime(this, 20960);
                                ItemTimeService.gI().sendItemTime(this, 20961,
                                        this.effectSkill.timeTranformation / 1000);
                                return 1770;
                            case 4:
                                ItemTimeService.gI().removeItemTime(this, 20961);
                                ItemTimeService.gI().sendItemTime(this, 20962,
                                        this.effectSkill.timeTranformation / 1000);
                                return 1773;
                            case 5:
                                ItemTimeService.gI().removeItemTime(this, 20962);
                                ItemTimeService.gI().sendItemTime(this, 20963,
                                        this.effectSkill.timeTranformation / 1000);
                                return 1776;
                            default:
                        }
                    case 1:
                        switch (this.isbienhinh) {
                            case 1:
                                ItemTimeService.gI().removeItemTime(this, 20964);
                                ItemTimeService.gI().sendItemTime(this, 20965,
                                        this.effectSkill.timeTranformation / 1000);
                                return 1781;
                            case 2:
                                ItemTimeService.gI().removeItemTime(this, 20965);
                                ItemTimeService.gI().sendItemTime(this, 20966,
                                        this.effectSkill.timeTranformation / 1000);
                                return 1784;
                            case 3:
                                ItemTimeService.gI().removeItemTime(this, 20966);
                                ItemTimeService.gI().sendItemTime(this, 20967,
                                        this.effectSkill.timeTranformation / 1000);
                                return 1787;
                            case 4:
                                ItemTimeService.gI().removeItemTime(this, 20967);
                                ItemTimeService.gI().sendItemTime(this, 20968,
                                        this.effectSkill.timeTranformation / 1000);
                                return 1790;
                            case 5:
                                ItemTimeService.gI().removeItemTime(this, 20968);
                                ItemTimeService.gI().sendItemTime(this, 20969,
                                        this.effectSkill.timeTranformation / 1000);
                                return 1793;

                            default:
                        }

                    case 2:
                        switch (this.isbienhinh) {
                            case 1:
                                ItemTimeService.gI().removeItemTime(this, 20952);
                                ItemTimeService.gI().sendItemTime(this, 20953,
                                        this.effectSkill.timeTranformation / 1000);
                                return 1801;
                            case 2:
                                ItemTimeService.gI().removeItemTime(this, 20953);
                                ItemTimeService.gI().sendItemTime(this, 20954,
                                        this.effectSkill.timeTranformation / 1000);
                                return 1804;
                            case 3:
                                ItemTimeService.gI().removeItemTime(this, 20954);
                                ItemTimeService.gI().sendItemTime(this, 20955,
                                        this.effectSkill.timeTranformation / 1000);
                                return 1807;
                            case 4:
                                ItemTimeService.gI().removeItemTime(this, 20955);
                                ItemTimeService.gI().sendItemTime(this, 20956,
                                        this.effectSkill.timeTranformation / 1000);
                                return 1810;
                            case 5:
                                ItemTimeService.gI().removeItemTime(this, 20956);
                                ItemTimeService.gI().sendItemTime(this, 20957,
                                        this.effectSkill.timeTranformation / 1000);
                                return 1813;
                            default:
                        }

                    default:
                }
            }
        }
        if (effectSkill != null && itemTime.isdkhi) {
            return (short) 1285;
        } else if (this.effectSkill.isBienHinh) {
            return 180;
        } else if (effectSkill != null && effectSkill.isSocola) {
            return 412;
        } else if (effectSkill != null && effectSkill.isMaPhongBa) {
            return 1410;
        } else if (effectSkin != null && effectSkin.isHoaDa) {
            return 406;
        } else if (fusion != null && fusion.typeFusion != ConstPlayer.NON_FUSION) {
            if (fusion.typeFusion == ConstPlayer.LUONG_LONG_NHAT_THE) {
                return idOutfitFusion[this.gender == ConstPlayer.NAMEC ? 2 : 0][0];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA) {
                if (this.pet.typePet == 1) {
                    return idOutfitFusion[3 + this.gender][0];
                }
                return idOutfitFusion[this.gender == ConstPlayer.NAMEC ? 2 : 1][0];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2) {
                if (this.pet.typePet == 1) {
                    return idOutfitFusion[3 + this.gender][0];
                }
                return idOutfitFusion[3 + this.gender][0];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3) {
                if (this.pet.typePet == 1) {
                    return idOutfitFusion[6 + this.gender][0];
                }
                return idOutfitFusion[6 + this.gender][0];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA4) {
                if (this.pet.typePet == 1) {
                    return idOutfitFusion[9 + this.gender][0];
                }
                return idOutfitFusion[9 + this.gender][0];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_GOGETA) {
                return idOutfitFusion[12][0];
            }
        } else if (inventory != null && inventory.itemsBody.get(5).isNotNullItem()) {
            int head = inventory.itemsBody.get(5).template.head;
            if (head != -1) {
                return (short) head;
            }
        }
        return this.head;
    }

    public short getBody() {
        if (effectSkill != null) {
            if (effectSkill.isTranformation || effectSkill.isEvolution) {
                switch (this.gender) {
                    case 0:
                        return 1779;
                    case 1:
                        return 1799;
                    case 2:
                        return 1816;
                    default:
                }
            }
        }
        if (effectSkill != null && effectSkill.isMonkey) {
            return 193;
        }
        if (effectSkill != null && itemTime.isdkhi) {
            return (short) 1286;
        } else if (effectSkill != null && effectSkill.isSocola) {
            return 413;
        } else if (this.effectSkill.isBienHinh) {
            return 181;
        } else if (effectSkill != null && effectSkill.isMaPhongBa) {
            return 1411;
            // } else if (effectSkill.isBang) {
            // return 1252;
            // } else if (effectSkill.isDa) {
            // return 455;
            // } else if (effectSkill.isCarot) {
            // return 407;
        } else if (effectSkin != null && effectSkin.isHoaDa) {
            return 407;
        } else if (fusion != null && fusion.typeFusion != ConstPlayer.NON_FUSION) {
            if (fusion.typeFusion == ConstPlayer.LUONG_LONG_NHAT_THE) {
                return idOutfitFusion[this.gender == ConstPlayer.NAMEC ? 2 : 0][1];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA) {
                if (this.pet.typePet == 1) {
                    return idOutfitFusion[3 + this.gender][1];
                }
                return idOutfitFusion[this.gender == ConstPlayer.NAMEC ? 2 : 1][1];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2) {
                if (this.pet.typePet == 1) {
                    return idOutfitFusion[3 + this.gender][1];
                }
                return idOutfitFusion[3 + this.gender][1];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3) {
                if (this.pet.typePet == 1) {
                    return idOutfitFusion[6 + this.gender][1];
                }
                return idOutfitFusion[6 + this.gender][1];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA4) {
                if (this.pet.typePet == 1) {
                    return idOutfitFusion[9 + this.gender][1];
                }
                return idOutfitFusion[9 + this.gender][1];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_GOGETA) {
                return idOutfitFusion[12][1];
            }
        } else if (inventory != null && inventory.itemsBody.get(5).isNotNullItem()) {
            int body = inventory.itemsBody.get(5).template.body;
            if (body != -1) {
                return (short) body;
            }
        }
        if (inventory != null && inventory.itemsBody.get(0).isNotNullItem()) {
            return inventory.itemsBody.get(0).template.part;
        }
        return (short) (gender == ConstPlayer.NAMEC ? 59 : 57);
    }

    public short getLeg() {
        if (effectSkill != null) {
            if (effectSkill.isTranformation || effectSkill.isEvolution) {
                switch (this.gender) {
                    case 0:
                        return 1780;
                    case 1:
                        return 1800;
                    case 2:
                        return 1817;
                    default:
                }
            }
        }
        if (effectSkill != null && effectSkill.isMonkey) {
            return 194;
        }
        if (effectSkill != null && itemTime.isdkhi) {
            return (short) 1287;
        } else if (this.effectSkill.isBienHinh) {
            return 182;
        } else if (effectSkill != null && effectSkill.isMaPhongBa) {
            return 1412;
        } else if (effectSkill != null && effectSkill.isSocola) {
            return 414;
            // } else if (effectSkill.isBang) {
            // return 1253;
            // } else if (effectSkill.isDa) {
            // return 456;
            // } else if (effectSkill.isCarot) {
            // return 408;
        } else if (effectSkin != null && effectSkin.isHoaDa) {
            return 408;
        } else if (fusion != null && fusion.typeFusion != ConstPlayer.NON_FUSION) {
            if (fusion.typeFusion == ConstPlayer.LUONG_LONG_NHAT_THE) {
                return idOutfitFusion[this.gender == ConstPlayer.NAMEC ? 2 : 0][2];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA) {
                if (this.pet.typePet == 1) {
                    return idOutfitFusion[3 + this.gender][2];
                }
                return idOutfitFusion[this.gender == ConstPlayer.NAMEC ? 2 : 1][2];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2) {
                if (this.pet.typePet == 1) {
                    return idOutfitFusion[3 + this.gender][2];
                }
                return idOutfitFusion[3 + this.gender][2];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3) {
                if (this.pet.typePet == 1) {
                    return idOutfitFusion[6 + this.gender][2];
                }
                return idOutfitFusion[6 + this.gender][2];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA4) {
                if (this.pet.typePet == 1) {
                    return idOutfitFusion[9 + this.gender][2];
                }
                return idOutfitFusion[9 + this.gender][2];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_GOGETA) {
                return idOutfitFusion[12][2];
            }
        } else if (inventory != null && inventory.itemsBody.get(5).isNotNullItem()) {
            int leg = inventory.itemsBody.get(5).template.leg;
            if (leg != -1) {
                return (short) leg;
            }
        }
        if (inventory != null && inventory.itemsBody.get(1).isNotNullItem()) {
            return inventory.itemsBody.get(1).template.part;
        }
        return (short) (gender == 1 ? 60 : 58);
    }

    public byte getAura() {
        if (this.inventory.itemsBody.isEmpty()
                || this.inventory.itemsBody.size() < 10) {
            return -1;
        }
        Item item = this.inventory.itemsBody.get(5); // Gốc là 8
        if (!item.isNotNullItem()) {
            return 0;
        }
        if (this.effectSkill != null && this.effectSkill.isTranformation) {
            return 12;
        }
        if (item.template.id == 1479) {
            return 39;
        }
        if (item.template.id == 1485) {
            return 40;
        }
        if (item.template.id == 1477) {
            return 34;
        }
        if (item.template.id == 1459) {
            return 38;
        }
        if (item.template.id == 1340) {
            return 20;
        }
        if (item.template.id == 1336) {
            return 36;
        }
        if (item.template.id == 2075) {
            return 55;
        }
        // phước aura cho cải trang img_by_name\x4
        if (item.template.id == 1651) {
            return 7;
        }
        if (item.template.id == 1650) {
            return 8;
        }
        if (item.template.id == 1765) {
            return 55;
        }
        // ------------
        if (item.template.id == 2074) {
            return 22;
        } else {
            return -1;
        }

    }

    public short getFlagBag() {
        if (this.iDMark.isHoldBlackBall()) {
            return 31;
        } else if (this.idNRNM >= 353 && this.idNRNM <= 359) {
            return 30;
        }
        if (this.inventory.itemsBody.size() >= 11) {
            if (this.inventory.itemsBody.get(8).isNotNullItem()) {
                return this.inventory.itemsBody.get(8).template.part;
            }
        }
        if (TaskService.gI().getIdTask(this) == ConstTask.TASK_3_2) {
            return 28;
        }
        if (this.clan != null) {
            return (short) this.clan.imgId;
        }
        return -1;
    }

    public short getMount() {
        if (this.inventory.itemsBody.isEmpty() || this.inventory.itemsBody.size() < 10) {
            return -1;
        }
        Item item = this.inventory.itemsBody.get(9);
        if (!item.isNotNullItem()) {
            return -1;
        }
        if (item.template.type == 24) {
            if (item.template.gender == 3 || item.template.gender == this.gender) {
                return item.template.id;
            } else {
                return -1;
            }
        } else {
            if (item.template.id < 500) {
                return item.template.id;
            } else {
                return (short) DataGame.MAP_MOUNT_NUM.get(String.valueOf(item.template.id));
            }
        }

    }

    public String NameThanthu(int CapBac) {
        switch (CapBac) {
            default:
                return "THẦN";
        }
    }

    public String DaDotpha(int CapBac) {
        switch (CapBac) {
            case 9:
                return "Đế Vương Thạch";
            case 8:
                return "Hỏa Hồn Thạch";
            case 7:
                return "Thiên Mệnh Thạch";
            case 6:
                return "Huyết Tinh Thạch";
            case 5:
                return "Linh Vân Thạch";
            case 4:
                return "Mịch Lâm Thạch";
            default:
                return "Thiên Nguyệt thạch";
        }
    }

    public String TrieuHoiKiNang(int CapBac) {
        switch (CapBac) {
            case 10:
                return "Tìm " + ((ThanLevel + 1) * 1) + " Hồng ngọc cho Chủ nhân\n"
                        + "Tăng " + (ThanLevel + 1) + "% HP, KI, Giáp, SD, SD chí mạng cho Chủ nhân\n";
            case 9:
                return "Tăng " + ((ThanLevel + 1) / 2) + "% HP, KI, Giáp, SD, SD chí mạng cho Chủ nhân";
            case 8:
                return "Tăng " + ((ThanLevel + 1) / 5) + "% HP, KI, Giáp, SD cho Chủ nhân";
            case 7:
                return "Tăng " + ((ThanLevel + 1) / 6) + "% HP, KI, Giáp, SD, SD chí mạng cho Chủ nhân";
            case 6:
                return "Tăng " + ((ThanLevel + 1) / 7) + "% HP, KI, Giáp, SD, SD chí mạng cho Chủ nhân";
            case 5:
                return "Tăng " + ((ThanLevel + 1) / 7) + "% HP, KI, Giáp, SD, SD chí mạng cho Chủ nhân";
            case 4:
                return "Tăng " + ((ThanLevel + 1) * 30) + " HP, KI, SD, Giáp cho Chủ nhân";
            case 3:
                return "Tăng " + ((ThanLevel + 1) * 20) + " HP, KI\n" + ((ThanLevel + 1) * 10) + " SD cho Chủ nhân";
            case 2:
                return "Tăng " + ((ThanLevel + 1) * 10) + " Sức đánh cho Chủ nhân";
            case 1:
                return "Tăng " + ((ThanLevel + 1) * 20) + " KI cho Chủ nhân";
            case 0:
                return "Tăng " + ((ThanLevel + 1) * 20) + " HP cho Chủ nhân";
            default:
                return "Phế vật mà làm được gì !!!";
        }
    }

    // --------------------------------------------------------------------------
    private <T extends Number> T processInjured(Player plAtt, T damage, boolean piercing, boolean isMobAttack,
            boolean isLongType) {
        if (!this.isDie()) {
            if (plAtt != null) {
                switch (plAtt.playerSkill.skillSelect.template.id) {
                    case Skill.KAMEJOKO:
                    case Skill.MASENKO:
                    case Skill.ANTOMIC:
                        if (this.nPoint.voHieuChuong > 0) {
                            double healAmount = damage.doubleValue() * this.nPoint.voHieuChuong / 100;
                            Dragon.services.PlayerService.gI().hoiPhuc(this, 0, healAmount);
                            return isLongType ? (T) Long.valueOf(0L) : (T) Double.valueOf(0.0);
                        }
                }
            }
            if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 100)) {
                return isLongType ? (T) Long.valueOf(0L) : (T) Double.valueOf(0.0);
            }
            double finalDamage = this.nPoint.subDameInjureWithDeff(damage.doubleValue());
            if (!piercing && effectSkill.isShielding) {
                if (finalDamage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                finalDamage = 1;
            }
            if (isMobAttack && this.charms.tdBatTu > System.currentTimeMillis() && finalDamage >= this.nPoint.hp) {
                finalDamage = this.nPoint.hp - 1;
            }
            this.nPoint.subHP(finalDamage);
            if (isDie()) {
                if (this != null && this.zone != null && this.zone.map != null) {
                    if (this.zone.map.mapId == 112 && plAtt != null) {
                        plAtt.pointPvp++;
                    }
                }
                setDie(plAtt);
            }

            return isLongType ? (T) Long.valueOf((long) finalDamage) : (T) Double.valueOf(finalDamage);
        } else {
            if (this.isClone) {
                ChangeMapService.gI().exitMap(this);
            }
            return isLongType ? (T) Long.valueOf(0L) : (T) Double.valueOf(0.0);
        }
    }

    public long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        return processInjured(plAtt, damage, piercing, isMobAttack, true);
    }

    public double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        return processInjured(plAtt, damage, piercing, isMobAttack, false);
    }

    public void setDie(Player plAtt) {
        // xóa phù
        if (this.effectSkin.xHPKI > 1) {
            this.effectSkin.xHPKI = 1;
            Service.gI().point(this);
        }
        // xóa tụ skill đặc biệt
        this.playerSkill.prepareQCKK = false;
        this.playerSkill.prepareLaze = false;
        this.playerSkill.prepareTuSat = false;
        // xóa hiệu ứng skill
        this.effectSkill.removeSkillEffectWhenDie();
        //
        nPoint.setHp(0);
        nPoint.setMp(0);
        // xóa trứng
        if (this.mobMe != null) {
            this.mobMe.mobMeDie();
        }
        Service.gI().charDie(this);
        // add kẻ thù
        if (!this.isPet && !this.isNewPet && !this.isBoss && plAtt != null && !plAtt.isPet && !plAtt.isNewPet
                && !plAtt.isBoss && !plAtt.isBot) {
            if (!plAtt.itemTime.isUseAnDanh) {
                FriendAndEnemyService.gI().addEnemy(this, plAtt);
            }
        }
        // kết thúc pk
        if (this.pvp != null) {
            this.pvp.lose(this, TYPE_LOSE_PVP.DEAD);
        }
        // PVPServcice.gI().finishPVP(this, PVP.TYPE_DIE);
        BlackBallWar.gI().dropBlackBall(this);
    }

    // --------------------------------------------------------------------------
    public void setClanMember() {
        if (this.clanMember != null) {
            this.clanMember.powerPoint = (long) this.nPoint.power;
            this.clanMember.head = this.getHead();
            this.clanMember.body = this.getBody();
            this.clanMember.leg = this.getLeg();
        }
    }

    public boolean isAdmin() {
        return this.session != null && this.session.isAdmin;
    }

    /**
     * Admin có thể teleport đến bất kỳ map nào
     */
    public void adminTeleport(int mapId, int zoneId, int x, int y) {
        if (isAdmin()) {
            Dragon.services.func.ChangeMapService.gI().changeMap(this, mapId, zoneId, x, y);
        } else {
            Dragon.services.Service.gI().sendThongBao(this, "Chỉ admin mới có thể sử dụng lệnh này!");
        }
    }

    /**
     * Admin có thể teleport đến bất kỳ map nào bằng spaceship
     */
    public void adminTeleportBySpaceship(int mapId, int zoneId, int x) {
        if (isAdmin()) {
            Dragon.services.func.ChangeMapService.gI().changeMapBySpaceShip(this, mapId, zoneId, x);
        } else {
            Dragon.services.Service.gI().sendThongBao(this, "Chỉ admin mới có thể sử dụng lệnh này!");
        }
    }

    public void congExpOff() {
        long exp = this.nPoint.getexp() * this.timeoff;
        Service.gI().addSMTN(this, (byte) 2, exp, false);
        NpcService.gI().createTutorial(this, 536,
                "Bạn tăng được " + exp + " sức mạnh trong thời gian " + this.timeoff + " phút tập luyện Offline");

    }

    public void setJustRevivaled() {
        if (this.isTrieuhoipet) {
            this.justRevived1 = true;
            this.lastTimeRevived1 = System.currentTimeMillis();
        }
        this.justRevived = true;
        this.lastTimeRevived = System.currentTimeMillis();
    }

    public void preparedToDispose() {

    }

    public String CapBacTuTien(int lvtt) {
        return Dragon.services.tutien.TutienRealm.getFullRankName(lvtt);
    }

    /**
     * Tính tổng bonus đại cảnh giới Tu Tiên (không áp dụng trực tiếp)
     */
    public long getTutienRealmDamageBonus() {
        int level = (int) this.TUTIEN[1];
        return Dragon.services.tutien.TutienCalculator.calculateRealmDamageBonus(level);
    }

    /**
     * Tính tổng bonus HP đại cảnh giới Tu Tiên
     */
    public long getTutienRealmHpBonus() {
        int level = (int) this.TUTIEN[1];
        return Dragon.services.tutien.TutienCalculator.calculateRealmHpBonus(level);
    }

    /**
     * Tính bonus Ki tu tiên (20k Ki mỗi cấp)
     */
    public int KiTutien(int lvtt) {
        // Kiểm tra cấu hình tắt hiệu ứng Tu Tiên
        if (Dragon.services.tutien.TutienConstants.Config.DISABLE_TUTIEN_EFFECTS) {
            return 0; // Trả về 0 bonus Ki
        }

        // Sử dụng hệ thống bonus mới: 20k Ki mỗi cấp
        int bonus = (int) Dragon.services.tutien.TutienCalculator.calculateRealmKiBonus(lvtt);
        return bonus;
    }

    /**
     * Lấy thông tin bonus đại cảnh giới hiện tại
     */
    public String getTutienRealmBonusInfo() {
        int level = (int) this.TUTIEN[1];
        return Dragon.services.tutien.TutienCalculator.getRealmBonusInfo(level);
    }

    /**
     * Debug method để kiểm tra bonus tu tiên
     */
    public void debugTutienBonus() {
        System.out.println("=== DEBUG TUTIEN BONUS ===");
        System.out.println("Player: " + this.name);
        System.out.println("Captutien: " + this.Captutien);
        System.out.println("TUTIEN[1]: " + this.TUTIEN[1]);
        System.out.println("TUTIEN[2]: " + this.TUTIEN[2]);
        System.out.println(
                "DISABLE_TUTIEN_EFFECTS: " + Dragon.services.tutien.TutienConstants.Config.DISABLE_TUTIEN_EFFECTS);

        int level = this.Captutien;
        System.out.println("Calculated Level: " + level);

        int damageBonus = this.Dametutien(level);
        int hpBonus = this.HpKiGiaptutien(level);
        int kiBonus = this.KiTutien(level);

        System.out.println("Damage Bonus: " + damageBonus);
        System.out.println("HP Bonus: " + hpBonus);
        System.out.println("KI Bonus: " + kiBonus);
        System.out.println("=== END DEBUG ===");
    }

    public float Bkttilecanhgioi(int lvtt) {
        switch (lvtt) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
                return 70f;
            case 9:
                return 69f;
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
                return 68f;
            case 19:
                return 67f;
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
                return 65f;
            case 29:
                return 60f;
            case 30:
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
                return 69f;
            case 39:
                return 65f;
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:
            case 48:
                return 64f;
            case 49:
                return 62f;
            case 50:
            case 51:
            case 52:
            case 53:
            case 54:
            case 55:
            case 56:
            case 57:
            case 58:
                return 60f;
            case 59:
                return 58f;
            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
            case 65:
            case 66:
            case 67:
            case 68:
                return 55f;
            case 69:
                return 53f;
            case 70:
            case 71:
            case 72:
            case 73:
            case 74:
            case 75:
            case 76:
            case 77:
            case 78:
                return 50f;
            case 79:
                return 49f;
            case 80:
            case 81:
            case 82:
            case 83:
            case 84:
            case 85:
            case 86:
            case 87:
            case 88:
                return 48f;
            case 89:
                return 47f;
            case 90:
                return 46f;
            case 91:
                return 43f;
            case 93:
                return 40f;
            case 94:
                return 39f;
            case 95:
                return 37f;
            case 96:
                return 35f;
            default:
                return 0.5f;
        }
    }

    public int Dametutien(int lvtt) {
        // Kiểm tra cấu hình tắt hiệu ứng Tu Tiên
        if (Dragon.services.tutien.TutienConstants.Config.DISABLE_TUTIEN_EFFECTS) {
            return 0; // Trả về 0% bonus dame
        }

        // Sử dụng hệ thống bonus mới: 1k damage mỗi cấp
        int bonus = (int) Dragon.services.tutien.TutienCalculator.calculateRealmDamageBonus(lvtt);
        return bonus;
    }

    public int HpKiGiaptutien(int lvtt) {
        // Kiểm tra cấu hình tắt hiệu ứng Tu Tiên
        if (Dragon.services.tutien.TutienConstants.Config.DISABLE_TUTIEN_EFFECTS) {
            return 0; // Trả về 0% bonus HP/Ki/Giáp
        }

        // Sử dụng hệ thống bonus mới: 20k HP/Ki mỗi cấp
        int bonus = (int) Dragon.services.tutien.TutienCalculator.calculateRealmHpBonus(lvtt);
        return bonus;
    }

    public int BktDieukiencanhgioi(int lvtt) {
        switch (lvtt) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
                return 5000000;
            case 9:
                return 7000000;
            case 10:
                return 7300000;
            case 11:
            case 12:
            case 13:
            case 14:
                return 7700000;
            case 15:
            case 16:
            case 17:
            case 18:
                return 9000000;
            case 19:
                return 12000000;
            case 20:
                return 13000000;
            case 21:
            case 22:
            case 23:
            case 24:
                return 15000000;
            case 25:
            case 26:
            case 27:
            case 28:
                return 17000000;
            case 29:
                return 20000000;
            case 30:
                return 21000000;
            case 31:
            case 32:
            case 33:
            case 34:
                return 23000000;
            case 35:
            case 36:
            case 37:
            case 38:
                return 25000000;
            case 39:
                return 27000000;
            case 40:
                return 30000000;
            case 41:
            case 42:
            case 43:
            case 44:
                return 31000000;
            case 45:
            case 46:
            case 47:
            case 48:
                return 32000000;
            case 49:
                return 35000000;
            case 50:
                return 37000000;
            case 51:
            case 52:
            case 53:
            case 54:
            case 55:
            case 56:
            case 57:
            case 58:
                return 40000000;
            case 59:
                return 42000000;
            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
            case 65:
            case 66:
            case 67:
            case 68:
                return 45000000;
            case 69:
                return 47000000;
            case 70:
            case 71:
            case 72:
            case 73:
            case 74:
            case 75:
            case 76:
            case 77:
            case 78:
                return 50000000;
            case 79:
                return 53000000;
            case 80:
            case 81:
            case 82:
            case 83:
            case 84:
            case 85:
            case 86:
            case 87:
            case 88:
                return 55000000;
            case 89:
                return 58000000;
            case 90:
                return 60000000;
            case 91:
                return 70000000;
            case 93:
                return 80000000;
            case 94:
                return 90000000;
            case 95:
                return 120000000;
            case 96:
                return 150000000;
            default:
                return Integer.MAX_VALUE;
        }
    }

    public String BktNameTuMa() {
        switch (this.Bkt_Tu_Ma) {
            case 0:
                return "Tiểu ma nhân";
            case 1:
                return "Ma nhân";
            case 2:
                return "Bán Ma";
            case 3:
                return "Hóa ma";
            case 4:
                return "Thiên ma";
            case 5:
                return "Địa ma";
            case 6:
                return "Huyền ma";
            case 7:
                return "Bán ma hoàng";
            case 8:
                return "Ma hoàng sơ kì";
            case 9:
                return "Ma hoàng trung kì";
            case 10:
                return "Ma hoàng hậu kì";
            case 11:
                return "Ma hoàng đỉnh phong";
            case 12:
                return "Ma Thần sơ kì";
            case 13:
                return "Ma Thần trung kì";
            case 14:
                return "Ma Thần hậu kì";
            case 15:
                return "Ma Thần \u0111\u1EC9nh phong";
            case 16:
                return "Thiên Ma Thần";
            default:
                return "Súc vật";
        }
    }

    public double BktDameTuMa() {
        switch (this.Bkt_Tu_Ma) {
            case 0:
                return 50;
            case 1:
                return 250;
            case 2:
                return 680;
            case 3:
                return 2001;
            case 4:
                return 2700;
            case 5:
                return 3500;
            case 6:
                return 4999;
            case 7:
                return 5892;
            case 8:
                return 7000;
            case 9:
                return 7800;
            case 10:
                return 8900;
            case 11:
                return 9999;
            case 12:
                return 11111;
            case 13:
                return 12555;
            case 14:
                return 13888;
            case 15:
                return 15555;
            case 16:
                return 33333;
            default:
                return -33333;
        }
    }

    public double BktGiapTuMa() {
        switch (this.Bkt_Tu_Ma) {
            case 0:
                return 25;
            case 1:
                return 50;
            case 2:
                return 80;
            case 3:
                return 201;
            case 4:
                return 270;
            case 5:
                return 350;
            case 6:
                return 499;
            case 7:
                return 582;
            case 8:
                return 700;
            case 9:
                return 780;
            case 10:
                return 890;
            case 11:
                return 999;
            case 12:
                return 1111;
            case 13:
                return 1255;
            case 14:
                return 1388;
            case 15:
                return 1555;
            case 16:
                return 3333;
            default:
                return -3333;
        }
    }

    public int BktTimeTuMa() {
        switch (this.Bkt_Tu_Ma) {
            case 0:
                return 5;
            case 1:
                return 12;
            case 2:
                return 17;
            case 3:
                return 25;
            case 4:
                return 33;
            case 5:
                return 38;
            case 6:
                return 43;
            case 7:
                return 50;
            case 8:
                return 55;
            case 9:
                return 59;
            case 10:
                return 65;
            case 11:
                return 69;
            case 12:
                return 76;
            case 13:
                return 83;
            case 14:
                return 89;
            case 15:
                return 95;
            case 16:
                return 100;
            default:
                return -100;
        }
    }

    public String BktNameHoncot(int Honcot) {
        switch (Honcot - 1) {
            case 0:
                return "-B\u00E1t Chu M\u00E2u ";
            case 1:
                return "-Tinh Th\u1EA7n Ng\u01B0ng T\u1EE5 Chi Tr\u00ED Tu\u1EC7 \u0110\u1EA7u C\u1ED1t";
            case 2:
                return "-Nhu C\u1ED1t Th\u1ECF H\u1EEFu T\u00ED C\u1ED1t";
            case 3:
                return "-Th\u00E1i Th\u1EA3n C\u1EF1 Vi\u00EAn";
            case 4:
                return "-Lam Ng\u00E2n Ho\u00E0ng";
            case 5:
                return "-T\u00E0 Ma H\u1ED5 K\u00ECnh";
            default:
                return "-H\u1ED3n x\u00E1c l\u00ECa \u0111\u1EDDi";
        }
    }

    public void dispose() {
        if (pet != null) {
            pet.dispose();
            pet = null;
        }
        if (clone != null && !this.isClone) {
            clone.dispose();
            clone = null;
        }
        if (newpet != null) {
            newpet.dispose();
            newpet = null;
        }
        if (mapBlackBall != null) {
            mapBlackBall.clear();
            mapBlackBall = null;
        }
        if (mapdiacung != null) {
            mapdiacung.clear();
            mapdiacung = null;
        }
        zone = null;
        mapBeforeCapsule = null;
        if (mapMaBu != null) {
            mapMaBu.clear();
            mapMaBu = null;
        }
        if (billEgg != null) {
            billEgg.dispose();
            billEgg = null;
        }
        zone = null;
        mapBeforeCapsule = null;
        if (mapCapsule != null) {
            mapCapsule.clear();
            mapCapsule = null;
        }
        if (mobMe != null) {
            mobMe.dispose();
            mobMe = null;
        }
        location = null;
        if (setClothes != null) {
            setClothes.dispose();
            setClothes = null;
        }
        if (effectSkill != null) {
            effectSkill.dispose();
            effectSkill = null;
        }
        if (mabuEgg != null) {
            mabuEgg.dispose();
            mabuEgg = null;
        }
        if (skillSpecial != null) {
            skillSpecial.dispose();
            skillSpecial = null;
        }
        if (playerTask != null) {
            playerTask.dispose();
            playerTask = null;
        }
        if (itemTime != null) {
            itemTime.dispose();
            itemTime = null;
        }
        if (fusion != null) {
            fusion.dispose();
            fusion = null;
        }
        if (magicTree != null) {
            magicTree.dispose();
            magicTree = null;
        }
        if (playerIntrinsic != null) {
            playerIntrinsic.dispose();
            playerIntrinsic = null;
        }
        if (inventory != null) {
            inventory.dispose();
            inventory = null;
        }
        if (playerSkill != null) {
            playerSkill.dispose();
            playerSkill = null;
        }
        if (combineNew != null) {
            combineNew.dispose();
            combineNew = null;
        }
        if (iDMark != null) {
            iDMark.dispose();
            iDMark = null;
        }
        if (charms != null) {
            charms.dispose();
            charms = null;
        }
        if (effectSkin != null) {
            effectSkin.dispose();
            effectSkin = null;
        }
        if (nPoint != null) {
            nPoint.dispose();
            nPoint = null;
        }
        if (rewardBlackBall != null) {
            rewardBlackBall.dispose();
            rewardBlackBall = null;
        }
        if (effectFlagBag != null) {
            effectFlagBag.dispose();
            effectFlagBag = null;
        }
        if (pvp != null) {
            pvp.dispose();
            pvp = null;
        }
        effectFlagBag = null;
        clan = null;
        clanMember = null;
        friends = null;
        enemies = null;
        session = null;
        name = null;
    }

    public void setfight(byte typeFight, byte typeTatget) {

        try {
            if (typeFight == (byte) 0 && typeTatget == (byte) 0) {
                this.istry = true;
            }
            if (typeFight == (byte) 0 && typeTatget == (byte) 1) {
                this.istry1 = true;
            }
            if (typeFight == (byte) 1 && typeTatget == (byte) 0) {
                this.isfight = true;
            }
            if (typeFight == (byte) 1 && typeTatget == (byte) 1) {
                this.isfight1 = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean IsActiveMaster() {

        if (this.istry || this.isfight) {
            this.istry = true;
        }

        return false;
    }

    public void rsfight() {
        if (this.istry) {
            this.istry = false;
        }
        if (this.istry1) {
            this.istry1 = false;
        }
        if (this.isfight) {
            this.isfight = false;
        }
        if (this.isfight1) {
            this.isfight1 = false;
        }
    }

    public boolean IsTry0() {
        if (this.istry && this.isfight) {
            return true;
        }
        return false;
    }

    public boolean IsTry1() {
        if (this.istry && this.isfight1) {
            return true;
        }
        return false;
    }

    public boolean IsFigh0() {
        if (this.istry && this.isfight1) {
            return true;
        }
        return false;
    }

    public String percentGold(int type) {
        try {
            if (type == 0) {
                double percent = ((double) this.goldNormar / ChonAiDay.gI().goldNormar) * 100;
                return String.valueOf(Math.ceil(percent));
            } else if (type == 1) {
                double percent = ((double) this.goldVIP / ChonAiDay.gI().goldVip) * 100;
                return String.valueOf(Math.ceil(percent));
            }
        } catch (ArithmeticException e) {

            return "0";
        }
        return "0";
    }

    public Mob mobTarget;

    public long lastTimeTargetMob;

    public long timeTargetMob;

    public long lastTimeAttack;

    public void moveTo(int x, int y) {
        byte dir = (byte) (this.location.x - x < 0 ? 1 : -1);
        byte move = (byte) Util.nextInt(40, 60);
        if (isBot) {
            move = (byte) (move * (byte) 2);
        }
        PlayerService.gI().playerMove(this, this.location.x + (dir == 1 ? move : -move),
                y + (Util.isTrue(3, 10) ? -50 : 0));
    }

    public Mob getMobAttack() {
        if (this.mobTarget != null && (this.mobTarget.isDie() || !this.zone.equals(this.mobTarget.zone))) {
            this.mobTarget = null;
        }
        if (this.mobTarget == null && Util.canDoWithTime(lastTimeTargetMob, timeTargetMob)) {
            this.mobTarget = this.zone.getRandomMobInMap();
            this.lastTimeTargetMob = System.currentTimeMillis();
            this.timeTargetMob = 500;
        }
        return this.mobTarget;
    }

    public int getRangeCanAttackWithSkillSelect() {
        int skillId = this.playerSkill.skillSelect.template.id;
        if (skillId == Skill.KAMEJOKO || skillId == Skill.MASENKO || skillId == Skill.ANTOMIC) {
            return Skill.RANGE_ATTACK_CHIEU_CHUONG;
        } else if (skillId == Skill.DRAGON || skillId == Skill.DEMON || skillId == Skill.GALICK) {
            return Skill.RANGE_ATTACK_CHIEU_DAM;
        }
        return 752002;
    }

    public void active() {
        if (this.isBot) {
            if (this.isDie()) {
                this.nPoint.hp = this.nPoint.hpMax;
            }
            if (this.nPoint.mp <= 0) {
                this.nPoint.mp = this.nPoint.mpMax;
            }
            this.attack();
        }
    }

    public void attack() {
        if (this.isBot) {
            // this.mobTarget = this.getMobAttack();
            if (Util.canDoWithTime(lastTimeAttack, 100) && this.mobTarget != null) {

                this.lastTimeAttack = System.currentTimeMillis();
                try {
                    Mob m = this.getMobAttack();
                    if (m == null || m.isDie()) {
                        return;
                    }

                    this.playerSkill.skillSelect = this.playerSkill.skills
                            .get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
                    // System.out.println(m.name);
                    if (Util.nextInt(100) < 70) {
                        this.playerSkill.skillSelect = this.playerSkill.skills.get(0);
                    }
                    if (Util.getDistance(this, m) <= this.getRangeCanAttackWithSkillSelect()) {
                        if (Util.isTrue(5, 20)) {
                            if (SkillUtil.isUseSkillChuong(this)) {
                                this.moveTo(m.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 200)),
                                        Util.nextInt(10) % 2 == 0 ? m.location.y : m.location.y);
                            } else {
                                this.moveTo(m.location.x + (Util.getOne(-1, 1) * Util.nextInt(10, 40)),
                                        Util.nextInt(10) % 2 == 0 ? m.location.y : m.location.y);
                            }
                        }
                        SkillService.gI().useSkill(this, null, m, null);
                    } else {
                        this.moveTo(m.location.x, m.location.y);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                this.mobTarget = getMobAttack();
            }
        }
    }

    public List<String> textRuongGo = new ArrayList<>();

    // Dungeon instance
    public Dragon.thuongnhanthanbi.DungeonInstance dungeonInstance;

}
