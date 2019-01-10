package thelm.packagedauto.integration.appeng;

import java.util.LinkedHashMap;
import java.util.Map;

import appeng.api.storage.data.IAEItemStack;

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
}
