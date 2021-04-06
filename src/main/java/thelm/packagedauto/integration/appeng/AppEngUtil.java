package thelm.packagedauto.integration.appeng;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import appeng.api.parts.IPartHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.helpers.IInterfaceHost;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;

public class AppEngUtil {

	private static final Comparator<IAEItemStack> COMPARE_BY_STACKSIZE = (s1, s2)->Long.compare(s1.getStackSize(), s2.getStackSize());

	private AppEngUtil() {}

	public static List<IAEItemStack> condenseStacks(IAEItemStack... stacks) {
		List<IAEItemStack> merged = Arrays.stream(stacks).filter(Objects::nonNull).
				collect(Collectors.toMap(Function.identity(), IAEItemStack::copy,
						(s1, s2)->s1.setStackSize(s1.getStackSize()+s2.getStackSize()))).
				values().stream().sorted(COMPARE_BY_STACKSIZE).collect(ImmutableList.toImmutableList());
		if(merged.isEmpty()) {
			throw new IllegalStateException("No pattern here!");
		}
		return merged;
	}

	public static boolean isInterface(TileEntity tile, Direction direction) {
		return tile instanceof IInterfaceHost || tile instanceof IPartHost && ((IPartHost)tile).getPart(direction) instanceof IInterfaceHost;
	}
}
