package Dragon.server.netty;

import Dragon.utils.Logger;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * Netty-based server implementation for high-performance networking
 * Replaces the traditional thread-per-connection model with event-driven
 * architecture
 */
public class NettyServer {

    private static NettyServer instance;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private boolean isRunning = false;

    // Configuration
    private static final int BOSS_THREADS = 1; // Usually 1 is enough for game servers
    private static final int WORKER_THREADS = Runtime.getRuntime().availableProcessors() * 2;
    private static final int MAX_FRAME_LENGTH = 1024 * 1024; // 1MB max message size
    private static final int LENGTH_FIELD_LENGTH = 4;
    private static final int LENGTH_ADJUSTMENT = 0;
    private static final int INITIAL_BYTES_TO_STRIP = 0;
    private static final int READER_IDLE_TIME = 30; // seconds
    private static final int WRITER_IDLE_TIME = 0; // seconds
    private static final int ALL_IDLE_TIME = 0; // seconds

    public static NettyServer getInstance() {
        if (instance == null) {
            instance = new NettyServer();
        }
        return instance;
    }

    private NettyServer() {
        // Private constructor for singleton
    }

    /**
     * Start the Netty server
     */
    public void start(int port) throws InterruptedException {
        if (isRunning) {
            Logger.log("NettyServer: Server is already running!");
            return;
        }

        Logger.log("NettyServer: Starting Netty server on port " + port + "...");
        Logger.log("NettyServer: Boss threads: " + BOSS_THREADS + ", Worker threads: " + WORKER_THREADS);

        // Create event loop groups
        bossGroup = new NioEventLoopGroup(BOSS_THREADS);
        workerGroup = new NioEventLoopGroup(WORKER_THREADS);

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_RCVBUF, 64 * 1024) // 64KB receive buffer
                    .childOption(ChannelOption.SO_SNDBUF, 64 * 1024) // 64KB send buffer
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();

                            // Custom message codec
                            pipeline.addLast("messageDecoder", new GameMessageDecoder());
                            pipeline.addLast("messageEncoder", new GameMessageEncoder());

                            // Idle state handler for connection management
                            pipeline.addLast("idleStateHandler", new IdleStateHandler(
                                    READER_IDLE_TIME, WRITER_IDLE_TIME, ALL_IDLE_TIME, TimeUnit.SECONDS));

                            // Main game handler
                            pipeline.addLast("gameHandler", new NettyGameHandler());
                        }
                    });

            // Bind and start to accept incoming connections
            ChannelFuture future = bootstrap.bind(port).sync();
            serverChannel = future.channel();
            isRunning = true;

            Logger.log("NettyServer: Server started successfully on port " + port);
            Logger.log("NettyServer: Ready to accept connections!");

            // Wait until the server socket is closed
            future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            Logger.logException(NettyServer.class, e, "Netty server interrupted");
            throw e;
        } finally {
            shutdown();
        }
    }

    /**
     * Shutdown the Netty server gracefully
     */
    public void shutdown() {
        if (!isRunning) {
            return;
        }

        Logger.log("NettyServer: Shutting down server...");
        isRunning = false;

        if (serverChannel != null) {
            serverChannel.close();
        }

        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }

        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }

        Logger.log("NettyServer: Server shutdown complete");
    }

    /**
     * Check if server is running
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Get server statistics
     */
    public String getStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("NettyServer Stats:\n");
        stats.append("- Status: ").append(isRunning ? "Running" : "Stopped").append("\n");
        stats.append("- Boss Threads: ").append(BOSS_THREADS).append("\n");
        stats.append("- Worker Threads: ").append(WORKER_THREADS).append("\n");
        stats.append("- Max Frame Length: ").append(MAX_FRAME_LENGTH).append(" bytes\n");
        stats.append("- Active Connections: ").append(NettyGameHandler.getActiveConnections()).append("\n");
        return stats.toString();
    }

    /**
     * Force close all connections (for maintenance)
     */
    public void forceCloseAllConnections() {
        Logger.log("NettyServer: Force closing all connections...");
        NettyGameHandler.forceCloseAllConnections();
    }
}
