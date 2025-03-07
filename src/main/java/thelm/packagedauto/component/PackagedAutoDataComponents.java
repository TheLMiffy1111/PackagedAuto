package thelm.packagedauto.component;

import java.util.List;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IVolumeStackWrapper;
import thelm.packagedauto.api.PatternType;
import thelm.packagedauto.api.SettingsClonerData;

public class PackagedAutoDataComponents {

	private PackagedAutoDataComponents() {}

	public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, "packagedauto");

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<IPackageRecipeInfo>> RECIPE = DATA_COMPONENTS.registerComponentType(
			"recipe", builder->builder.persistent(IPackageRecipeInfo.CODEC).networkSynchronized(IPackageRecipeInfo.STREAM_CODEC).cacheEncoding());
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<IPackageRecipeInfo>>> RECIPE_LIST = DATA_COMPONENTS.registerComponentType(
			"recipe_list", builder->builder.persistent(IPackageRecipeInfo.CODEC.listOf().orElse(List.of())).networkSynchronized(IPackageRecipeInfo.STREAM_CODEC.apply(ByteBufCodecs.list())).cacheEncoding());
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> PACKAGE_INDEX = DATA_COMPONENTS.registerComponentType(
			"package_index", builder->builder.persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT));
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<PatternType>> PATTERN_TYPE = DATA_COMPONENTS.registerComponentType(
			"pattern_type", builder->builder.persistent(PatternType.CODEC).networkSynchronized(PatternType.STREAM_CODEC));
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<DirectionalGlobalPos>> MARKER_POS = DATA_COMPONENTS.registerComponentType(
			"marker_pos", builder->builder.persistent(DirectionalGlobalPos.CODEC).networkSynchronized(DirectionalGlobalPos.STREAM_CODEC));
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<SettingsClonerData>> CLONER_DATA = DATA_COMPONENTS.registerComponentType(
			"cloner_data", builder->builder.persistent(SettingsClonerData.CODEC).networkSynchronized(SettingsClonerData.STREAM_CODEC).cacheEncoding());
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<IVolumeStackWrapper>> VOLUME_PACKAGE_STACK = DATA_COMPONENTS.registerComponentType(
			"volume_package_stack", builder->builder.persistent(IVolumeStackWrapper.CODEC).networkSynchronized(IVolumeStackWrapper.STREAM_CODEC).cacheEncoding());
}
