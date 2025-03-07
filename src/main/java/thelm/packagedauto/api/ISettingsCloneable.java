package thelm.packagedauto.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public interface ISettingsCloneable {

	String getConfigTypeName();

	boolean saveConfig(NBTTagCompound nbt, EntityPlayer player);

	boolean loadConfig(NBTTagCompound nbt, EntityPlayer player);
}
