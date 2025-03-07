package thelm.packagedauto.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;

public interface ISettingsCloneable {

	String getConfigTypeName();

	boolean saveConfig(CompoundNBT nbt, PlayerEntity player);

	boolean loadConfig(CompoundNBT nbt, PlayerEntity player);
}
