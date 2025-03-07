package thelm.packagedauto.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record SettingsClonerData(String type, CompoundTag data, GlobalPos globalPos) {

	public SettingsClonerData(String type, CompoundTag data, ResourceKey<Level> dimension, BlockPos blockPos) {
		this(type, data, GlobalPos.of(dimension, blockPos));
	}

	public String type() {
		return type;
	}

	public CompoundTag data() {
		return data;
	}

	public GlobalPos globalPos() {
		return globalPos;
	}

	public ResourceKey<Level> dimension() {
		return globalPos.dimension();
	}

	public BlockPos blockPos() {
		return globalPos.pos();
	}

	public int x() {
		return blockPos().getX();
	}

	public int y() {
		return blockPos().getY();
	}

	public int z() {
		return blockPos().getZ();
	}
}
