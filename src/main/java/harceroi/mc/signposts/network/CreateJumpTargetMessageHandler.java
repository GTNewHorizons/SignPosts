package harceroi.mc.signposts.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import harceroi.mc.signposts.SignPostsMod;

public class CreateJumpTargetMessageHandler implements IMessageHandler<CreateJumpTargetMessage, IMessage>{

  @Override
  public IMessage onMessage(CreateJumpTargetMessage message, MessageContext ctx) {     
    SignPostsMod.addJumpTarget(message.getSignX(), message.getSignY(), message.getSignZ(), message.getJumpX(), message.getJumpY(), message.getJumpZ(), message.getLabel(), ctx.getServerHandler().playerEntity);    
    return null;
  }

}
