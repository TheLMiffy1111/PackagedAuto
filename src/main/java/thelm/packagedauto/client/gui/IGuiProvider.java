package thelm.packagedauto.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public interface IGuiProvider {

	@SideOnly(Side.CLIENT)
	GuiContainer getClientGuiElement(EntityPlayer player, Object... args);

	Container getServerGuiElement(EntityPlayer player, Object... args);
}
