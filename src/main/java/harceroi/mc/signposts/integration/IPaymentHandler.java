package harceroi.mc.signposts.integration;

import net.minecraft.entity.player.EntityPlayer;

public interface IPaymentHandler {

    public boolean pay(EntityPlayer player, int x, int y, int z);

}
