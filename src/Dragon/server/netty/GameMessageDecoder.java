package Dragon.server.netty;

import com.girlkun.network.io.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import Dragon.utils.Logger;

import java.util.List;

/**
 * Decodes incoming bytes into GameMessage objects
 * Handles the protocol conversion from Netty to the existing Message system
 */
public class GameMessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // Ensure we have enough data for a complete header first
        if (in.readableBytes() < 5) { // 1 byte command + 4 bytes length
            return;
        }

        in.markReaderIndex();
        try {
            // Read command byte
            byte command = in.readByte();

            // Read message length (4 bytes)
            int messageLength = in.readInt();

            // Ensure we have the complete message body available
            if (in.readableBytes() < messageLength) {
                // Not enough bytes yet; rewind to the marked position and wait for more data
                in.resetReaderIndex();
                return;
            }

            // Read message data
            byte[] messageData = new byte[messageLength];
            in.readBytes(messageData);

            // Create Message object compatible with existing system
            Message message = new Message(command);
            message.writer().write(messageData);

            out.add(message);

        } catch (Exception e) {
            Logger.logException(GameMessageDecoder.class, e, "Error decoding message");
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Logger.logException(GameMessageDecoder.class, (Exception) cause, "Decoder exception");
        ctx.close();
    }
}
