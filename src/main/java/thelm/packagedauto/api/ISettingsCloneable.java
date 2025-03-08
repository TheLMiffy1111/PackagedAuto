package thelm.packagedauto.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public interface ISettingsCloneable {

	String getConfigTypeName();

	Result saveConfig(CompoundNBT nbt, PlayerEntity player);

	Result loadConfig(CompoundNBT nbt, PlayerEntity player);

	class Result {

		public final ResultType type;
		public final ITextComponent message;

		public Result(ResultType type, ITextComponent message) {
			this.type = type;
			this.message = message;
		}

		public static Result success() {
			return new Result(ResultType.SUCCESS, StringTextComponent.EMPTY);
		}

		public static Result partial(ITextComponent message) {
			return new Result(ResultType.PARTIAL, message);
		}

		public static Result fail(ITextComponent message) {
			return new Result(ResultType.FAIL, message);
		}
	}

	enum ResultType {
		SUCCESS, PARTIAL, FAIL;
	}
}
