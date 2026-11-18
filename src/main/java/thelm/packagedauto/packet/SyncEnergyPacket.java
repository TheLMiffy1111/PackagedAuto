package thelm.packagedauto.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import thelm.packagedauto.block.entity.BaseBlockEntity;

public record SyncEnergyPacket(BlockPos pos, int energy) implements CustomPacketPayload {

	public static final Type<SyncEnergyPacket> TYPE = new Type<>(Identifier.parse("packagedauto:sync_energy"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SyncEnergyPacket> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, SyncEnergyPacket::pos,
			ByteBufCodecs.INT, SyncEnergyPacket::energy,
			SyncEnergyPacket::new);

	@Override
	public Type<SyncEnergyPacket> type() {
		return TYPE;
	}

	public void handle(IPayloadContext ctx) {
		ctx.enqueueWork(()->{
			ClientLevel level = Minecraft.getInstance().level;
			if(level.isLoaded(pos)) {
				BlockEntity be = level.getBlockEntity(pos);
				if(be instanceof BaseBlockEntity bbe) {
					bbe.getEnergyStorage().setEnergyStored(energy);
				}
			}
		});
	}

	public static void syncEnergy(ServerLevel level, BlockPos pos, int energy, double range) {
		PacketDistributor.sendToPlayersNear(level, null, pos.getX()+0.5D, pos.getY()+0.5D, pos.getZ()+0.5D, range, new SyncEnergyPacket(pos, energy));
	}
}
