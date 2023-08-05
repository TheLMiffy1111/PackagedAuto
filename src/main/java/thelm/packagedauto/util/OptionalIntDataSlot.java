package thelm.packagedauto.util;

import java.util.OptionalInt;

import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.DataSlot;

public abstract class OptionalIntDataSlot extends DataSlot {

	private OptionalInt prevValue = OptionalInt.empty();

	public static OptionalIntDataSlot of(ContainerData data, final int id) {
		return new OptionalIntDataSlot() {
			@Override
			public int get() {
				return data.get(id);
			}
			@Override
			public void set(int value) {
				data.set(id, value);
			}
		};
	}

	public static OptionalIntDataSlot of(int[] data, final int id) {
		return new OptionalIntDataSlot() {
			@Override
			public int get() {
				return data[id];
			}
			@Override
			public void set(int value) {
				data[id] = value;
			}
		};
	}

	public static OptionalIntDataSlot of() {
		return new OptionalIntDataSlot() {
			private int value;
			@Override
			public int get() {
				return value;
			}
			@Override
			public void set(int value) {
				this.value = value;
			}
		};
	}

	public boolean checkAndClearUpdateFlag() {
		int i = get();
		boolean flag = prevValue.isEmpty() || i != prevValue.getAsInt();
		prevValue = OptionalInt.of(i);
		return flag;
	}
}
