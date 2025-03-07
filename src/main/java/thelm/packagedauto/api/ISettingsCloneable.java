package thelm.packagedauto.api;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public interface ISettingsCloneable {

	String getConfigTypeName();

	boolean saveConfig(CompoundTag nbt, HolderLookup.Provider registries, Player player);

	boolean loadConfig(CompoundTag nbt, HolderLookup.Provider registries, Player player);
}
