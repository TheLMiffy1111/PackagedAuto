package thelm.packagedauto.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public interface ISettingsCloneable {

	String getConfigTypeName();

	boolean saveConfig(CompoundTag nbt, Player player);

	boolean loadConfig(CompoundTag nbt, Player player);
}
