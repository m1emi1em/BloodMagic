package WayofTime.alchemicalWizardry.common.items;

import net.minecraft.client.renderer.texture.IIconRegister;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class DawnScribeTool extends ScribeTool {

    public DawnScribeTool() {
        super(6);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister) {
        this.itemIcon = iconRegister.registerIcon("AlchemicalWizardry:DawnScribeTool");
    }
}
