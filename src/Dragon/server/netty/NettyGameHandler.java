package Dragon.server.netty;

import Dragon.server.Client;
import Dragon.server.Controller;
import Dragon.server.ServerManager;
import Dragon.utils.Logger;
import com.girlkun.network.io.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main game handler for Netty connections
 * Manages player sessions and message routing
 */
public class NettyGameHandler extends ChannelInboundHandlerAdapter {

    private static final ConcurrentHashMap<ChannelHandlerContext, NettySession> sessions = new ConcurrentHashMap<>();
    private static final AtomicInteger connectionCounter = new AtomicInteger(0);
    private static final AtomicInteger activeConnections = new AtomicInteger(0);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String clientIp = getClientIp(ctx);

        // Check IP connection limit (same logic as original server)
        if (!canConnectWithIp(clientIp)) {
            Logger.log("NettyGameHandler: Connection rejected for IP: " + clientIp + " (limit exceeded)");
            ctx.close();
            return;
        }

        // Create Netty session wrapper
        NettySession session = new NettySession(ctx, clientIp);
        // Initialize legacy MySession bridge immediately so Controller receives a valid session
        session.initializeMySession();
        sessions.put(ctx, session);

        int connectionId = connectionCounter.incrementAndGet();
        activeConnections.incrementAndGet();

        Logger.log("NettyGameHandler: New connection #" + connectionId + " from " + clientIp);
        Logger.log("NettyGameHandler: Active connections: " + activeConnections.get());

        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NettySession session = sessions.remove(ctx);
        if (session != null) {
            // Handle player disconnect
            if (session.getPlayer() != null) {
                Client.gI().kickSession(session.getMySession());
            }

            activeConnections.decrementAndGet();
            Logger.log("NettyGameHandler: Connection closed. Active connections: " + activeConnections.get());

            // Decrease per-IP connection count to match canConnectWithIp logic
            try {
                String ip = session.getIP();
                Object o = ServerManager.CLIENTS.get(ip);
                if (o != null) {
                    int n = Integer.parseInt(String.valueOf(o));
                    n = Math.max(0, n - 1);
                    if (n == 0) {
                        ServerManager.CLIENTS.remove(ip);
                    } else {
                        ServerManager.CLIENTS.put(ip, n);
                    }
                }
            } catch (Exception ignored) {
            }
        }

        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Message) {
            Message message = (Message) msg;
            NettySession session = sessions.get(ctx);

            if (session != null) {
                // Route message through existing Controller
                Controller.getInstance().onMessage(session.getMySession(), message);
            } else {
                Logger.log("NettyGameHandler: Received message but no session found for channel");
                ctx.close();
            }
        } else {
            Logger.log("NettyGameHandler: Received unknown message type: " + msg.getClass().getName());
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                Logger.log("NettyGameHandler: Connection timeout for " + getClientIp(ctx));
                ctx.close();
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Logger.logException(NettyGameHandler.class, (Exception) cause,
                "Exception in channel " + getClientIp(ctx));
        ctx.close();
    }

    /**
     * Get client IP address from channel context
     */
    private String getClientIp(ChannelHandlerContext ctx) {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        return address.getAddress().getHostAddress();
    }

    /**
     * Check if IP can connect (same logic as original ServerManager)
     */
    private boolean canConnectWithIp(String ipAddress) {
        Object o = ServerManager.CLIENTS.get(ipAddress);
        if (o == null) {
            ServerManager.CLIENTS.put(ipAddress, 1);
            return true;
        } else {
            int n = Integer.parseInt(String.valueOf(o));
            if (n < Dragon.server.Manager.MAX_PER_IP) {
                n++;
                ServerManager.CLIENTS.put(ipAddress, n);
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Get number of active connections
     */
    public static int getActiveConnections() {
        return activeConnections.get();
    }

    /**
     * Force close all connections (for maintenance)
     */
    public static void forceCloseAllConnections() {
        Logger.log("NettyGameHandler: Force closing " + sessions.size() + " connections...");
        sessions.forEach((ctx, session) -> {
            try {
                ctx.close();
            } catch (Exception e) {
                Logger.logException(NettyGameHandler.class, e, "Error closing connection");
            }
        });
        sessions.clear();
        activeConnections.set(0);
    }

    /**
     * Get session by channel context
     */
    public static NettySession getSession(ChannelHandlerContext ctx) {
        return sessions.get(ctx);
    }

    /**
     * Get all active sessions
     */
    public static ConcurrentHashMap<ChannelHandlerContext, NettySession> getAllSessions() {
        return new ConcurrentHashMap<>(sessions);
    }
}
