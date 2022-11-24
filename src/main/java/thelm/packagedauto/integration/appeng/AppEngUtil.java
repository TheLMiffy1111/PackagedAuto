package thelm.packagedauto.integration.appeng;

import java.util.LinkedHashMap;
import java.util.Map;

import appeng.api.parts.IPartHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.helpers.IInterfaceHost;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class AppEngUtil {

	private AppEngUtil() {}

	public static IAEItemStack[] condenseStacks(IAEItemStack... stacks) {
		Map<IAEItemStack, IAEItemStack> map = new LinkedHashMap<>();
		for(IAEItemStack stack : stacks) {
			if(stack == null) {
				continue;
			}
			IAEItemStack stored = map.get( stack );
			if(stored == null) {
				map.put(stack, stack.copy());
			}
			else {
				stored.add(stack);
			}
		}
		IAEItemStack[] ret = new IAEItemStack[map.size()];
		int i = 0;
		for(IAEItemStack stack : map.values()) {
			ret[i] = stack;
			++i;
		}
		return ret;
	}

	public static boolean isInterface(TileEntity tile, EnumFacing facing) {
		return tile instanceof IInterfaceHost || tile instanceof IPartHost && ((IPartHost)tile).getPart(facing) instanceof IInterfaceHost;
	}
}
