package Dragon.utils;

import Dragon.jdbc.daos.GodGK;
import Dragon.models.boss.Boss;
import Dragon.models.boss.BossManager;
import Dragon.models.item.Item;
import Dragon.models.map.ItemMap;
import Dragon.models.map.Zone;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.Normalizer;
import java.util.*;

import Dragon.models.matches.TOP;
import Dragon.models.mob.Mob;
import Dragon.models.npc.Npc;
import Dragon.models.player.Player;
import com.girlkun.network.io.Message;
import Dragon.server.Client;
import Dragon.server.Manager;
import Dragon.services.ItemService;

import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang.ArrayUtils;

import javax.imageio.ImageIO;
import java.security.MessageDigest;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.stream.IntStream;
import Dragon.services.MapService;

public class Util {

    private static final Random rand;
    private static final Locale locale = new Locale("vi", "VN");
    private static final NumberFormat num = NumberFormat.getInstance(locale);
    private static SimpleDateFormat dateFormatDay = new SimpleDateFormat("yyyy-MM-dd");

    static {
        rand = new Random();

    }

    public static byte getHead(byte gender) {
        switch (gender) {
            case 2:
                return 28;
            case 1:
                return 32;
            default:
                return 64;
        }
    }

    public static int randomMapBossBroly() {
        int[] listMap = new int[] { 6, 10, 11, 12, 13, 19, 20, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38 };
        int mapId = Util.nextInt(listMap.length);
        while (!MapService.gI().getZone(mapId).getBosses().isEmpty()) {
            mapId = Util.nextInt(listMap.length);
        }
        return listMap[mapId];
    }

