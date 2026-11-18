package thelm.packagedauto.inventory;

import java.util.Arrays;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueInput.ValueInputList;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueOutput.ValueOutputList;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import thelm.packagedauto.api.IVolumeStackWrapper;
import thelm.packagedauto.api.IVolumeType;

public class BaseVolumeInventory implements ValueIOSerializable {

	public final IVolumeType type;
	public final IVolumeStackWrapper[] stacks;

	public BaseVolumeInventory(IVolumeType type, int size) {
		this.type = type;
		stacks = new IVolumeStackWrapper[size];
		Arrays.fill(stacks, type.getEmptyStackInstance());
	}

	public int size() {
		return stacks.length;
	}

	public boolean isEmpty() {
		for(IVolumeStackWrapper stack : stacks) {
			if(!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public IVolumeStackWrapper getStack(int index) {
		return index >= 0 && index < stacks.length ? stacks[index] : type.getEmptyStackInstance();
	}

	public void setStack(int index, IVolumeStackWrapper stack) {
		if(index >= 0 && index < stacks.length) {
			if(stack.isEmpty()) {
				stack = type.getEmptyStackInstance();
			}
			stacks[index] = stack;
		}
	}

	@Override
	public void serialize(ValueOutput output) {
		ValueOutputList list = output.childrenList("volumes");
		for(int i = 0; i < stacks.length; ++i) {
			IVolumeStackWrapper stack = stacks[i];
			if(!stack.isEmpty()) {
				ValueOutput child = list.addChild();
				child.putByte("slot", (byte)i);
				type.serializeStack(stack, child);
			}
		}
	}

	@Override
	public void deserialize(ValueInput input) {
		Arrays.fill(stacks, type.getEmptyStackInstance());
		ValueInputList list = input.childrenListOrEmpty("volumes");
		for(ValueInput child : list) {
			int i = child.getByteOr("slot", (byte)0) & 255;
			if(i >= 0 && i < stacks.length) {
				stacks[i] = type.deserializeStack(input);
			}
		}
	}
}
