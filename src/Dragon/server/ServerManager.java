package Dragon.server;

import com.girlkun.database.GirlkunDB;

import java.net.ServerSocket;
import java.awt.GraphicsEnvironment;

import Dragon.Bot.BotManager;
import Dragon.jdbc.daos.HistoryTransactionDAO;
import Dragon.jdbc.daos.PlayerDAO;
import Dragon.kygui.ShopKyGuiManager;
import Dragon.kygui.ShopKyGuiService;
import Dragon.models.ThanhTich.CheckDataDay;
import Dragon.models.boss.BossManager;
import Dragon.models.boss.RefactoredBossManager;
import Dragon.models.item.Item;
import Dragon.models.map.BDKB.BanDoKhoBau;
import Dragon.models.map.Zone;
import Dragon.models.map.challenge.MartialCongressManager;
import Dragon.models.map.vodai.VoDaiManager;
import Dragon.models.player.Player;
import Dragon.models.sieuhang.SieuHangManager;
import Dragon.thuongnhanthanbi.Dungeon_Manager;
import com.girlkun.network.session.ISession;
import com.girlkun.network.example.MessageSendCollect;
import com.girlkun.network.server.GirlkunServer;
import com.girlkun.network.server.IServerClose;
import com.girlkun.network.server.ISessionAcceptHandler;
import static Dragon.server.Maintenance.isBaoTri;
import Dragon.server.io.MyKeyHandler;
import Dragon.server.io.MySession;
import Dragon.services.*;
import Dragon.models.npc.NpcFactory;
import Dragon.server.GameLoopManager;
import Dragon.server.netty.NettyServerManager;
import Dragon.utils.Logger;
import Dragon.utils.TimeUtil;
import Dragon.utils.Util;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import Dragon.services.func.TaiXiu;
import com.girlkun.result.GirlkunResultSet;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class ServerManager {

    public int threadMap;

    public static String timeStart;

    public static final Map CLIENTS = new HashMap();

    public static String NAME = "Ahwuocdz";
    public static int PORT = 14445;

    private static ServerManager instance;

    public static ServerSocket listenSocket;
    public static boolean isRunning;

    public void init() {
        NpcFactory.initializeNpcInstances();
        Manager.gI();
        try {
            if (Manager.LOCAL) {
                return;
            }
        } catch (Exception e) {
            System.err.print("\nError at 310\n");
            e.printStackTrace();
        }
    }

    public static ServerManager gI() {
        if (instance == null) {
            instance = new ServerManager();
            instance.init();
        }
        return instance;
    }

    public static void main(String[] args) {
        timeStart = TimeUtil.getTimeNow("dd/MM/yyyy HH:mm:ss");
        ServerManager serverManager = ServerManager.gI();
        serverManager.run();
        try {
            boolean enableUi = Boolean.parseBoolean(System.getProperty("enable.ui", "false"));
            if (enableUi && !GraphicsEnvironment.isHeadless()) {
                menu.main(args);
            } else {
                Logger.log(Logger.YELLOW, "UI disabled or headless environment detected. Skipping Swing menu.\n");
            }
        } catch (Throwable t) {
            Logger.log(Logger.RED, "Failed to start Swing menu (ignored): " + t.getClass().getSimpleName() + " - "
                    + t.getMessage() + "\n");
        }
        // Tạo và chạy player ảo
        // Playerao.createVirtualPlayers(200); // Thay đổi số lượng player ảo tùy thích
    }

    public void run() {
        long delay = 500;
        activeCommandLine();
        activeGame();
        activeServerSocket();
        Logger.log(Logger.GREEN, "Voice Chat Service integrated with game server\n");
        TaiXiu.gI().lastTimeEnd = System.currentTimeMillis() + 50000;
        Logger.log(Logger.YELLOW, "TIME STARTING THE SERVER: " + ServerManager.timeStart + "\n");
        // Logger.log(Logger.GREEN, "BY NROEVEIL");

        new Thread(() -> {
            while (true) {
                try {
                    SieuHangManager.gI().update();
                    Thread.sleep(50);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        new Thread(BotManager.gI(), "Thread Bot Game").start();
        new Thread(TaiXiu.gI(), "Thread TaiXiu").start();
        isRunning = true;
        new Thread(() -> {
            while (isRunning) {
                try {
                    long start = System.currentTimeMillis();
                    MartialCongressManager.gI().update();
                    Dungeon_Manager.gI().globalUpdate();
                    VoDaiManager.gI().update();
                    Dungeon_Manager.gI().globalUpdate();
                    Player player = null;
                    for (int i = 0; i < Client.gI().getPlayers().size(); ++i) {
                        if (Client.gI().getPlayers().get(i) != null) {
                            player = (Client.gI().getPlayers().get(i));
                        }
                    }
                    ShopKyGuiManager.gI().save();
                    long timeUpdate = System.currentTimeMillis() - start;
                    if (timeUpdate < delay) {
                        Thread.sleep(delay - timeUpdate);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "Update dai hoi vo thuat").start();
        try {
            Thread.sleep(1000);
            RefactoredBossManager.getInstance().loadBosses();
            Manager.MAPS.forEach(Dragon.models.map.Map::initBoss);
        } catch (InterruptedException ex) {
            System.err.print("\nError at 311\n");
            java.util.logging.Logger.getLogger(BossManager.class.getName()).log(Level.SEVERE, null, ex);

        }
    }

    private void act() throws Exception {
        // Check if Netty mode is enabled
        if (NettyServerManager.isNettyModeEnabled()) {
            Logger.log("ServerManager: Starting Netty server...");
            NettyServerManager.getInstance().startNettyServer(PORT);
            return;
        }

        // Traditional server startup
        Logger.log("ServerManager: Starting traditional server...");
        GirlkunServer.gI().init().setAcceptHandler(new ISessionAcceptHandler() {
            @Override
            public void sessionInit(ISession is) {
                if (!canConnectWithIp(is.getIP())) {
                    is.disconnect();
                    return;
                }

                is = is.setMessageHandler(Controller.getInstance())
                        .setSendCollect(new MessageSendCollect())
                        .setKeyHandler(new MyKeyHandler())
                        .startCollect();
            }

            @Override
            public void sessionDisconnect(ISession session) {
                Client.gI().kickSession((MySession) session);
            }
        }).setTypeSessioClone(MySession.class)
                .setDoSomeThingWhenClose(new IServerClose() {
                    @Override
                    public void serverClose() {
                        System.out.println("server close");
                        System.exit(0);
                    }
                })
                .start(PORT);

    }

    private void activeServerSocket() {
        if (true) {
            try {
                this.act();
            } catch (Exception e) {
                System.err.print("\nError at 312\n");
                e.printStackTrace();
            }
            return;
        }
    }

    private boolean canConnectWithIp(String ipAddress) {
        Object o = CLIENTS.get(ipAddress);
        if (o == null) {
            CLIENTS.put(ipAddress, 1);
            return true;
        } else {
            int n = Integer.parseInt(String.valueOf(o));
            if (n < Manager.MAX_PER_IP) {
                n++;
                CLIENTS.put(ipAddress, n);
                return true;
            } else {
                return false;
            }
        }
    }

    public void disconnect(MySession session) {
        Object o = CLIENTS.get(session.getIP());
        if (o != null) {
            int n = Integer.parseInt(String.valueOf(o));
            n--;
            if (n < 0) {
                n = 0;
            }
            CLIENTS.put(session.getIP(), n);
        }
    }

    private void activeCommandLine() {
        if (System.console() == null) {
            Logger.log(Logger.YELLOW, "Command-line input disabled (no console available).\n");
            return;
        }
        new Thread(() -> {
            Scanner sc = new Scanner(System.in);
            while (true) {
                if (!sc.hasNextLine()) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    continue;
                }
                String line = sc.nextLine();
                if (line.equals("savekigui")) {
                }
                if (line.equals("baotri")) {
                    Maintenance.gI().start(2);
                }
                if (line.equals("lavie")) {
                    ClanService.gI().saveclan();
                } else if (line.equals("online")) {
                    List<String> lines = new ArrayList<>();
                    ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
                    ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds());
                    System.out.println("Danh sách tên các luồng đang chạy:");
                    lines.add("Danh sách tên các luồng đang chạy:");
                    for (ThreadInfo threadInfo : threadInfos) {
                        lines.add(threadInfo.getThreadName());
                        System.out.println(threadInfo.getThreadName());
                    }
                    Path file = Paths.get("DataThread.txt");
                    try {
                        Files.write(file, lines, StandardCharsets.UTF_8);
                    } catch (IOException ex) {
                        java.util.logging.Logger.getLogger(ServerManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    Logger.log(Logger.PURPLE, "Thread" + (Thread.activeCount() - threadMap) + "\nOnline:"
                            + Client.gI().getPlayers().size());

                } else if (line.equals("nplayer")) {
                    Logger.error("Player in game: " + Client.gI().getPlayers().size() + "\n");
                } else if (line.equals("admin")) {
                    new Thread(() -> {
                        Client.gI().close();
                    }, "adminThread").start();
                } else if (line.startsWith("bang")) {
                    new Thread(() -> {
                        try {
                            ClanService.gI().close();
                            Logger.error("Save " + Manager.CLANS.size() + " bang");
                        } catch (Exception e) {

                            e.printStackTrace();
                            Logger.error("Thông báo: lỗi lưu dữ liệu bang hội.\n");

                        }
                    }, "bangThread").start();
                } else if (line.startsWith("a")) {
                    String a = line.replace("a ", "");
                    Service.gI().sendThongBaoAllPlayer(a);
                } else if (line.startsWith("tb")) {
                    String a = line.replace("tb ", "");
                    Service.gI().sendBangThongBaoAllPlayervip(a);
                } else if (line.startsWith("qua")) {
                    try {
                        List<Item.ItemOption> ios = new ArrayList<>();
                        String[] pagram1 = line.split("=")[1].split("-");
                        String[] pagram2 = line.split("=")[2].split("-");
                        if (pagram1.length == 4 && pagram2.length % 2 == 0) {
                            Player p = Client.gI().getPlayer(Integer.parseInt(pagram1[0]));
                            if (p != null) {
                                for (int i = 0; i < pagram2.length; i += 2) {
                                    ios.add(new Item.ItemOption(Integer.parseInt(pagram2[i]),
                                            Integer.parseInt(pagram2[i + 1])));
                                }
                                Item i = Util.sendDo(Integer.parseInt(pagram1[2]), Integer.parseInt(pagram1[3]), ios);
                                i.quantity = Integer.parseInt(pagram1[1]);
                                InventoryServiceNew.gI().addItemBag(p, i);
                                InventoryServiceNew.gI().sendItemBags(p);
                                Service.gI().sendThongBao(p, "Admin trả đồ. anh em thông cảm nhé...");
                            } else {
                                System.out.println("Người chơi không online");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Lỗi quà");

                    }
                } else if (line.equals("refreshmobcache")) {
                    try {
                        Dragon.utils.Logger.log("ServerManager: Refreshing Mob Reward Cache...");
                        Dragon.jdbc.daos.MobRewardService.getInstance().refreshCache();
                        Dragon.utils.Logger.log("ServerManager: Mob Reward Cache refreshed successfully!");
                        Dragon.utils.Logger.log(
                                "ServerManager: " + Dragon.jdbc.daos.MobRewardService.getInstance().getCacheStats());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Dragon.utils.Logger.log("ServerManager: Failed to refresh Mob Reward Cache!");
                    }
                } else if (line.equals("mobcachestats")) {
                    try {
                        Dragon.utils.Logger.log(
                                "ServerManager: " + Dragon.jdbc.daos.MobRewardService.getInstance().getCacheStats());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Dragon.utils.Logger.log("ServerManager: Failed to get cache stats!");
                    }
                } else if (line.equals("refreshbosscache")) {
                    try {
                        Dragon.utils.Logger.log("ServerManager: Refreshing Boss Reward Cache...");
                        Dragon.jdbc.daos.BossRewardService.getInstance().refreshCache();
                        Dragon.utils.Logger.log("ServerManager: Boss Reward Cache refreshed successfully!");
                        Dragon.utils.Logger.log(
                                "ServerManager: " + Dragon.jdbc.daos.BossRewardService.getInstance().getCacheStats());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Dragon.utils.Logger.log("ServerManager: Failed to refresh Boss Reward Cache!");
                    }
                } else if (line.equals("bosscachestats")) {
                    try {
                        Dragon.utils.Logger.log(
                                "ServerManager: " + Dragon.jdbc.daos.BossRewardService.getInstance().getCacheStats());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Dragon.utils.Logger.log("ServerManager: Failed to get boss cache stats!");
                    }
                } else if (line.equals("refreshgiftcache")) {
                    try {
                        Dragon.utils.Logger.log("ServerManager: Refreshing Gift Code Cache...");
                        Dragon.jdbc.daos.GiftCodeCache.getInstance().refreshCache();
                        Dragon.utils.Logger.log("ServerManager: Gift Code Cache refreshed successfully!");
                        Dragon.utils.Logger.log(
                                "ServerManager: " + Dragon.jdbc.daos.GiftCodeCache.getInstance().getCacheStats());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Dragon.utils.Logger.log("ServerManager: Failed to refresh Gift Code Cache!");
                    }
                } else if (line.equals("giftcachestats")) {
                    try {
                        Dragon.utils.Logger.log(
                                "ServerManager: " + Dragon.jdbc.daos.GiftCodeCache.getInstance().getCacheStats());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Dragon.utils.Logger.log("ServerManager: Failed to get gift code cache stats!");
                    }
                } else if (line.equals("reloadgiftcodes")) {
                    try {
                        Dragon.utils.Logger.log("ServerManager: Reloading Gift Codes from database...");
                        Dragon.jdbc.daos.GiftCodeCache.getInstance().clearCache();
                        Dragon.jdbc.daos.GiftCodeCache.getInstance().initializeCache();
                        Dragon.utils.Logger.log("ServerManager: Gift Codes reloaded successfully!");
                        Dragon.utils.Logger.log(
                                "ServerManager: " + Dragon.jdbc.daos.GiftCodeCache.getInstance().getCacheStats());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Dragon.utils.Logger.log("ServerManager: Failed to reload Gift Codes!");
                    }
                } else if (line.equals("gameloopstats")) {
                    try {
                        Dragon.utils.Logger.log("ServerManager: GameLoopManager Performance Stats:");
                        Dragon.utils.Logger.log(GameLoopManager.getInstance().getPerformanceStats());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Dragon.utils.Logger.log("ServerManager: Failed to get GameLoop stats!");
                    }
                } else if (line.equals("netty")) {
                    try {
                        Dragon.utils.Logger.log("ServerManager: Netty Server Manager:");
                        Dragon.utils.Logger.log(NettyServerManager.getInstance().getStats());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Dragon.utils.Logger.log("ServerManager: Failed to get Netty stats!");
                    }
                } else if (line.equals("enablenetty")) {
                    try {
                        Dragon.utils.Logger.log("ServerManager: Enabling Netty mode...");
                        NettyServerManager.getInstance().enableNettyMode();
                        Dragon.utils.Logger.log("ServerManager: Netty mode enabled! Restart server to use Netty.");
                    } catch (Exception e) {
                        e.printStackTrace();
                        Dragon.utils.Logger.log("ServerManager: Failed to enable Netty mode!");
                    }
                } else if (line.equals("disablenetty")) {
                    try {
                        Dragon.utils.Logger.log("ServerManager: Disabling Netty mode...");
                        NettyServerManager.getInstance().disableNettyMode();
                        Dragon.utils.Logger.log(
                                "ServerManager: Traditional mode enabled! Restart server to use traditional server.");
                    } catch (Exception e) {
                        e.printStackTrace();
                        Dragon.utils.Logger.log("ServerManager: Failed to disable Netty mode!");
                    }
                } else if (line.equals("startnetty")) {
                    try {
                        Dragon.utils.Logger.log("ServerManager: Starting Netty server...");
                        NettyServerManager.getInstance().startNettyServer(PORT);
                        Dragon.utils.Logger.log("ServerManager: Netty server started!");
                    } catch (Exception e) {
                        e.printStackTrace();
                        Dragon.utils.Logger.log("ServerManager: Failed to start Netty server!");
                    }
                } else if (line.equals("stopnetty")) {
                    try {
                        Dragon.utils.Logger.log("ServerManager: Stopping Netty server...");
                        NettyServerManager.getInstance().stopNettyServer();
                        Dragon.utils.Logger.log("ServerManager: Netty server stopped!");
                    } catch (Exception e) {
                        e.printStackTrace();
                        Dragon.utils.Logger.log("ServerManager: Failed to stop Netty server!");
                    }
                } else if (line.equals("closeallnetty")) {
                    try {
                        Dragon.utils.Logger.log("ServerManager: Force closing all Netty connections...");
                        NettyServerManager.getInstance().forceCloseAllConnections();
                        Dragon.utils.Logger.log("ServerManager: All Netty connections closed!");
                    } catch (Exception e) {
                        e.printStackTrace();
                        Dragon.utils.Logger.log("ServerManager: Failed to close Netty connections!");
                    }
                }
            }
        }, "activeCommandLineThread").start();
    }

    private void activeGame() {
        final long delay = 2000; // Thời gian delay là 5000ms (5 giây)

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                while (!isBaoTri) {
                    for (BanDoKhoBau bando : BanDoKhoBau.BAN_DO_KHO_BAUS) {
                        bando.update();
                    }

                    ClanService.gI().saveclan();
                    Service.gI().AutoSavePlayerData();
                    ShopKyGuiManager.gI().save();

                    Thread.sleep(delay);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("\nThread interrupted\n");
            } catch (Exception e) {
                System.err.print("\nError at 314\n");
                e.printStackTrace();
            }
        });

        executor.shutdown();
    }

    public void close(long delay) {
        isRunning = false;
        try {
            GirlkunServer.gI().stopConnect();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("Thông báo: Lỗi Đóng kết nối tới server.\n");
        }
        Logger.log(Logger.BLACK, "\nĐóng kết nối tới server.\n");
        try {
            Client.gI().close();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("Thông báo: Lỗi lưu dử liệu người chơi.\n");
        }
        try {
            ClanService.gI().close();
            Logger.log(Logger.BLACK, "Lưu dử liệu bang hội\n");
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("Thông báo: lỗi lưu dữ liệu bang hội.\n");
        }
        try {
            ShopKyGuiManager.gI().save();
            Logger.log(Logger.BLACK, "Lưu dử liệu ký gửi\n");
        } catch (InterruptedException ex) {
            System.err.print("\nError at 315\n");
            ex.printStackTrace();
        }
        try {
            CheckDataDay.ResetDataDay();
            Logger.log(Logger.BLACK,
                    "Reset Dữ Liệu Hoạt Động Hằng Ngày - Quà Nạp Hằng Ngày.\n");
        } catch (SQLException ex) {
            System.err.print("\nError at 316\n");
            ex.printStackTrace();
        }

        Logger.log(Logger.BLACK, "Bảo trì đóng server thành công.\n");
        System.exit(0);
    }

    public long getNumPlayer() {
        long num = 0;
        try {
            GirlkunResultSet rs = GirlkunDB.executeQuery("SELECT COUNT(*) FROM `player`");
            rs.first();
            num = rs.getLong(1);
        } catch (Exception e) {
        }
        return num;
    }
}
