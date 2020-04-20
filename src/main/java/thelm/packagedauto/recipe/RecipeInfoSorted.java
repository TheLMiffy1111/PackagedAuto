package thelm.packagedauto.recipe;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IRecipeInfo;
import thelm.packagedauto.api.IRecipeType;
import thelm.packagedauto.api.MiscUtil;
import thelm.packagedauto.util.PatternSorted;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RecipeInfoSorted implements IRecipeInfo {
    List<ItemStack> input = new ArrayList<>();
    List<ItemStack> output = new ArrayList<>();
    List<IPackagePattern> patterns = new ArrayList<>();

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        MiscUtil.loadAllItems(nbt.getTagList("Input", 10), input);
        MiscUtil.loadAllItems(nbt.getTagList("Output", 10), output);
        patterns.clear();
        for(int i = 0; i*9 < input.size(); ++i) {
            patterns.add(new PatternSorted(this, i));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList inputTag = MiscUtil.saveAllItems(new NBTTagList(), input);
        nbt.setTag("Input", inputTag);
        NBTTagList outputTag = MiscUtil.saveAllItems(new NBTTagList(), output);
        nbt.setTag("Output", outputTag);
        return nbt;
    }

    @Override
    public IRecipeType getRecipeType() {
        return RecipeTypeSorted.INSTANCE;
    }

    @Override
    public boolean isValid() {
        return !input.isEmpty();
    }

    @Override
    public List<IPackagePattern> getPatterns() {
        return Collections.unmodifiableList(patterns);
    }

    @Override
    public List<ItemStack> getInputs() {
        return Collections.unmodifiableList(input);
    }

    @Override
    public List<ItemStack> getOutputs() {
        return Collections.unmodifiableList(output);
    }

    private static List<ItemStack> removeEmptyStacks(List<ItemStack> stacks) {
        return stacks.stream().filter(stack -> !stack.isEmpty()).collect(Collectors.toList());
    }

    @Override
    public void generateFromStacks(List<ItemStack> input, List<ItemStack> output, World world) {
        this.input.clear();
        this.input.addAll(removeEmptyStacks(input));
        this.output.clear();
        this.output.addAll(removeEmptyStacks(output));
        patterns.clear();
        for(int i = 0; i*9 < this.input.size(); ++i) {
            patterns.add(new PatternSorted(this, i));
        }
    }

    @Override
    public Int2ObjectMap<ItemStack> getEncoderStacks() {
        Int2ObjectMap<ItemStack> map = new Int2ObjectOpenHashMap<>();
        for(int i = 0; i < input.size(); ++i) {
            map.put(i, input.get(i));
        }
        for(int i = 0; i < output.size(); ++i) {
            map.put(i+81, output.get(i));
        }
        return map;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof RecipeInfoSorted) {
            RecipeInfoSorted other = (RecipeInfoSorted)obj;
            if(input.size() != other.input.size() || output.size() != other.output.size()) {
                return false;
            }
            for(int i = 0; i < input.size(); ++i) {
                if(!ItemStack.areItemStacksEqualUsingNBTShareTag(input.get(i), other.input.get(i))) {
                    return false;
                }
            }
            for(int i = 0; i < output.size(); ++i) {
                if(!ItemStack.areItemStacksEqualUsingNBTShareTag(output.get(i), other.output.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        Object[] toHash = new Object[2];
        Object[] inputArray = new Object[input.size()];
        for(int i = 0; i < input.size(); ++i) {
            ItemStack stack = input.get(i);
            inputArray[i] = new Object[] {stack.getItem(), stack.getItemDamage(), stack.getCount(), stack.getTagCompound()};
        }
        Object[] outputArray = new Object[output.size()];
        for(int i = 0; i < output.size(); ++i) {
            ItemStack stack = output.get(i);
            outputArray[i] = new Object[] {stack.getItem(), stack.getItemDamage(), stack.getCount(), stack.getTagCompound()};
        }
        toHash[0] = inputArray;
        toHash[1] = outputArray;
        return Arrays.deepHashCode(toHash);
    }
}
