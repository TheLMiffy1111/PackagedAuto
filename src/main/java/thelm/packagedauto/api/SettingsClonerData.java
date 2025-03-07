package thelm.packagedauto.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class SettingsClonerData {

	private String type;
	private NBTTagCompound data;
	private int dimension;
	private BlockPos blockPos;

	public SettingsClonerData(String type, NBTTagCompound data, int dimension, BlockPos blockPos) {
		this.type = type;
		this.data = data;
		this.dimension = dimension;
		this.blockPos = blockPos;
	}

	public String type() {
		return type;
	}

	public NBTTagCompound data() {
		return data;
	}

	public int dimension() {
		return dimension;
	}

	public BlockPos blockPos() {
		return blockPos;
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
