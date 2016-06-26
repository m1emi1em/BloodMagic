package WayofTime.alchemicalWizardry.common.items;

import java.util.List;

import WayofTime.alchemicalWizardry.AlchemicalWizardry;
import WayofTime.alchemicalWizardry.common.entity.projectile.EnergyBlastProjectile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class EnergyBlast extends EnergyItems
{
    @SideOnly(Side.CLIENT)
    private IIcon activeIcon;
    @SideOnly(Side.CLIENT)
    private IIcon activeIconTier2;
    @SideOnly(Side.CLIENT)
    private IIcon activeIconTier3;
    @SideOnly(Side.CLIENT)
    private IIcon passiveIcon;
    private int tier;
    private int damage;

    public EnergyBlast(int tier)
    {
        super();
        setMaxStackSize(1);
        setCreativeTab(AlchemicalWizardry.tabBloodMagic);
        setUnlocalizedName("energyBlaster");
        setFull3D();
        setMaxDamage(250);
        this.tier = tier;
        this.setEnergyUsed(this.tier == 1 ? AlchemicalWizardry.energyBlastLPPerShot : AlchemicalWizardry.energyBlastSecondTierLPPerShot);
        this.damage = this.tier == 1 ? AlchemicalWizardry.energyBlastDamage : AlchemicalWizardry.energyBlastSecondTierDamage;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister)
    {
        this.itemIcon = iconRegister.registerIcon("AlchemicalWizardry:EnergyBlaster_activated");
        this.activeIcon = iconRegister.registerIcon("AlchemicalWizardry:EnergyBlaster_activated");
        this.activeIconTier2 = iconRegister.registerIcon("AlchemicalWizardry:EnergyBlaster2_activated");
        this.passiveIcon = iconRegister.registerIcon("AlchemicalWizardry:SheathedItem");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int renderPass, EntityPlayer player, ItemStack usingItem, int useRemaining)
    {
        if (stack.getTagCompound() == null)
        {
            stack.setTagCompound(new NBTTagCompound());
        }

        NBTTagCompound tag = stack.getTagCompound();

        if (tag.getBoolean("isActive"))
        {
            return this.tier == 1 ? this.activeIcon : this.activeIconTier2;
        }
        else
        {
            return this.passiveIcon;
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
    {
        final int maxDelay = this.tier == 1 ? AlchemicalWizardry.energyBlastMaxDelay : AlchemicalWizardry.energyBlastSecondTierMaxDelay;

        if (!EnergyItems.checkAndSetItemOwner(par1ItemStack, par3EntityPlayer) || par3EntityPlayer.isSneaking())
        {
            this.setActivated(par1ItemStack, !getActivated(par1ItemStack));
            par1ItemStack.getTagCompound().setInteger("worldTimeDelay", (int) (par2World.getWorldTime() - 1) %
                                                                        (this.tier == 1 ?
                                                                         AlchemicalWizardry.energyBlastMaxDelayAfterActivation + 1 :
                                                                         AlchemicalWizardry.energyBlastSecondTierMaxDelayAfterActivation + 1));
            return par1ItemStack;
        }

        if (!getActivated(par1ItemStack))
        {
            return par1ItemStack;
        }

        if (this.getDelay(par1ItemStack) > 0)
        {
            return par1ItemStack;
        }

        if (!par3EntityPlayer.capabilities.isCreativeMode)
        {
            if(!syphonBatteries(par1ItemStack, par3EntityPlayer, this.getEnergyUsed()))
            {
            	return par1ItemStack;
            }
        }

        par2World.playSoundAtEntity(par3EntityPlayer, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

        if (!par2World.isRemote)
        {
            par2World.spawnEntityInWorld(new EnergyBlastProjectile(par2World, par3EntityPlayer, damage));
            this.setDelay(par1ItemStack, maxDelay);
        }

        return par1ItemStack;
    }

    @Override
    public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean par5)
    {
        if (!(par3Entity instanceof EntityPlayer))
        {
            return;
        }
        EntityPlayer par3EntityPlayer = (EntityPlayer) par3Entity;

        if (par1ItemStack.getTagCompound() == null)
        {
            par1ItemStack.setTagCompound(new NBTTagCompound());
        }
        int delay = this.getDelay(par1ItemStack);

        if (!par2World.isRemote && delay > 0)
        {
            this.setDelay(par1ItemStack, delay - 1);
        }

        if (par2World.getWorldTime() % (this.tier == 1 ?
                                        AlchemicalWizardry.energyBlastMaxDelayAfterActivation + 1 :
                                        AlchemicalWizardry.energyBlastSecondTierMaxDelayAfterActivation + 1) == par1ItemStack.getTagCompound().getInteger("worldTimeDelay") &&
            par1ItemStack.getTagCompound().getBoolean("isActive"))
        {
            if (!par3EntityPlayer.capabilities.isCreativeMode)
            {
                if(!EnergyItems.syphonBatteries(par1ItemStack, par3EntityPlayer, tier == 1 ? AlchemicalWizardry.energyBlastLPPerActivation : AlchemicalWizardry.energyBlastSecondTierLPPerActivation))
                {
                	this.setActivated(par1ItemStack, false);
                }
            }
        }

        par1ItemStack.setItemDamage(0);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4)
    {
        par3List.add(StatCollector.translateToLocal("tooltip.energyblast.desc1"));
        par3List.add(StatCollector.translateToLocal("tooltip.energyblast.desc2"));
        par3List.add(StatCollector.translateToLocal("tooltip.alchemy.damage") + " " + damage);

        if (!(par1ItemStack.getTagCompound() == null))
        {
            if (par1ItemStack.getTagCompound().getBoolean("isActive"))
            {
                par3List.add(StatCollector.translateToLocal("tooltip.sigil.state.activated"));
            } else
            {
                par3List.add(StatCollector.translateToLocal("tooltip.sigil.state.deactivated"));
            }

            if (!par1ItemStack.getTagCompound().getString("ownerName").equals(""))
            {
                par3List.add(StatCollector.translateToLocal("tooltip.owner.currentowner") + " " + par1ItemStack.getTagCompound().getString("ownerName"));
            }
        }
    }

    public void setActivated(ItemStack par1ItemStack, boolean newActivated)
    {
        if (par1ItemStack.getTagCompound() == null)
        {
            par1ItemStack.setTagCompound(new NBTTagCompound());
        }
        par1ItemStack.getTagCompound().setBoolean("isActive", newActivated);
    }

    public boolean getActivated(ItemStack par1ItemStack)
    {
        if (par1ItemStack.getTagCompound() == null)
        {
            par1ItemStack.setTagCompound(new NBTTagCompound());
        }
        return par1ItemStack.getTagCompound().getBoolean("isActive");
    }

    public void setDelay(ItemStack par1ItemStack, int newDelay)
    {
        if (par1ItemStack.getTagCompound() == null)
        {
            par1ItemStack.setTagCompound(new NBTTagCompound());
        }
        par1ItemStack.getTagCompound().setInteger("delay", newDelay);
    }

    public int getDelay(ItemStack par1ItemStack)
    {
        if (par1ItemStack.getTagCompound() == null)
        {
            par1ItemStack.setTagCompound(new NBTTagCompound());
        }
        return par1ItemStack.getTagCompound().getInteger("delay");
    }
}
