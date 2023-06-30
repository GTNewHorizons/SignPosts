package harceroi.mc.signposts.item;

import harceroi.mc.signposts.configuration.ConfigurationHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class SignPostMarkerItem extends Item {

    public SignPostMarkerItem() {
        super();
        int maxDamage = ConfigurationHandler.getMarkerMaxUsage();
        if (maxDamage != -1){
            setMaxDamage(maxDamage);
        }

        maxStackSize = 1;
    }

    public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float p_77648_8_, float p_77648_9_, float p_77648_10_) {
        return true;
    }

    @Override
    public int getDamage(ItemStack stack) {
        if(ConfigurationHandler.getMarkerMaxUsage() == -1){
            return 0;
        }
        return super.getDamage(stack);
    }

}
