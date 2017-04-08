package harceroi.mc.signposts.block;

import org.lwjgl.opengl.GL11;

import harceroi.mc.signposts.SignPostsMod;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

public class SignPostRenderer extends TileEntitySpecialRenderer {

  private IModelCustom signPostModel;
  private ResourceLocation signPostTextureLocation;
  private ResourceLocation signPostModelLocation;

  public SignPostRenderer() {
    super();
    signPostModelLocation = new ResourceLocation(SignPostsMod.ID + ":" + "models/signPost.obj");
    signPostTextureLocation = new ResourceLocation(SignPostsMod.ID + ":" + "models/signPost.png");
    signPostModel = AdvancedModelLoader.loadModel(signPostModelLocation);
  }

  @Override
  public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float f) {
    int meta = tileEntity.getBlockMetadata();
    if (meta == 4) return;
    
    GL11.glPushMatrix();
    if (meta == 0) {
      GL11.glTranslatef((float) (x + 1.0F), (float) y, (float) (z + 1.0F));
    } else if (meta == 1) {
      GL11.glTranslatef((float) x, (float) y, (float) (z + 1.0F));
      GL11.glRotatef(270, 0.0F, 1.0F, 0.0F);
    } else if (meta == 2) {
      GL11.glTranslatef((float) x, (float) y, (float) z);
      GL11.glRotatef(180, 0.0F, 1.0F, 0.0F);
    } else if (meta == 3) {
      GL11.glTranslatef((float) (x + 1.0F), (float) y, (float) z);
      GL11.glRotatef(90, 0.0F, 1.0F, 0.0F);
    }

    bindTexture(signPostTextureLocation);
    signPostModel.renderAll();

    // GL11.glPopMatrix();
    // GL11.glPushMatrix();
    /*
    GL11.glEnable(GL11.GL_TEXTURE_2D);
    GL11.glEnable(GL11.GL_BLEND);
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
    GL11.glColor4f(0.25f, 0.95f, 0.51f, 1.0f);
    signPostModel.renderAll();
    
    GL11.glDisable(GL11.GL_BLEND);
    GL11.glColor3f(1F, 1F, 1F);
    */
    GL11.glPopMatrix();
    
  }

}
