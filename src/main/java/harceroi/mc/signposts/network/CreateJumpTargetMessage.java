package harceroi.mc.signposts.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class CreateJumpTargetMessage implements IMessage {

    private int signX, signY, signZ;
    private double jumpX, jumpY, jumpZ;
    private String label;

    public CreateJumpTargetMessage() {

    }

    public CreateJumpTargetMessage(int signX, int signY, int signZ, double jumpX, double jumpY, double jumpZ,
        String label) {
        super();
        this.signX = signX;
        this.signY = signY;
        this.signZ = signZ;
        this.jumpX = jumpX;
        this.jumpY = jumpY;
        this.jumpZ = jumpZ;
        this.label = label;
    }

    // public void addSignPost(int signX, int signY, int signZ, double jumpX, double jumpY, double jumpZ, String label,
    // World world)

    @Override
    public void toBytes(ByteBuf buf) {

        buf.writeInt(signX);
        buf.writeInt(signY);
        buf.writeInt(signZ);

        buf.writeDouble(jumpX);
        buf.writeDouble(jumpY);
        buf.writeDouble(jumpZ);

        ByteBufUtils.writeUTF8String(buf, label);

    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.signX = buf.readInt();
        this.signY = buf.readInt();
        this.signZ = buf.readInt();

        this.jumpX = buf.readDouble();
        this.jumpY = buf.readDouble();
        this.jumpZ = buf.readDouble();

        this.label = ByteBufUtils.readUTF8String(buf);

    }

    public int getSignX() {
        return signX;
    }

    public int getSignY() {
        return signY;
    }

    public int getSignZ() {
        return signZ;
    }

    public double getJumpX() {
        return jumpX;
    }

    public double getJumpY() {
        return jumpY;
    }

    public double getJumpZ() {
        return jumpZ;
    }

    public String getLabel() {
        return label;
    }

}
