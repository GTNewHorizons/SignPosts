package harceroi.mc.signposts.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class JumpMessage implements IMessage {

    private int markerId;
    private String paymentHandler;

    public JumpMessage() {}

    public JumpMessage(int markerId, String paymentHandler) {
        super();
        this.markerId = markerId;
        this.paymentHandler = paymentHandler;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        markerId = buf.readInt();
        paymentHandler = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(markerId);
        ByteBufUtils.writeUTF8String(buf, paymentHandler);
    }

    public int getMarkerId() {
        return markerId;
    }

    public String getPaymentHandler() {
        return paymentHandler;
    }

}
