package harceroi.mc.signposts.integration.gollorum;

import net.minecraft.entity.player.EntityPlayer;

import gollorum.signpost.management.PostHandler;
import harceroi.mc.signposts.integration.IPaymentHandler;

public class GollorumPaymentHandler implements IPaymentHandler {

    @Override
    public boolean pay(EntityPlayer player, int x, int y, int z) {
        return PostHandler.pay(player, (int) player.posX, (int) player.posY, (int) player.posZ, x, y, z);
    }

}
