package thelm.packagedauto.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import thelm.packagedauto.menu.EncoderMenu;

public record CycleRecipeTypePacket(boolean reverse) implements CustomPacketPayload {

	public static final ResourceLocation ID = new ResourceLocation("packagedauto:cycle_recipe_type");

	@Override
	public ResourceLocation id() {
		return ID;
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeBoolean(reverse);
	}

	public static CycleRecipeTypePacket read(FriendlyByteBuf buf) {
		return new CycleRecipeTypePacket(buf.readBoolean());
	}

	public void handle(PlayPayloadContext ctx) {
		if(ctx.player().orElse(null) instanceof ServerPlayer player) {
			ctx.workHandler().execute(()->{
				if(player.containerMenu instanceof EncoderMenu menu) {
					menu.patternItemHandler.cycleRecipeType(reverse);
					menu.setupSlots();
				}
			});
		}
	}
}
