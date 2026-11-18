package thelm.packagedauto.util;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueInput.ValueInputList;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueOutput.ValueOutputList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import thelm.packagedauto.api.IMiscHelper;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IVolumeStackWrapper;
import thelm.packagedauto.api.IVolumeType;
import thelm.packagedauto.component.PackagedAutoDataComponents;
import thelm.packagedauto.item.VolumePackageItem;

public class MiscHelper implements IMiscHelper {

	public static final MiscHelper INSTANCE = new MiscHelper();

	private static final Cache<CompoundTag, IPackageRecipeInfo> RECIPE_CACHE = CacheBuilder.newBuilder().maximumSize(500).build();
	private static final Logger LOGGER = LogManager.getLogger();

	private static MinecraftServer server;
	private static RecipeMap recipeMap;

	private MiscHelper() {}

	@Override
	public List<ItemStack> condenseStacks(Container container) {
		List<ItemStack> stacks = new ArrayList<>(container.getContainerSize());
		for(int i = 0; i < container.getContainerSize(); ++i) {
			stacks.add(container.getItem(i));
		}
		return condenseStacks(stacks);
	}

	@Override
	public List<ItemStack> condenseStacks(ResourceHandler<ItemResource> itemHandler) {
		List<ItemStack> stacks = new ArrayList<>(itemHandler.size());
		for(int i = 0; i < itemHandler.size(); ++i) {
			stacks.add(itemHandler.getResource(i).toStack(itemHandler.getAmountAsInt(i)));
		}
		return condenseStacks(stacks);
	}

	@Override
	public List<ItemStack> condenseStacks(ItemStack... stacks) {
		return condenseStacks(List.of(stacks));
	}

	@Override
	public List<ItemStack> condenseStacks(Stream<ItemStack> stacks) {
		return condenseStacks(stacks.toList());
	}

	@Override
	public List<ItemStack> condenseStacks(Iterable<ItemStack> stacks) {
		return condenseStacks(stacks instanceof List<?> ? (List<ItemStack>)stacks : Lists.newArrayList(stacks));
	}

	@Override
	public List<ItemStack> condenseStacks(List<ItemStack> stacks) {
		return condenseStacks(stacks, false);
	}

	@Override
	public List<ItemStack> condenseStacks(List<ItemStack> stacks, boolean ignoreStackSize) {
		Object2IntLinkedOpenHashMap<Pair<Item, DataComponentPatch>> map = new Object2IntLinkedOpenHashMap<>();
		for(ItemStack stack : stacks) {
			if(stack.isEmpty()) {
				continue;
			}
			Pair<Item, DataComponentPatch> pair = Pair.of(stack.getItem(), stack.getComponentsPatch());
			if(!map.containsKey(pair)) {
				map.put(pair, 0);
			}
			map.addTo(pair, stack.getCount());
		}
		List<ItemStack> list = new ArrayList<>();
		for(Object2IntMap.Entry<Pair<Item, DataComponentPatch>> entry : map.object2IntEntrySet()) {
			Pair<Item, DataComponentPatch> pair = entry.getKey();
			int count = entry.getIntValue();
			Item item = pair.getLeft();
			DataComponentPatch patch = pair.getRight();
			while(count > 0) {
				ItemStack toAdd = new ItemStack(item, 1);
				toAdd.applyComponents(patch);
				int limit = ignoreStackSize ? 1000000000 : Math.min(item.getMaxStackSize(toAdd), 1000000000);
				toAdd.setCount(Math.min(count, limit));
				list.add(toAdd);
				count -= limit;
			}
		}
		map.clear();
		return list;
	}

	@Override
	public void serializeItemList(List<ItemStack> list, ValueOutputList output) {
		serializeItemList(list, "index", output);
	}

	@Override
	public void serializeItemList(List<ItemStack> list, String indexKey, ValueOutputList output) {
		for(int i = 0; i < list.size(); ++i) {
			ItemStack stack = list.get(i);
			boolean empty = stack.isEmpty();
			if(!empty || i == list.size()-1) {
				ValueOutput child = output.addChild();
				child.putByte(indexKey, (byte)i);
				if(!empty) {
					child.store(LARGE_ITEM_MAP_CODEC, stack);
				}
			}
		}
	}

