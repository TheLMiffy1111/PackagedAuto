package thelm.packagedauto.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.block.entity.BaseBlockEntity;
import thelm.packagedauto.block.entity.CraftingProxyBlockEntity;

public class CraftingProxyBlock extends BaseBlock {

	public static final CraftingProxyBlock INSTANCE = new CraftingProxyBlock();
	public static final Item ITEM_INSTANCE = new BlockItem(INSTANCE, new Item.Properties().tab(PackagedAuto.CREATIVE_TAB)).setRegistryName("packagedauto:crafting_proxy");

	protected CraftingProxyBlock() {
		super(BlockBehaviour.Properties.of(Material.METAL).strength(15F, 25F).sound(SoundType.METAL));
		setRegistryName("packagedauto:crafting_proxy");
	}

	@Override
	public CraftingProxyBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return CraftingProxyBlockEntity.TYPE_INSTANCE.create(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
		return BaseBlockEntity::tick;
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		if(player.isShiftKeyDown()) {
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if(blockEntity instanceof CraftingProxyBlockEntity proxy) {
				if(!level.isClientSide) {
					proxy.sendPreview((ServerPlayer)player);
				}
				return InteractionResult.SUCCESS;
			}
		}
		return super.use(state, level, pos, player, hand, hitResult);
	}
}
