package thelm.packagedauto.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public interface ISettingsCloneable {

	String getConfigTypeName();

	Result saveConfig(CompoundTag nbt, Player player);

	Result loadConfig(CompoundTag nbt, Player player);

	record Result(ResultType type, Component message) {

		public static Result success() {
			return new Result(ResultType.SUCCESS, Component.empty());
		}

		public static Result partial(Component message) {
			return new Result(ResultType.PARTIAL, message);
		}

		public static Result fail(Component message) {
			return new Result(ResultType.FAIL, message);
		}
	}

	enum ResultType {
		SUCCESS, PARTIAL, FAIL;
	}
}
