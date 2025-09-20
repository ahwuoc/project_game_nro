package Dragon.server.netty;

import Dragon.server.io.MySession;
import Dragon.models.player.Player;
import io.netty.channel.ChannelHandlerContext;

import java.net.SocketAddress;

/**
 * Netty session wrapper that provides compatibility with the existing MySession
 * system
 * Bridges Netty channels with the game's session management
 */
public class NettySession {

    private final ChannelHandlerContext ctx;
    private final String ipAddress;
    private final long connectionTime;
    private MySession mySession;
    private Player player;
    private boolean connected = false;
    private boolean sentKey = false;

    // Session properties (compatible with MySession)
    public boolean isAdmin = false;
    public int userId = 0;
    public String uu = "";
    public String pp = "";
    public int TongNap = 0;
    public int typeClient = 0;
    public byte zoomLevel = 0;
    public long lastTimeLogout = 0;
    public long lastTimeOff = 0;
    public boolean joinedGame = false;
    public long lastTimeReadMessage = 0;
    public boolean actived = false;
    public boolean mtvgtd = false;
    public boolean vip1d = false;
    public boolean vip2d = false;
    public boolean vip3d = false;
    public boolean vip4d = false;
    public boolean vip5d = false;
    public boolean vip6d = false;
    public int goldBar = 0;
    public int vang = 0;
    public int vip1 = 0;
    public int vip2 = 0;
    public int vip3 = 0;
    public int vip4 = 0;
    public int vip5 = 0;
    public int vip6 = 0;
    public int coinBar = 0;
    public boolean is_gift_box = false;
    public double bdPlayer = 0.0;
    public int version = 0;
    public int coin = 0;
    public int vnd = 0;
    public int mocnap = 0;
    public int gioithieu = 0;
    public int Bar = 0;
    public boolean isRIcon = false;
    public int tongnap = 0;

    public NettySession(ChannelHandlerContext ctx, String ipAddress) {
        this.ctx = ctx;
        this.ipAddress = ipAddress;
        this.connectionTime = System.currentTimeMillis();
        this.isRIcon = false;
    }

    /**
     * Initialize MySession wrapper
     */
    public void initializeMySession() {
        if (mySession == null) {
            // Create a mock socket for MySession compatibility
            MockSocket mockSocket = new MockSocket(ipAddress);
            mySession = new MySession(mockSocket);

            // Copy properties
            mySession.ipAddress = this.ipAddress;
            mySession.isAdmin = this.isAdmin;
            mySession.userId = this.userId;
            mySession.uu = this.uu;
            mySession.pp = this.pp;
            mySession.TongNap = this.TongNap;
            mySession.typeClient = this.typeClient;
            mySession.zoomLevel = this.zoomLevel;
            mySession.lastTimeLogout = this.lastTimeLogout;
            mySession.lastTimeOff = this.lastTimeOff;
            mySession.joinedGame = this.joinedGame;
            mySession.lastTimeReadMessage = this.lastTimeReadMessage;
            mySession.actived = this.actived;
            mySession.mtvgtd = this.mtvgtd;
            mySession.vip1d = this.vip1d;
            mySession.vip2d = this.vip2d;
            mySession.vip3d = this.vip3d;
            mySession.vip4d = this.vip4d;
            mySession.vip5d = this.vip5d;
            mySession.vip6d = this.vip6d;
            mySession.goldBar = this.goldBar;
            mySession.vang = this.vang;
            mySession.vip1 = this.vip1;
            mySession.vip2 = this.vip2;
            mySession.vip3 = this.vip3;
            mySession.vip4 = this.vip4;
            mySession.vip5 = this.vip5;
            mySession.vip6 = this.vip6;
            mySession.coinBar = this.coinBar;
            mySession.is_gift_box = this.is_gift_box;
            mySession.bdPlayer = this.bdPlayer;
            mySession.version = this.version;
            mySession.coin = this.coin;
            mySession.vnd = this.vnd;
            mySession.mocnap = this.mocnap;
            mySession.gioithieu = this.gioithieu;
            mySession.Bar = this.Bar;
            mySession.isRIcon = this.isRIcon;
            mySession.tongnap = this.tongnap;

            // Set player reference
            mySession.player = this.player;

            // Bridge back so MySession can send via Netty channel
            mySession.setNettySession(this);

            // Mark as connected to align with legacy flags
            this.connected = true;
        }
    }

    /**
     * Send message through Netty channel
     */
    public void sendMessage(com.girlkun.network.io.Message message) {
        if (ctx.channel().isActive()) {
            ctx.writeAndFlush(message);
        }
    }

    /**
     * Disconnect the session
     */
    public void disconnect() {
        if (ctx.channel().isActive()) {
            ctx.close();
        }
    }

    /**
     * Check if session is connected
     */
    public boolean isConnected() {
        return ctx.channel().isActive() && connected;
    }

    /**
     * Get IP address
     */
    public String getIP() {
        return ipAddress;
    }

    /**
     * Get MySession wrapper
     */
    public MySession getMySession() {
        return mySession;
    }

    /**
     * Get player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Set player
     */
    public void setPlayer(Player player) {
        this.player = player;
        if (mySession != null) {
            mySession.player = player;
        }
    }

    /**
     * Get channel context
     */
    public ChannelHandlerContext getChannelContext() {
        return ctx;
    }

    /**
     * Get connection time
     */
    public long getConnectionTime() {
        return connectionTime;
    }

    /**
     * Mock Socket class for MySession compatibility
     */
    private static class MockSocket extends java.net.Socket {
        private final String ipAddress;

        public MockSocket(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        @Override
        public SocketAddress getRemoteSocketAddress() {
            return new SocketAddress() {
                @Override
                public String toString() {
                    return ipAddress + ":0";
                }
            };
        }

        @Override
        public boolean isClosed() {
            return false; // Netty manages the actual connection
        }

        @Override
        public boolean isConnected() {
            return true; // Netty manages the actual connection
        }
    }
}
