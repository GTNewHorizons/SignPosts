package harceroi.mc.signposts.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class JumpMessage implements IMessage {

  private int markerId;

  public JumpMessage() {
  }

  public JumpMessage(int markerId) {
    super();
    this.markerId = markerId;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    markerId = buf.readInt();
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeInt(markerId);
  }

  public int getMarkerId() {
    return markerId;
  }

}
