package thelm.packagedauto.util;

import java.util.OptionalInt;

import net.minecraft.util.IIntArray;
import net.minecraft.util.IntReferenceHolder;

public abstract class OptionalIntReferenceHolder extends IntReferenceHolder {

	private OptionalInt prevValue = OptionalInt.empty();

	public static OptionalIntReferenceHolder of(IIntArray data, final int id) {
		return new OptionalIntReferenceHolder() {
			@Override
			public int get() {
				return data.get(id);
			}
			@Override
			public void set(int pValue) {
				data.set(id, pValue);
			}
		};
	}

	public static OptionalIntReferenceHolder of(int[] data, final int id) {
		return new OptionalIntReferenceHolder() {
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

	public static OptionalIntReferenceHolder of() {
		return new OptionalIntReferenceHolder() {
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

	@Override
	public boolean checkAndClearUpdateFlag() {
		int i = get();
		boolean flag = !prevValue.isPresent() || i != prevValue.getAsInt();
		prevValue = OptionalInt.of(i);
		return flag;
	}
}