	@Override
	public void deserializeItemList(List<ItemStack> list, ValueInputList input) {
		deserializeItemList(list, "index", input);
	}

	@Override
	public void deserializeItemList(List<ItemStack> list, String indexKey, ValueInputList input) {
		list.clear();
		try {
			for(ValueInput child : input) {
				int i = child.getByteOr(indexKey, (byte)0) & 255;
				while(i >= list.size()) {
					list.add(ItemStack.EMPTY);
				}
				if(i >= 0)  {
					ItemStack stack = child.read(LARGE_ITEM_MAP_CODEC).orElse(ItemStack.EMPTY);
					list.set(i, stack.isEmpty() ? ItemStack.EMPTY : stack);
				}
			}
		}
		catch(UnsupportedOperationException | IndexOutOfBoundsException e) {}
	}

	public static final MapCodec<ItemStack> LARGE_ITEM_MAP_CODEC = RecordCodecBuilder.mapCodec(instance->instance.group(
			Item.CODEC_WITH_BOUND_COMPONENTS.fieldOf("item").forGetter(ItemStack::typeHolder),
			ExtraCodecs.POSITIVE_INT.fieldOf("count").orElse(1).forGetter(ItemStack::getCount),
			DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemStack::getComponentsPatch)).
			apply(instance, ItemStack::new));
	public static final Codec<ItemStack> LARGE_ITEM_CODEC = Codec.lazyInitialized(LARGE_ITEM_MAP_CODEC::codec);
	public static final Codec<ItemStack> LARGE_ITEM_OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(LARGE_ITEM_CODEC).
			xmap(itemStack->itemStack.orElse(ItemStack.EMPTY), itemStack->itemStack.isEmpty() ? Optional.empty() : Optional.of(itemStack));

	@Override
	public IPackagePattern getPattern(IPackageRecipeInfo recipeInfo, int index) {
		return new PackagePattern(recipeInfo, index);
	}

	@Override
	public List<ItemStack> getRemainingItems(Container container) {
		return getRemainingItems(IntStream.range(0, container.getContainerSize()).mapToObj(container::getItem).toList());
	}

	@Override
	public List<ItemStack> getRemainingItems(Container container, int minInclusive, int maxExclusive) {
		return getRemainingItems(IntStream.range(minInclusive, maxExclusive).mapToObj(container::getItem).toList());
	}

	@Override
	public List<ItemStack> getRemainingItems(ItemStack... stacks) {
		return getRemainingItems(List.of(stacks));
	}

	@Override
	public List<ItemStack> getRemainingItems(List<ItemStack> stacks) {
		NonNullList<ItemStack> ret = NonNullList.withSize(stacks.size(), ItemStack.EMPTY);
		for(int i = 0; i < ret.size(); i++) {
			ret.set(i, getContainerItem(stacks.get(i)));
		}
		return ret;
	}

	@Override
	public ItemStack getContainerItem(ItemStack stack) {
		if(stack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		if(stack.getCraftingRemainder() != null) {
			stack = stack.getCraftingRemainder().create();
			if(!stack.isEmpty() && stack.isDamageableItem() && stack.getDamageValue() > stack.getMaxDamage()) {
				return ItemStack.EMPTY;
			}
			return stack;
		}
		else {
			if(stack.getCount() > 1) {
				stack = stack.copy();
				stack.setCount(stack.getCount() - 1);
				return stack;
			}
			return ItemStack.EMPTY;
		}
	}

	@Override
	public ItemStack cloneStack(ItemStack stack, int stackSize) {
		if(stack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack retStack = stack.copy();
		retStack.setCount(stackSize);
		return retStack;
	}

	@Override
	public boolean isPackage(ItemStack stack) {
		return stack.has(PackagedAutoDataComponents.RECIPE) && stack.has(PackagedAutoDataComponents.PACKAGE_INDEX);
	}

	@Override
	public boolean isPackage(ItemResource resource) {
		return resource.has(PackagedAutoDataComponents.RECIPE) && resource.has(PackagedAutoDataComponents.PACKAGE_INDEX);
	}

	@Override
	public boolean isEmpty(ResourceHandler<ItemResource> itemHandler) {
		if(itemHandler == null || itemHandler.size() == 0) {
			return false;
		}
		for(int i = 0; i < itemHandler.size(); ++i) {
			if(itemHandler.getAmountAsLong(i) > 0) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack makeVolumePackage(IVolumeStackWrapper volumeStack) {
		return VolumePackageItem.makeVolumePackage(volumeStack);
	}

	@Override
	public ItemStack tryMakeVolumePackage(Object volumeStack) {
		return VolumePackageItem.tryMakeVolumePackage(volumeStack);
	}

	@Override
	public void serializeRecipeList(List<IPackageRecipeInfo> recipes, ValueOutputList output) {
		for(IPackageRecipeInfo recipe : recipes) {
			if(recipe != null) {
				output.addChild().store(IPackageRecipeInfo.MAP_CODEC, recipe);
			}
		}
	}

	@Override
	public List<IPackageRecipeInfo> deserializeRecipeList(ValueInputList input) {
		List<IPackageRecipeInfo> recipes = new ArrayList<>();
		for(ValueInput child : input) {
			child.read(IPackageRecipeInfo.MAP_CODEC).ifPresent(recipes::add);
		}
		return recipes;
	}

	@Override
	public boolean recipeEquals(IPackageRecipeInfo recipeA, Object recipeInternalA, IPackageRecipeInfo recipeB, Object recipeInternalB) {
		if(!Objects.equals(recipeInternalA, recipeInternalB)) {
			return false;
		}
		List<ItemStack> inputsA = recipeA.getInputs();
		List<ItemStack> inputsB = recipeB.getInputs();
		if(inputsA.size() != inputsB.size()) {
			return false;
		}
		List<ItemStack> outputsA = recipeA.getOutputs();
		List<ItemStack> outputsB = recipeB.getOutputs();
		if(outputsA.size() != outputsB.size()) {
			return false;
		}
		for(int i = 0; i < inputsA.size(); ++i) {
			if(!ItemStack.matches(inputsA.get(i), inputsB.get(i))) {
				return false;
			}
		}
		for(int i = 0; i < outputsA.size(); ++i) {
			if(!ItemStack.matches(outputsA.get(i), outputsB.get(i))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int recipeHashCode(IPackageRecipeInfo recipe, Object recipeInternal) {
		List<ItemStack> inputs = recipe.getInputs();
		List<ItemStack> outputs = recipe.getOutputs();
		Function<ItemStack, Object[]> decompose = stack->new Object[] {
				stack.getItem(), stack.getCount(), stack.getComponentsPatch(),
		};
		Object[] toHash = {
				recipeInternal, inputs.stream().map(decompose).toArray(), outputs.stream().map(decompose).toArray(),
		};
		return Arrays.deepHashCode(toHash);
	}

	//Modified from Forestry
	@Override
	public boolean removeExactSet(List<ItemStack> offered, List<ItemStack> required, boolean simulate) {
		List<ItemStack> condensedRequired = condenseStacks(required, true);
		List<ItemStack> condensedOffered = condenseStacks(offered, true);
		f:for(ItemStack req : condensedRequired) {
			for(ItemStack offer : condensedOffered) {
				if(req.getCount() <= offer.getCount() && req.is(offer.getItem()) &&
						(req.isComponentsPatchEmpty() || ItemStack.isSameItemSameComponents(req, offer))) {
					continue f;
				}
			}
			return false;
		}
		if(simulate) {
			return true;
		}
		for(ItemStack req : condensedRequired) {
			int count = req.getCount();
			for(ItemStack offer : offered) {
				if(!offer.isEmpty()) {
					if(req.is(offer.getItem()) && (req.isComponentsPatchEmpty() || ItemStack.isSameItemSameComponents(req, offer))) {
						int toRemove = Math.min(count, offer.getCount());
						offer.shrink(toRemove);
						count -= toRemove;
						if(count == 0) {
							continue;
						}
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean arePatternsDisjoint(List<IPackagePattern> patternList) {
		ObjectRBTreeSet<Pair<Item, DataComponentPatch>> set = new ObjectRBTreeSet<>(
				Comparator.comparing(pair->Pair.of(BuiltInRegistries.ITEM.getKey(pair.getLeft()), ""+pair.getRight())));
		for(IPackagePattern pattern : patternList) {
			List<ItemStack> condensedInputs = condenseStacks(pattern.getInputs(), true);
			for(ItemStack stack : condensedInputs) {
				Pair<Item, DataComponentPatch> toAdd = Pair.of(stack.getItem(), stack.getComponentsPatch());
				if(set.contains(toAdd)) {
					return false;
				}
				set.add(toAdd);
			}
		}
		set.clear();
		return true;
	}

	@Override
	public int insertItem(ResourceHandler<ItemResource> itemHandler, ItemStack stack, boolean requireEmptySlot, TransactionContext transaction) {
		if(itemHandler == null || stack.isEmpty()) {
			return 0;
		}
		ItemResource resource = ItemResource.of(stack);
		int count = stack.count();
		if(!requireEmptySlot) {
			return itemHandler.insert(resource, count, transaction);
		}
		int inserted = 0;
		for(int slot = 0; slot < itemHandler.size(); ++slot) {
			if(itemHandler.getAmountAsLong(slot) == 0) {
				inserted += itemHandler.insert(slot, resource, count - inserted, transaction);
				if(inserted >= count) {
					break;
				}
			}
		}
		return inserted;
	}

	@Override
	public int insertVolume(Level level, BlockPos pos, Direction direction, ItemStack stack, TransactionContext transaction) {
		if(stack.isEmpty()) {
			return 0;
		}
		if(stack.has(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK) &&
				stack.get(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK).getVolumeType().hasBlockCapability(level, pos, direction)) {
			int count = stack.count();
			int inserted = 0;
			IVolumeStackWrapper vStack = stack.get(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK);
			IVolumeType vType = vStack.getVolumeType();
			while(inserted < count) {
				Transaction subTransaction = Transaction.open(transaction);
				int subInserted = vType.insert(level, pos, direction, vStack, subTransaction);
				if(subInserted == vStack.getAmount()) {
					subTransaction.commit();
					++inserted;
				}
				else {
					subTransaction.close();
					break;
				}
			}
			return inserted;
		}
		return 0;
	}

	@Override
	public Runnable conditionalRunnable(BooleanSupplier conditionSupplier, Supplier<Runnable> trueRunnable, Supplier<Runnable> falseRunnable) {
		return ()->(conditionSupplier.getAsBoolean() ? trueRunnable : falseRunnable).get().run();
	}

	@Override
	public <T> Supplier<T> conditionalSupplier(BooleanSupplier conditionSupplier, Supplier<Supplier<T>> trueSupplier, Supplier<Supplier<T>> falseSupplier) {
		return ()->(conditionSupplier.getAsBoolean() ? trueSupplier : falseSupplier).get().get();
	}

	public void setServer(MinecraftServer server) {
		MiscHelper.server = server;
	}

	public void setRecipeMap(RecipeMap recipeMap) {
		MiscHelper.recipeMap = recipeMap;
	}

	@Override
	public RecipeMap getRecipeMap() {
		return server != null ? server.getRecipeManager().recipeMap() : recipeMap;
	}

	@Override
	public RegistryAccess getRegistryAccess() {
		return server != null ? server.registryAccess() :
			conditionalSupplier(FMLEnvironment.getDist()::isClient, ()->()->Minecraft.getInstance().level.registryAccess(), ()->()->null).get();
	}

	public static final Codec<byte[]> BYTE_ARRAY_CODEC = Codec.BYTE_BUFFER.xmap(ByteBuffer::array, ByteBuffer::wrap);
}
