package harceroi.mc.signposts.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import harceroi.mc.signposts.SignPostsMod;

public class JumpMessageHandler implements IMessageHandler<JumpMessage, IMessage> {

  @Override
  public IMessage onMessage(JumpMessage msg, MessageContext ctx) {

    SignPostsMod.playerJump(msg.getMarkerId(), ctx.getServerHandler().playerEntity);

    return null;
  }

}