    public static void setTimeout(Runnable runnable, int delay) {
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                runnable.run();
            } catch (Exception e) {
                System.err.println(e);
            }
        }).start();
    }

    public static int createIdDuongTank(int idPlayer) {
        return -idPlayer - 100_000_000;
    }

    public static int createIdBossLV(long idPlayer) {
        return (int) (-idPlayer - 500_000_000);
    }

    public static long GioiHannext(double from, double to) {
        // code by Việt
        return (long) (from + rand.nextInt((int) (to - from + 1)));
    }

    public static int createIdBossClone(int idPlayer) {
        return -idPlayer - 120_000_000;
    }

    public static int nextIntDhvt(int from, int to) {
        return from + rand.nextInt(to - from);
    }

    public static synchronized boolean compareDay(Date now, Date when) {
        try {
            Date date1 = Util.dateFormatDay.parse(Util.dateFormatDay.format(now));
            Date date2 = Util.dateFormatDay.parse(Util.dateFormatDay.format(when));
            return !date1.equals(date2) && !date1.before(date2);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String msToThang(long ms) {
        ms = ms - System.currentTimeMillis();
        if (ms < 0) {
            ms = 0;
        }
        long mm;
        long ss;
        long hh;
        long hhd;
        long dd;
        ss = (ms / 1000);
        mm = (ss / 60);
        ss = ss % 60;
        hh = (mm / 60);
        hhd = hh % 24;
        mm = mm % 60;
        dd = hh / 24;
        String ssString = String.valueOf(ss);
        String mmString = String.valueOf(mm);
        String hhString = String.valueOf(hh);
        String hhdString = String.valueOf(hhd);
        String ddString = String.valueOf(dd);
        String time;
        if (dd != 0) {
            time = ddString + " Ngày (" + hhdString + "H, " + mmString + "M, " + ssString + "s)";
        } else if (hh != 0) {
            time = " (" + hhString + "H, " + mmString + "M, " + ssString + "s)";
        } else if (mm != 0) {
            time = " (" + mmString + "M, " + ssString + "s)";
        } else if (ss != 0) {
            time = ssString + " giây";
        } else {
            time = "Hết hạn";
        }
        return time;
    }

    public static void checkPlayer(Player player) {
        new Thread(() -> {
            List<Player> list = Client.gI().getPlayers().stream().filter(p -> !p.isPet && !p.isClone && !p.isNewPet
                    && !p.isTrieuhoipet && p.getSession().userId == player.getSession().userId)
                    .collect(Collectors.toList());
            if (list.size() > 1) {
                list.forEach(pp -> Client.gI().kickSession(pp.getSession()));
                list.clear();
            }
        }).start();
    }

    public static String tinhgio(long ms) {
        ms = ms - System.currentTimeMillis();
        if (ms < 0) {
            ms = 0;
        }
        long mm;
        long ss;
        long hh;
        ss = ms / 1000;
        mm = (long) (ss / 60);
        ss = ss % 60;
        hh = (long) (mm / 60);
        mm = mm % 60;
        String ssString = String.valueOf(ss);
        String mmString = String.valueOf(mm);
        String hhString = String.valueOf(hh);
        String time;
        if (hh != 0) {
            time = hhString + " giờ, " + mmString + " phút, " + ssString + " giây";
        } else if (mm != 0) {
            time = mmString + " phút, " + ssString + "giây";
        } else if (ss != 0) {
            time = ssString + " giây";
        } else {
            time = ssString + " giây";
        }
        return time;
    }

    public static int[] pickNRandInArr(int[] array, int n) {
        List<Integer> list = new ArrayList<Integer>(array.length);
        for (int i : array) {
            list.add(i);
        }
        Collections.shuffle(list);
        int[] answer = new int[n];
        for (int i = 0; i < n; i++) {
            answer[i] = list.get(i);
        }
        Arrays.sort(answer);
        return answer;
    }

    public static int maxShort(long a) {
        if (a > Short.MAX_VALUE) {
            a = Short.MAX_VALUE;
        }
        return (short) a;
    }

    public static String getFormatNumber(double hp) {
        // Nro Kuroko
        return Util.num.format(Math.floor(hp));
    }

    public static int Ahwuocdz(double a) {
        // Nro Kuroko
        if (a > 0.0f && a <= 1f) {
            a = 1;
        }
        if (a > Integer.MAX_VALUE) {
            a = Integer.MAX_VALUE;
        }
        return (int) a;
    }

    public static int maxInt(long a) {
        if (a > 2123456789) {
            a = 2123456789;
        }
        return (int) a;
    }

    public static boolean contains(String[] arr, String key) {
        return Arrays.toString(arr).contains(key);
    }

    public static Item petrandom(int tempId) {
        Item gapthuong = ItemService.gI().createNewItem((short) tempId);
        if (Util.isTrue(90, 100)) {
            gapthuong.itemOptions.add(new Item.ItemOption(50, Util.nextInt(5, 10)));
            gapthuong.itemOptions.add(new Item.ItemOption(103, Util.nextInt(5, 10)));
            gapthuong.itemOptions.add(new Item.ItemOption(77, Util.nextInt(5, 10)));
            if (Util.isTrue(30, 100)) {
                gapthuong.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 9)));
            }
        }
        return gapthuong;
    }

    public static Item petccrandom(int tempId) {
        Item gapcc = ItemService.gI().createNewItem((short) tempId);
        if (Util.isTrue(90, 100)) {
            gapcc.itemOptions.add(new Item.ItemOption(50, Util.nextInt(10, 20)));
            gapcc.itemOptions.add(new Item.ItemOption(103, Util.nextInt(10, 20)));
            gapcc.itemOptions.add(new Item.ItemOption(77, Util.nextInt(10, 20)));
            if (Util.isTrue(30, 100)) {
                gapcc.itemOptions.add(new Item.ItemOption(93, Util.nextInt(5, 12)));
            }
        }
        return gapcc;
    }

    public static Item petviprandom(int tempId) {
        Item gapvip = ItemService.gI().createNewItem((short) tempId);
        if (Util.isTrue(90, 100)) {
            gapvip.itemOptions.add(new Item.ItemOption(50, Util.nextInt(2, 50)));
            gapvip.itemOptions.add(new Item.ItemOption(103, Util.nextInt(2, 50)));
            gapvip.itemOptions.add(new Item.ItemOption(77, Util.nextInt(2, 50)));
            gapvip.itemOptions.add(new Item.ItemOption(101, Util.nextInt(1, 30)));
            if (Util.isTrue(95, 100)) {
                gapvip.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 7)));
            }
        }
        return gapvip;
    }

    public static Item trungthu(int tempId) {
        Item trugnthu = ItemService.gI().createNewItem((short) tempId);
        if (trugnthu.template.id == 578 || trugnthu.template.id == 765 || trugnthu.template.id == 904
                || trugnthu.template.id == 1550 || trugnthu.template.id == 1551) {
            trugnthu.itemOptions.add(new Item.ItemOption(50, Util.nextInt(20, 35)));
            trugnthu.itemOptions.add(new Item.ItemOption(77, Util.nextInt(20, 35)));
            trugnthu.itemOptions.add(new Item.ItemOption(103, Util.nextInt(20, 35)));
            trugnthu.itemOptions.add(new Item.ItemOption(30, 0));
            if (Util.isTrue(99, 100)) {
                trugnthu.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 7)));
            }
        } else if (trugnthu.template.id == 1552 || trugnthu.template.id == 1553) {
            trugnthu.itemOptions.add(new Item.ItemOption(50, Util.nextInt(10, 20)));
            trugnthu.itemOptions.add(new Item.ItemOption(77, Util.nextInt(10, 20)));
            trugnthu.itemOptions.add(new Item.ItemOption(103, Util.nextInt(10, 20)));
            trugnthu.itemOptions.add(new Item.ItemOption(30, 0));
            if (Util.isTrue(99, 100)) {
                trugnthu.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 7)));
            }
        } else {
            trugnthu.itemOptions.add(new Item.ItemOption(30, 0));
        }
        return trugnthu;
    }

    public static Item duoiKhi(int tempId) {
        Item trugnthu = ItemService.gI().createNewItem((short) tempId);
        if (trugnthu.template.id == 528) {
            trugnthu.itemOptions.add(new Item.ItemOption(50, Util.nextInt(20, 35)));
            trugnthu.itemOptions.add(new Item.ItemOption(77, Util.nextInt(20, 35)));
            trugnthu.itemOptions.add(new Item.ItemOption(103, Util.nextInt(20, 35)));
            trugnthu.itemOptions.add(new Item.ItemOption(30, 0));
            if (Util.isTrue(99, 100)) {
                trugnthu.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 7)));
            }
        } else if (trugnthu.template.id == 1552 || trugnthu.template.id == 920) {
            trugnthu.itemOptions.add(new Item.ItemOption(50, Util.nextInt(10, 20)));
            trugnthu.itemOptions.add(new Item.ItemOption(77, Util.nextInt(10, 20)));
            trugnthu.itemOptions.add(new Item.ItemOption(103, Util.nextInt(10, 20)));
            trugnthu.itemOptions.add(new Item.ItemOption(30, 0));
            if (Util.isTrue(99, 100)) {
                trugnthu.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 7)));
            }
        } else {
            trugnthu.itemOptions.add(new Item.ItemOption(30, 0));
        }
        return trugnthu;
    }

    public static Item thit(int tempId) {
        Item tet2025 = ItemService.gI().createNewItem((short) tempId);
        if (tet2025.template.id == 1200) {
            tet2025.itemOptions
                    .add(new Item.ItemOption(50, Util.isTrue(5, 100) ? Util.nextInt(10, 45) : Util.nextInt(5, 25)));
            tet2025.itemOptions
                    .add(new Item.ItemOption(77, Util.isTrue(5, 100) ? Util.nextInt(10, 45) : Util.nextInt(5, 25)));
            tet2025.itemOptions
                    .add(new Item.ItemOption(103, Util.isTrue(5, 100) ? Util.nextInt(10, 45) : Util.nextInt(5, 25)));
            tet2025.itemOptions.add(new Item.ItemOption(30, 0));
            if (Util.isTrue(99, 100)) {
                tet2025.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 7)));
            }
        } else if (tet2025.template.id == 1665) {
            tet2025.itemOptions
                    .add(new Item.ItemOption(50, Util.isTrue(5, 100) ? Util.nextInt(5, 30) : Util.nextInt(2, 25)));
            tet2025.itemOptions
                    .add(new Item.ItemOption(77, Util.isTrue(5, 100) ? Util.nextInt(5, 30) : Util.nextInt(2, 25)));
            tet2025.itemOptions
                    .add(new Item.ItemOption(103, Util.isTrue(5, 100) ? Util.nextInt(5, 30) : Util.nextInt(2, 25)));
            tet2025.itemOptions.add(new Item.ItemOption(30, 0));
            if (Util.isTrue(99, 100)) {
                tet2025.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 7)));
            }
        } else {
            tet2025.itemOptions.add(new Item.ItemOption(30, 0));
        }
        return tet2025;
    }

    public static Item batbo(int tempId) {
        Item batbo = ItemService.gI().createNewItem((short) tempId);
        if (Util.isTrue(90, 100)) {
            batbo.itemOptions.add(new Item.ItemOption(30, 0));
        }
        return batbo;
    }

    public static Item cauca(int tempId) {
        Item cauca = ItemService.gI().createNewItem((short) tempId);
        if (Util.isTrue(100, 100)) {
            cauca.itemOptions.add(new Item.ItemOption(223, 0));
        }
        return cauca;
    }

    public static String strSQL(final String str) {
        return str.replaceAll("['\"\\\\%]", "\\\\$0");
    }

    public static String numberToMoney(double power) {
        Locale locale = new Locale("vi", "VN");
        NumberFormat num = NumberFormat.getInstance(locale);
        num.setMaximumFractionDigits(1);
        if (power >= 1000000000) {
            return num.format((double) power / 1000000000) + " Tỷ";
        } else if (power >= 1000000) {
            return num.format((double) power / 1000000) + " Tr";
        } else if (power >= 1000) {
            return num.format((double) power / 1000) + " k";
        } else {
            return num.format(power);
        }
    }

    public static String powerToString(double power) {
        Locale locale = new Locale("vi", "VN");
        NumberFormat num = NumberFormat.getInstance(locale);
        num.setMaximumFractionDigits(1);
        if (power >= 1000000000) {
            return num.format((double) power / 1000000000) + " Tỷ";
        } else if (power >= 1000000) {
            return num.format((double) power / 1000000) + " Tr";
        } else if (power >= 1000) {
            return num.format((double) power / 1000) + " k";
        } else {
            return num.format(power);
        }
    }

    public static String format(int power) {
        return num.format(power);
    }

    public static String format(long power) {
        return num.format(power);
    }

    public static String format(double power) {
        return num.format(power);
    }

    public static int getDistance(int x1, int y1, int x2, int y2) {
        return (int) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    public static int getDistance(Player pl1, Player pl2) {
        return getDistance(pl1.location.x, pl1.location.y, pl2.location.x, pl2.location.y);
    }

    public static int getDistance(Player pl, Npc npc) {
        return getDistance(pl.location.x, pl.location.y, npc.cx, npc.cy);
    }

    public static int getDistance(Player pl, Mob mob) {
        return getDistance(pl.location.x, pl.location.y, mob.location.x, mob.location.y);
    }

    public static int getDistance(Mob mob1, Mob mob2) {
        return getDistance(mob1.location.x, mob1.location.y, mob2.location.x, mob2.location.y);
    }

    public static int nextInt(int from, int to) {
        return from + rand.nextInt(to - from + 1);
    }

    public static int nextInt(int max) {
        return rand.nextInt(max);
    }

    public static int nextInt(int[] percen) {
        int next = nextInt(1000), i;
        for (i = 0; i < percen.length; i++) {
            if (next < percen[i]) {
                return i;
            }
            next -= percen[i];
        }
        return i;
    }

    public static int getOne(int n1, int n2) {
        return rand.nextInt() % 2 == 0 ? n1 : n2;
    }

    public static int currentTimeSec() {
        return (int) System.currentTimeMillis() / 1000;
    }

    public static String replace(String text, String regex, String replacement) {
        return text.replace(regex, replacement);
    }

    public static boolean isTrue(int ratio, int typeRatio) {
        int num = Util.nextInt(typeRatio);
        if (num < ratio) {
            return true;
        }
        return false;
    }

    public static int trum(double a) {
        // Telegram: @Tamkjll
        if (a > 2123456789) {
            a = 2123456789;
        }
        return (int) a;
    }

    public static double limitDouble(double value) {
        final double MAX_SAFE_DOUBLE = 9.223372036854775E15;
        final double MIN_SAFE_DOUBLE = -9.223372036854775E15;

        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0;
        }

        if (value > MAX_SAFE_DOUBLE) {
            return MAX_SAFE_DOUBLE;
        }

        if (value < MIN_SAFE_DOUBLE) {
            return MIN_SAFE_DOUBLE;
        }

        return value;
    }

    public static boolean isTrue(float ratio, int typeRatio) {
        if (ratio < 1) {
            ratio *= 10;
            typeRatio *= 10;
        }
        int num = Util.nextInt(typeRatio);
        if (num < ratio) {
            return true;
        }
        return false;
    }

    public static boolean haveSpecialCharacter(String text) {
        Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        boolean b = m.find();
        return b || text.contains(" ");
    }

    public static boolean canDoWithTime(long lastTime, long miniTimeTarget) {
        return System.currentTimeMillis() - lastTime > miniTimeTarget;
    }

    public static char removeAccent(char ch) {
        String s = String.valueOf(ch);
        String normalized = Normalizer.normalize(s, Normalizer.Form.NFD);
        String without = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return without.isEmpty() ? ch : without.charAt(0);
    }

    public static String removeAccent(String str) {
        if (str == null)
            return null;
        String normalized = Normalizer.normalize(str, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    public static String generateRandomText(int len) {
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijk"
                + "lmnopqrstuvwxyz!@#$%&";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public static Object[] addArray(Object[]... arrays) {
        if (arrays == null || arrays.length == 0) {
            return null;
        }
        if (arrays.length == 1) {
            return arrays[0];
        }
        Object[] arr0 = arrays[0];
        for (int i = 1; i < arrays.length; i++) {
            arr0 = ArrayUtils.addAll(arr0, arrays[i]);
        }
        return arr0;
    }

    public static ItemMap manhTS(Zone zone, int tempId, int quantity, int x, int y, long playerId) {
        return new ItemMap(zone, tempId, quantity, x, y, playerId);
    }

    public static ItemMap ratiDTL(Zone zone, int tempId, int quantity, int x, int y, long playerId) {
        ItemMap it = new ItemMap(zone, tempId, quantity, x, zone.map.yPhysicInTop(x, y - 24), playerId);
        List<Integer> ao = Arrays.asList(555, 557, 559);
        List<Integer> quan = Arrays.asList(556, 558, 560);
        List<Integer> gang = Arrays.asList(562, 564, 566);
        List<Integer> giay = Arrays.asList(563, 565, 567);
        int ntl = 561;
        if (ao.contains(tempId)) {
            it.options.add(new Item.ItemOption(47,
                    highlightsItem(it.itemTemplate.gender == 2, new Random().nextInt(501) + 1300)));
        }
        if (quan.contains(tempId)) {
            it.options.add(new Item.ItemOption(22,
                    highlightsItem(it.itemTemplate.gender == 0, new Random().nextInt(11) + 45)));
        }
        if (gang.contains(tempId)) {
            it.options.add(new Item.ItemOption(0,
                    highlightsItem(it.itemTemplate.gender == 2, new Random().nextInt(1001) + 3500)));
        }
        if (giay.contains(tempId)) {
            it.options.add(new Item.ItemOption(23,
                    highlightsItem(it.itemTemplate.gender == 1, new Random().nextInt(11) + 35)));
        }
        if (ntl == tempId) {
            it.options.add(new Item.ItemOption(14, new Random().nextInt(2) + 15));
        }
        it.options.add(new Item.ItemOption(209, 1)); // đồ rơi từ boss
        it.options.add(new Item.ItemOption(21, 18)); // ycsm 18 tỉ
        it.options.add(new Item.ItemOption(30, 1)); // ko thể gd
        if (Util.isTrue(90, 100)) {// tỉ lệ ra spl
            it.options.add(new Item.ItemOption(107, new Random().nextInt(3) + 1));
        } else if (Util.isTrue(4, 100)) {
            it.options.add(new Item.ItemOption(107, new Random().nextInt(3) + 5));
        } else {
            it.options.add(new Item.ItemOption(107, new Random().nextInt(5) + 1));
        }
        return it;
    }

    public static ItemMap RaitiDoc12(Zone zone, int tempId, int quantity, int x, int y, long playerId) {
        ItemMap it = new ItemMap(zone, tempId, quantity, x, y, playerId);
        List<Integer> ao = Arrays.asList(233, 237, 241);
        List<Integer> quan = Arrays.asList(245, 249, 253);
        List<Integer> gang = Arrays.asList(257, 261, 265);
        List<Integer> giay = Arrays.asList(269, 273, 277);
        int rd12 = 281;
        if (ao.contains(tempId)) {
            it.options.add(new Item.ItemOption(47,
                    highlightsItem(it.itemTemplate.gender == 2, new Random().nextInt(121) + 350)));// giáp 350-470
        }
        if (quan.contains(tempId)) {
            it.options.add(
                    new Item.ItemOption(22, highlightsItem(it.itemTemplate.gender == 0, new Random().nextInt(5) + 20)));// hp
            // 20-24k
        }
        if (gang.contains(tempId)) {
            it.options.add(new Item.ItemOption(0,
                    highlightsItem(it.itemTemplate.gender == 2, new Random().nextInt(51) + 2200)));// 2200-2250
        }
        if (giay.contains(tempId)) {
            it.options.add(
                    new Item.ItemOption(23, highlightsItem(it.itemTemplate.gender == 1, new Random().nextInt(4) + 20)));// 20-23k
            // ki
        }
        if (rd12 == tempId) {
            it.options.add(new Item.ItemOption(14, new Random().nextInt(3) + 10));// 10-12cm
        }
        it.options.add(new Item.ItemOption(209, 1));// đồ rơi từ boss
        if (Util.isTrue(70, 100)) {// tỉ lệ ra spl 1-3 sao 70%
            it.options.add(new Item.ItemOption(107, new Random().nextInt(1) + 3));
        } else if (Util.isTrue(4, 100)) {// tỉ lệ ra spl 5-7 sao 4%
            it.options.add(new Item.ItemOption(107, new Random().nextInt(3) + 5));
        } else {// tỉ lệ ra spl 1-5 sao 6%
            it.options.add(new Item.ItemOption(107, new Random().nextInt(2) + 3));
        }
        return it;
    }

    public static Item ratiItemTL(int tempId) {
        Item it = ItemService.gI().createItemSetKichHoat(tempId, 1);
        List<Integer> ao = Arrays.asList(555, 557, 559);
        List<Integer> quan = Arrays.asList(556, 558, 560);
        List<Integer> gang = Arrays.asList(562, 564, 566);
        List<Integer> giay = Arrays.asList(563, 565, 567);
        int ntl = 561;
        if (ao.contains(tempId)) {
            it.itemOptions.add(
                    new Item.ItemOption(47, highlightsItem(it.template.gender == 2, new Random().nextInt(501) + 1000)));
        }
        if (quan.contains(tempId)) {
            it.itemOptions.add(
                    new Item.ItemOption(22, highlightsItem(it.template.gender == 0, new Random().nextInt(11) + 45)));
        }
        if (gang.contains(tempId)) {
            it.itemOptions.add(
                    new Item.ItemOption(0, highlightsItem(it.template.gender == 2, new Random().nextInt(1001) + 3500)));
        }
        if (giay.contains(tempId)) {
            it.itemOptions.add(
                    new Item.ItemOption(23, highlightsItem(it.template.gender == 1, new Random().nextInt(11) + 35)));
        }
        if (ntl == tempId) {
            it.itemOptions.add(new Item.ItemOption(14, new Random().nextInt(3) + 15));
        }
        it.itemOptions.add(new Item.ItemOption(107, new Random().nextInt(6)));
        it.itemOptions.add(new Item.ItemOption(21, 15));
        return it;
    }

    public static ItemMap useItem(Zone zone, int tempId, int quantity, int x, int y, long playerId) {
        ItemMap it = new ItemMap(zone, tempId, quantity, x, zone.map.yPhysicInTop(x, y - 24), playerId);
        List<Integer> tanjiro = Arrays.asList(1087, 1088, 1091, 1090);
        if (tanjiro.contains(tempId)) {
            it.options.add(
                    new Item.ItemOption(77, highlightsItem(it.itemTemplate.gender == 3, new Random().nextInt(30) + 1)));
            it.options.add(new Item.ItemOption(103,
                    highlightsItem(it.itemTemplate.gender == 3, new Random().nextInt(30) + 1)));
            it.options.add(
                    new Item.ItemOption(50, highlightsItem(it.itemTemplate.gender == 3, new Random().nextInt(30) + 1)));
        }
        it.options.add(new Item.ItemOption(209, 1)); // đồ rơi từ boss
        it.options.add(new Item.ItemOption(30, 1)); // ko thể gd

        return it;
    }

    public static ItemMap ratiItem(Zone zone, int tempId, int quantity, int x, int y, long playerId) {
        ItemMap it = new ItemMap(zone, tempId, quantity, x, y, playerId);
        List<Integer> aotl = Arrays.asList(555, 557, 559);
        List<Integer> quantl = Arrays.asList(556, 558, 560);
        List<Integer> gangtl = Arrays.asList(562, 564, 566);
        List<Integer> giaytl = Arrays.asList(563, 565, 567);
        List<Integer> aohd = Arrays.asList(650, 652, 654);
        List<Integer> quanhd = Arrays.asList(651, 653, 655);
        List<Integer> ganghd = Arrays.asList(657, 659, 661);
        List<Integer> giayhd = Arrays.asList(658, 660, 662);
        int ntl = 561;
        int nhd = 656;
        if (aotl.contains(tempId)) {
            it.options.add(new Item.ItemOption(47,
                    highlightsItem(it.itemTemplate.gender == 2, new Random().nextInt(501) + 1000)));
        }
        if (quantl.contains(tempId)) {
            it.options.add(new Item.ItemOption(22,
                    highlightsItem(it.itemTemplate.gender == 0, new Random().nextInt(11) + 45)));
        }
        if (gangtl.contains(tempId)) {
            it.options.add(new Item.ItemOption(0,
                    highlightsItem(it.itemTemplate.gender == 2, new Random().nextInt(1001) + 3500)));
        }
        if (giaytl.contains(tempId)) {
            it.options.add(new Item.ItemOption(23,
                    highlightsItem(it.itemTemplate.gender == 1, new Random().nextInt(11) + 35)));
        }
        if (ntl == tempId) {
            it.options.add(new Item.ItemOption(14, new Random().nextInt(3) + 15));
        }
        if (aohd.contains(tempId)) {
            it.options.add(new Item.ItemOption(47,
                    highlightsItem(it.itemTemplate.gender == 2, new Random().nextInt(501) + 1500)));
            it.options.add(new Item.ItemOption(30, 1));
        }
        if (quanhd.contains(tempId)) {
            it.options.add(new Item.ItemOption(22,
                    highlightsItem(it.itemTemplate.gender == 0, new Random().nextInt(11) + 100)));
            it.options.add(new Item.ItemOption(30, 1));
        }
        if (ganghd.contains(tempId)) {
            it.options.add(new Item.ItemOption(0,
                    highlightsItem(it.itemTemplate.gender == 2, new Random().nextInt(1001) + 5500)));
            it.options.add(new Item.ItemOption(30, 1));
        }
        if (giayhd.contains(tempId)) {
            it.options.add(new Item.ItemOption(23,
                    highlightsItem(it.itemTemplate.gender == 1, new Random().nextInt(11) + 80)));
            it.options.add(new Item.ItemOption(30, 1));
        }
        if (nhd == tempId) {
            it.options.add(new Item.ItemOption(14, new Random().nextInt(3) + 19));
            it.options.add(new Item.ItemOption(30, 1));
        }
        it.options.add(new Item.ItemOption(209, 1));
        it.options.add(new Item.ItemOption(21, 15));
        it.options.add(new Item.ItemOption(107, new Random().nextInt(6)));
        return it;
    }

    public static ItemMap sukienhungvuong(Zone zone, int tempId, int quantity, int x, int y, long playerId) {
        ItemMap it = new ItemMap(zone, tempId, quantity, x, y, playerId);
        List<Integer> konggozila = Arrays.asList(1442, 1443, 421, 422);
        if (konggozila.contains(tempId)) {
            it.options.add(new Item.ItemOption(50, 25));
            it.options.add(new Item.ItemOption(77, 25));
            it.options.add(new Item.ItemOption(103, 25));
            it.options.add(new Item.ItemOption(207, 0));
        }
        if (Util.isTrue(99, 100)) {
            it.options.add(new Item.ItemOption(93, Util.nextInt(1, 5)));
        }
        it.options.add(new Item.ItemOption(209, 1));
        return it;
    }

    public static ItemMap khongthegiaodich(Zone zone, int tempId, int quantity, int x, int y, long playerId) {
        ItemMap it = new ItemMap(zone, tempId, quantity, x, y, playerId);
        List<Integer> konggozila = Arrays.asList(1401, 1402, 1403);
        if (konggozila.contains(tempId)) {
            it.options.add(new Item.ItemOption(30, 0));
        }
        it.options.add(new Item.ItemOption(209, 1));
        return it;
    }

    public static int highlightsItem(boolean highlights, int value) {
        double highlightsNumber = 1.1;
        return highlights ? (int) (value * highlightsNumber) : value;
    }

    public static Item sendDo(int itemId, int sql, List<Item.ItemOption> ios) {
        // InventoryServiceNew.gI().addItemBag(player,
        // ItemService.gI().createItemFromItemShop(is));
        // InventoryServiceNew.gI().sendItemBags(player);
        Item item = ItemService.gI().createNewItem((short) itemId);
        item.itemOptions.addAll(ios);
        item.itemOptions.add(new Item.ItemOption(107, sql));
        return item;
    }

    public static boolean checkDo(Item.ItemOption itemOption) {
        switch (itemOption.optionTemplate.id) {
            case 0:// tấn công
                if (itemOption.param > 12000) {
                    return false;
                }
                break;
            case 14:// chí mạng
                if (itemOption.param > 30) {
                    return false;
                }
                break;
            case 107:// spl
            case 102:// spl
                if (itemOption.param > 8) {
                    return false;
                }
                break;
            case 77:
            case 103:
            case 95:
            case 96:
                if (itemOption.param > 41) {
                    return false;
                }
                break;
            case 50:// sd 3%
                if (itemOption.param > 24) {
                    return false;
                }
                break;
            case 6:// hp
            case 7:// ki
                if (itemOption.param > 120000) {
                    return false;
                }
                break;
            case 47:// giáp
                if (itemOption.param > 3500) {
                    return false;
                }
                break;
        }
        return true;
    }

    public static void useCheckDo(Player player, Item item, String position) {
        try {
            if (item.template != null) {
                if (item.template.id >= 381 && item.template.id <= 385) {
                    return;
                }
                if (item.template.id >= 66 && item.template.id <= 135) {
                    return;
                }
                if (item.template.id >= 474 && item.template.id <= 515) {
                    return;
                }
                item.itemOptions.forEach(itemOption -> {
                    if (!Util.checkDo(itemOption)) {
                        Logger.error(player.name + "-" + item.template.name + "-" + position + "\n");
                    }
                });
            }
        } catch (Exception e) {

        }
    }

    public static void showListTop(Player player, byte select) {
        try {
            // Sử dụng TopRankingService thay vì Manager.topSM
            List<TOP> tops = Dragon.services.TopRankingService.getInstance().getTopSucManh();

            Message msg = new Message(-96);
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Top Sức Mạnh");
            msg.writer().writeByte(tops.size());

            for (int i = 0; i < tops.size(); i++) {
                TOP top = tops.get(i);
                msg.writer().writeInt(i + 1);
                msg.writer().writeInt(i + 1);
                msg.writer().writeShort(top.getHead());
                if (player.getSession().version == 15) {// version
                    msg.writer().writeShort(-1);
                }
                msg.writer().writeShort(top.getBody());
                msg.writer().writeShort(top.getLeg());
                msg.writer().writeUTF(top.getName());
                switch (select) {
                    case 0:
                        msg.writer().writeUTF("" + Util.numberToMoney(top.getPower()) + " Sức Mạnh");
                        msg.writer().writeUTF("" + top.getPower() + " Sức Mạnh");
                        break;
                }

            }
            player.sendMessage(msg);
            msg.cleanup();

        } catch (IOException e) {
            Logger.log("err");
            e.printStackTrace();
        }
    }

    public static String phanthuong(int i) {
        switch (i) {
            case 1:
                return "5tr";
            case 2:
                return "3tr";
            case 3:
                return "1tr";
            default:
                return "100k";
        }
    }

    public static int randomBossId() {
        int bossId = Util.nextInt(-10000, -300);
        while (BossManager.gI().getBossById(bossId) != null) {
            bossId = Util.nextInt(-10000, -300);
        }
        return bossId;
    }

    public static long tinhLuyThua(int coSo, int soMu) {
        long ketQua = 1;

        for (int i = 0; i < soMu; i++) {
            ketQua *= coSo;
        }
        return ketQua;
    }

    public static byte[] randomImg() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] array = null;
        try {
            BufferedImage img = new BufferedImage(nextInt(80, 250), nextInt(80, 250), BufferedImage.TYPE_INT_ARGB);
            IntStream.range(0, img.getWidth())
                    .forEach(x -> IntStream.range(0, img.getHeight())
                            .forEach(y -> {
                                int a = ThreadLocalRandom.current().nextInt(256);
                                int r = ThreadLocalRandom.current().nextInt(256);
                                int g = ThreadLocalRandom.current().nextInt(256);
                                int b = ThreadLocalRandom.current().nextInt(256);
                                int p = (a << 24) | (r << 16) | (g << 8) | b;
                                img.setRGB(x, y, p);
                            }));
            ImageIO.write(img, "png", baos);
            array = baos.toByteArray();
        } catch (IOException e) {

        }
        return array;
    }
}
