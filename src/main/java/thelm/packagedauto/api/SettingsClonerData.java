package thelm.packagedauto.api;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

public class SettingsClonerData {

	private String type;
	private CompoundNBT data;
	private GlobalPos globalPos;

	public SettingsClonerData(String type, CompoundNBT data, GlobalPos globalPos) {
		this.type = type;
		this.data = data;
		this.globalPos = globalPos;
	}

	public SettingsClonerData(String type, CompoundNBT data, RegistryKey<World> dimension, BlockPos blockPos) {
		this(type, data, GlobalPos.of(dimension, blockPos));
	}

	public String type() {
		return type;
	}

	public CompoundNBT data() {
		return data;
	}

	public GlobalPos globalPos() {
		return globalPos;
	}

	public RegistryKey<World> dimension() {
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
