package thelm.packagedauto.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor.TargetPoint;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import thelm.packagedauto.block.entity.BaseBlockEntity;

public record SyncEnergyPacket(BlockPos pos, int energy) implements CustomPacketPayload {

	public static final ResourceLocation ID = new ResourceLocation("packagedauto:sync_energy");

	@Override
	public ResourceLocation id() {
		return ID;
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeBlockPos(pos);
		buf.writeInt(energy);
	}

	public static SyncEnergyPacket read(FriendlyByteBuf buf) {
		return new SyncEnergyPacket(buf.readBlockPos(), buf.readInt());
	}

	public void handle(PlayPayloadContext ctx) {
		ctx.workHandler().execute(()->{
			ClientLevel level = Minecraft.getInstance().level;
			if(level.isLoaded(pos)) {
				BlockEntity be = level.getBlockEntity(pos);
				if(be instanceof BaseBlockEntity bbe) {
					bbe.getEnergyStorage().setEnergyStored(energy);
				}
			}
		});
	}

	public static void syncEnergy(BlockPos pos, int energy, ResourceKey<Level> dimension, double range) {
		PacketDistributor.NEAR.
		with(new TargetPoint(pos.getX()+0.5D, pos.getY()+0.5D, pos.getZ()+0.5D, range, dimension)).
		send(new SyncEnergyPacket(pos, energy));
	}
}
