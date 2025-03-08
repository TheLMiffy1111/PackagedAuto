package thelm.packagedauto.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public interface ISettingsCloneable {

	String getConfigTypeName();

	Result saveConfig(NBTTagCompound nbt, EntityPlayer player);

	Result loadConfig(NBTTagCompound nbt, EntityPlayer player);

	class Result {

		private static final ITextComponent EMPTY = new TextComponentString("");

		public final ResultType type;
		public final ITextComponent message;

		public Result(ResultType type, ITextComponent message) {
			this.type = type;
			this.message = message;
		}

		public static Result success() {
			return new Result(ResultType.SUCCESS, Result.EMPTY);
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
