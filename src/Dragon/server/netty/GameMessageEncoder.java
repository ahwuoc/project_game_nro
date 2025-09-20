package Dragon.server.netty;

import com.girlkun.network.io.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import Dragon.utils.Logger;

/**
 * Encodes GameMessage objects into bytes for transmission
 * Handles the protocol conversion from the existing Message system to Netty
 */
public class GameMessageEncoder extends MessageToByteEncoder<Message> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        try {
            // Write command byte
            out.writeByte(msg.command);

            // Ensure data is flushed to the internal buffer
            msg.writer().flush();

            // Retrieve payload and write length + data
            byte[] payload = msg.getData();
            int len = (payload != null) ? payload.length : 0;
            out.writeInt(len);
            if (len > 0) {
                out.writeBytes(payload);
            }

        } catch (Exception e) {
            Logger.logException(GameMessageEncoder.class, e, "Error encoding message");
            throw e;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Logger.logException(GameMessageEncoder.class, (Exception) cause, "Encoder exception");
        ctx.close();
    }
}
