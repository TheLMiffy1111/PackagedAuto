package thelm.packagedauto.api;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public interface ISettingsCloneable {

	String getConfigTypeName();

	Result saveConfig(ValueOutput output, Player player);

	Result loadConfig(ValueInput input, Player player);

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
