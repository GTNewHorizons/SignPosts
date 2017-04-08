package harceroi.mc.signposts.network;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import harceroi.mc.signposts.SignPostsMod;

public class SignPostsNetworkHelper {
  
  public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(SignPostsMod.ID);
  
  public static void registerMessageHandlers(){
    INSTANCE.registerMessage(CreateJumpTargetMessageHandler.class, CreateJumpTargetMessage.class, 0, Side.SERVER);
    INSTANCE.registerMessage(JumpMessageHandler.class, JumpMessage.class, 1, Side.SERVER);
  }
  
  public static void sendCreateJumpTargetMessage(CreateJumpTargetMessage msg){
    INSTANCE.sendToServer(msg);
  }
  
  public static void sendJumpMessage(JumpMessage msg){
    INSTANCE.sendToServer(msg);
  }
  
}
