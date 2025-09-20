package Dragon.server.io;

import java.net.Socket;

import Dragon.models.player.Player;
import Dragon.server.Controller;
import Dragon.data.DataGame;
import Dragon.data.smallVersion;
import Dragon.jdbc.daos.GodGK;
import Dragon.models.item.Item;
import com.girlkun.network.session.Session;
import com.girlkun.network.session.TypeSession;
import com.girlkun.network.io.Message;
import Dragon.server.Client;
import Dragon.server.Maintenance;
import Dragon.server.Manager;
import Dragon.server.model.AntiLogin;
import Dragon.services.ItemService;
import Dragon.services.MapService;
import Dragon.services.Service;
import Dragon.services.func.ChangeMapService;
import Dragon.utils.Logger;
import Dragon.utils.Util;
import Dragon.server.netty.NettySession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySession extends Session {

    private static final Map<String, AntiLogin> ANTILOGIN = new HashMap<>();
    public Player player;

    public byte timeWait = 1;

    public boolean connected;
    public boolean sentKey;

    public static final byte[] KEYS = { 0 };
    public byte curR, curW;

    public String ipAddress;
    public boolean isAdmin;
    public int userId;
    public String uu;
    public String pp;
    public int TongNap;
    public int typeClient;
    public byte zoomLevel;

    public long lastTimeLogout;
    public long lastTimeOff;
    public boolean joinedGame;

    public long lastTimeReadMessage;

    public boolean actived;
    public boolean mtvgtd;
    public boolean vip1d;
    public boolean vip2d;
    public boolean vip3d;
    public boolean vip4d;
    public boolean vip5d;
    public boolean vip6d;

    public int goldBar;
    public int vang;
    public int vip1;
    public int vip2;
    public int vip3;
    public int vip4;
    public int vip5;
    public int vip6;

    public int coinBar;
    public List<Item> itemsReward;
    public String dataReward;
    public boolean is_gift_box;
    public double bdPlayer;

    public int version;
    public int coin;
    public int vnd;
    public int mocnap;
    public int gioithieu;
    public int Bar;
    public boolean isRIcon;
    public int tongnap;

    // Bridge to Netty
    private transient NettySession nettySession;

    public MySession(Socket socket) {
        super(socket);
        ipAddress = socket.getInetAddress().getHostAddress();
        this.isRIcon = false;
    }

    public void setNettySession(NettySession nettySession) {
        this.nettySession = nettySession;
    }

    public void initItemsReward() {
        try {
            this.itemsReward = new ArrayList<>();
            String[] itemsReward = dataReward.split(";");
            for (String itemInfo : itemsReward) {
                if (itemInfo == null || itemInfo.equals("")) {
                    continue;
                }
                String[] subItemInfo = itemInfo.replaceAll("[{}\\[\\]]", "").split("\\|");
                String[] baseInfo = subItemInfo[0].split(":");
                int itemId = Integer.parseInt(baseInfo[0]);
                int quantity = Integer.parseInt(baseInfo[1]);
                Item item = ItemService.gI().createNewItem((short) itemId, quantity);
                if (subItemInfo.length == 2) {
                    String[] options = subItemInfo[1].split(",");
                    for (String opt : options) {
                        if (opt == null || opt.equals("")) {
                            continue;
                        }
                        String[] optInfo = opt.split(":");
                        int tempIdOption = Integer.parseInt(optInfo[0]);
                        int param = Integer.parseInt(optInfo[1]);
                        item.itemOptions.add(new Item.ItemOption(tempIdOption, param));
                    }
                }
                this.itemsReward.add(item);
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void sendKey() throws Exception {
        super.sendKey();
        this.startSend();
    }

    @Override
    public void sendMessage(Message msg) {
        // If running under Netty, forward through Netty channel to avoid legacy socket path
        if (this.nettySession != null) {
            try {
                this.nettySession.sendMessage(msg);
            } catch (Exception e) {
                // fallback to legacy if needed
                try {
                    super.sendMessage(msg);
                } catch (Exception ignored) {
                }
            }
        } else {
            try {
                super.sendMessage(msg);
            } catch (Exception e) {
                // swallow to keep behavior similar to existing code
            }
        }
    }

    public void sendSessionKey() {
        Message msg = new Message(-27);
        try {
            msg.writer().writeByte(KEYS.length);
            msg.writer().writeByte(KEYS[0]);
            for (int i = 1; i < KEYS.length; i++) {
                msg.writer().writeByte(KEYS[i] ^ KEYS[i - 1]);
            }
            this.sendMessage(msg);
            msg.cleanup();
            sentKey = true;
        } catch (Exception e) {

        }
    }

    public void login(String username, String password) {
        // Trace login attempt
        Logger.log("LOGIN: attempt user=" + username + ", ip=" + this.ipAddress + ", sessionId=" + this.id);
        AntiLogin al = ANTILOGIN.get(this.ipAddress);
        if (al == null) {
            al = new AntiLogin();
            ANTILOGIN.put(this.ipAddress, al);
        }
        if (!al.canLogin()) {
            Logger.log("LOGIN: blocked by AntiLogin user=" + username + ", ip=" + this.ipAddress);
            Service.gI().sendThongBaoOK(this, al.getNotifyCannotLogin());
            return;
        }
        if (Manager.LOCAL) {
            Service.gI().sendThongBaoOK(this, "Server này chỉ để lưu dữ liệu\nVui lòng qua server khác");
            return;
        }
        if (Maintenance.isRuning) {
            Service.gI().sendThongBaoOK(this, "Server Đang Bảo Trì, Vui Lòng Quay Lại Sau!");
            return;
        }
        if (Maintenance.isRuning || version > 15) {// version
            Service.gI().sendThongBaoOK(this,
                    "UPDATE RỒI, LÊN TRANG CHỦ NROTUONGLAI.COM TẢI!\n------------------------\n[LƯU Ý: XÓA BẢN CŨ TẢI LẠI BẢN MỚI TRÊN WEB]");
            return;
        }
        if (Maintenance.isRuning || version < 15) {// version
            Service.gI().sendThongBaoOK(this,
                    "UPDATE RỒI, LÊN TRANG CHỦ NROTUONGLAI.COM TẢI!\n------------------------\n[LƯU Ý: XÓA BẢN CŨ TẢI LẠI BẢN MỚI TRÊN WEB]");
            return;
        }
        if (!this.isAdmin && Client.gI().getPlayers().size() >= Manager.MAX_PLAYER) {
            Service.gI().sendThongBaoOK(this, "Máy chủ hiện đang quá tải, "
                    + "cư dân vui lòng di chuyển sang máy chủ khác.");
            return;
        }
        if (this.player != null) {
            return;
        } else {
            Player player = null;
            try {
                this.uu = username;
                this.pp = password;

                player = GodGK.login(this, al);
                if (player != null) {
                    Logger.log("LOGIN: success user=" + username + ", playerId=" + player.id + ", ip=" + this.ipAddress + ", sessionId=" + this.id);
                    smallVersion.send(this);
                    Service.gI().sendMessage(this, -93, "1630679752231_-93_r");
                    this.timeWait = 1;
                    this.joinedGame = true;
                    player.nPoint.calPoint();
                    player.nPoint.setHp((long) player.nPoint.hp);
                    player.nPoint.setMp((long) player.nPoint.mp);
                    player.zone.addPlayer(player);
                    if (player.pet != null) {
                        player.pet.nPoint.calPoint();
                        player.pet.nPoint.setHp(player.pet.nPoint.hp);
                        player.pet.nPoint.setMp(player.pet.nPoint.mp);
                    }

                    player.setSession(this);

                    // Check if player is already online BEFORE adding to Client
                    // Use userId instead of username to ensure correct match even if nhân vật (player.name) khác username
                    Player existingPlayer = Client.gI().getPlayerByUser(this.userId);
                    if (existingPlayer != null) {
                        Logger.log("LOGIN: userId=" + this.userId + " đã online, tiến hành kick session cũ (playerName=" + existingPlayer.name + ")");
                        Client.gI().kickSession(existingPlayer.getSession());
                        // Wait a bit for cleanup
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    Client.gI().put(player);
                    this.player = player;
                    DataGame.sendVersionGame(this);
                    DataGame.sendDataItemBG(this);
                    Controller.getInstance().sendInfo(this);
                    Service.gI().sendThongBao(player, "|30|Chào Bạn Đến Với Ahwuuocdz");
                } else {
                    Logger.log("LOGIN: failed user=" + username + ", ip=" + this.ipAddress + ", sessionId=" + this.id);
                }
            } catch (Exception e) {
                Logger.logException(MySession.class, e, "LOGIN: exception user=" + username + ", ip=" + this.ipAddress + ", sessionId=" + this.id);

                if (player != null) {
                    player.dispose();
                }
            }
        }
    }
}
