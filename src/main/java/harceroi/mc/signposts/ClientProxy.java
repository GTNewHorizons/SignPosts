package harceroi.mc.signposts;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import harceroi.mc.signposts.atlas.gui.SignPostsAtlasGui;
import harceroi.mc.signposts.block.SignPostRenderer;
import harceroi.mc.signposts.block.SignPostTileEntity;
import harceroi.mc.signposts.gui.SignPostLabelGui;
import hunternif.mc.atlas.AntiqueAtlasMod;
import hunternif.mc.atlas.api.AtlasAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ClientProxy extends CommonProxy {

  SignPostsAtlasGui atlasGui;
  SignPostLabelGui labelGui;

  public void preInit(FMLPreInitializationEvent event) {
    super.preInit(event);
    ClientRegistry.bindTileEntitySpecialRenderer(SignPostTileEntity.class, new SignPostRenderer());
    
    ResourceLocation signPostMarkerTextureLocation = new ResourceLocation(SignPostsMod.ID + ":" + "textures/gui/markers/signPost.png");
    AtlasAPI.getMarkerAPI().setTexture("signPost", signPostMarkerTextureLocation);
    
  }

  public void init(FMLInitializationEvent event) {
    super.init(event);
    atlasGui = new SignPostsAtlasGui();
    labelGui = new SignPostLabelGui();
  }

  public void openAtlasGui(ItemStack stack) {
    Minecraft mc = Minecraft.getMinecraft();
    if (mc.currentScreen == null) {
      mc.displayGuiScreen(atlasGui.setAtlasItemStack(stack));
    }
  }

  @Override
  public void openSignPostLabelGui(int signX, int signY, int signZ, double jumpX, double jumpY, double jumpZ) {
    Minecraft mc = Minecraft.getMinecraft();
    if(mc.currentScreen == null){
      mc.displayGuiScreen(labelGui.setJumpTargetValues(signX, signY, signZ, jumpX, jumpY, jumpZ));
    }
  }

  

}
