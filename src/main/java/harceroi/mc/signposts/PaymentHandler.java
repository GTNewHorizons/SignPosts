package harceroi.mc.signposts;

import harceroi.mc.signposts.integration.IPaymentHandler;
import net.minecraft.entity.player.EntityPlayer;

public class PaymentHandler implements IPaymentHandler{

  @Override
  public boolean pay(EntityPlayer player, int x, int y, int z) {
    double distance = Math.pow(Math.abs(player.posX - x), 2) + Math.pow(Math.abs(player.posY - y), 2) + Math.pow(Math.abs(player.posZ - z), 2);
    int exhaustion = Math.min((Math.floorDiv((int) distance, 10000)), 10);
    player.getFoodStats().addStats(-1 * exhaustion, 0.0F);
    return true;
  }
  
  

}
