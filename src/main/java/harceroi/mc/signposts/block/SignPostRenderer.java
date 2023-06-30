package harceroi.mc.signposts.block;

import hunternif.mc.atlas.AntiqueAtlasMod;
import hunternif.mc.atlas.marker.MarkersData;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelSign;
import net.minecraft.client.renderer.tileentity.TileEntitySignRenderer;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

import net.minecraft.util.ResourceLocation;

public class SignPostRenderer extends TileEntitySignRenderer {

    private static final ResourceLocation texture = new ResourceLocation("textures/entity/sign.png");
    private final ModelSign model = new ModelSign();

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float p_147500_8_) {
        int meta = tileEntity.getBlockMetadata();
        if(meta >= 4) {
            return;
        }

        float realX = 0.5f, realY = 0.5f, rotation = 0f;

        switch(meta) {
            case 0:
                realY += 0.13f;
                break;
            case 1:
                realX -= 0.13f;
                rotation = 90f;
                break;
            case 2:
                realY -= 0.13f;
                rotation = 180f;
                break;
            case 3:
                realX += 0.13f;
                rotation = 270f;
                break;
        }

        GL11.glPushMatrix();
        float scale = 0.6666667F;

        GL11.glTranslatef((float)x + realX, (float)y + 1.75F * scale, (float)z + realY);
        GL11.glRotatef(-rotation, 0.0F, 1.0F, 0.0F);
        model.signStick.showModel = false;

        bindTexture(texture);
        GL11.glPushMatrix();
        GL11.glScalef(scale, -scale, -scale);
        model.renderSign();

        GL11.glPopMatrix();

        float newScale = 0.016666668F * scale;
        GL11.glTranslatef(0.0F, 0.5F * scale, 0.07F * scale);
        GL11.glScalef(newScale, -newScale, newScale);
        GL11.glNormal3f(0.0F, 0.0F, -1.0F * newScale);
        GL11.glDepthMask(false);

        SignPostTileEntity signTe = (SignPostTileEntity)tileEntity;
        if(signTe.text == null) {
            int id = signTe.getMarkerId();
            if(id != 0) {
                MarkersData data = AntiqueAtlasMod.globalMarkersData.getData();
                signTe.text = data.getMarkerByID(id).getLocalizedLabel();
            }
        } else {
            FontRenderer fontrenderer = func_147498_b();
            fontrenderer.drawString(signTe.text, -fontrenderer.getStringWidth(signTe.text) / 2, 2 * 10 - 4 * 5, 0);
        }

        GL11.glDepthMask(true);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
    }
}
