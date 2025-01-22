package net.piofox4.foxfurnace.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.SharedConstants;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.*;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.FurnaceScreenHandler;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static net.minecraft.block.entity.AbstractFurnaceBlockEntity.DEFAULT_COOK_TIME;

public abstract class AbstractGenericFurnaceBlockEntity extends LockableContainerBlockEntity implements SidedInventory, RecipeUnlocker, RecipeInputProvider {
    private static final int[] TOP_SLOTS = new int[]{0};
    private static final int[] BOTTOM_SLOTS = new int[]{2, 1};
    private static final int[] SIDE_SLOTS = new int[]{1};
    private int minusCookTimeTotal;
    protected DefaultedList<ItemStack> inventory;
    int burnTime;
    int fuelTime;
    int cookTime;
    int cookTimeTotal;
    private boolean isUpdating = false;
    @Nullable
    protected final PropertyDelegate propertyDelegate;
    private static volatile Map<Item, Integer> fuelTimes;
    private final Object2IntOpenHashMap<Identifier> recipesUsed;
    private final RecipeManager.MatchGetter<SingleStackRecipeInput, ? extends AbstractCookingRecipe> matchGetter;

    @Override
    public Text getContainerName() {
        return Text.translatable("container.furnace");
    }
    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new FurnaceScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
    }

    protected AbstractGenericFurnaceBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, RecipeType<? extends AbstractCookingRecipe> recipeType, int minusCookTimeTotal) {
        super(blockEntityType, pos, state);
        this.minusCookTimeTotal = minusCookTimeTotal;
        this.inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);
        this.propertyDelegate = new PropertyDelegate() {
            public int get(int index) {
                return switch (index) {
                    case 0 -> AbstractGenericFurnaceBlockEntity.this.burnTime;
                    case 1 -> AbstractGenericFurnaceBlockEntity.this.fuelTime;
                    case 2 -> AbstractGenericFurnaceBlockEntity.this.cookTime;
                    case 3 -> AbstractGenericFurnaceBlockEntity.this.cookTimeTotal;
                    default -> 0;
                };
            }

            public void set(int index, int value) {
                switch (index) {
                    case 0 -> AbstractGenericFurnaceBlockEntity.this.burnTime = value;
                    case 1 -> AbstractGenericFurnaceBlockEntity.this.fuelTime = value;
                    case 2 -> AbstractGenericFurnaceBlockEntity.this.cookTime = value;
                    case 3 -> AbstractGenericFurnaceBlockEntity.this.cookTimeTotal = value;
                }

            }

            public int size() {
                return 4;
            }
        };
        this.recipesUsed = new Object2IntOpenHashMap<>();
        this.matchGetter = RecipeManager.createCachedMatchGetter(recipeType);
    }

    public static void clearFuelTimes() {
        fuelTimes = null;
    }

    public static Map<Item, Integer> createFuelTimeMap() {
        Map<Item, Integer> map = fuelTimes;
        if (map != null) {
            return map;
        } else {
            Map<Item, Integer> map2 = Maps.newLinkedHashMap();
            addFuel(map2, Items.LAVA_BUCKET, 20000);
            addFuel(map2, Blocks.COAL_BLOCK, 16000);
            addFuel(map2, Items.BLAZE_ROD, 2400);
            addFuel(map2, Items.COAL, 1600);
            addFuel(map2, Items.CHARCOAL, 1600);
            addFuel(map2, ItemTags.LOGS, 300);
            addFuel(map2, ItemTags.BAMBOO_BLOCKS, 300);
            addFuel(map2, ItemTags.PLANKS, 300);
            addFuel(map2, Blocks.BAMBOO_MOSAIC, 300);
            addFuel(map2, ItemTags.WOODEN_STAIRS, 300);
            addFuel(map2, Blocks.BAMBOO_MOSAIC_STAIRS, 300);
            addFuel(map2, ItemTags.WOODEN_SLABS, 150);
            addFuel(map2, Blocks.BAMBOO_MOSAIC_SLAB, 150);
            addFuel(map2, ItemTags.WOODEN_TRAPDOORS, 300);
            addFuel(map2, ItemTags.WOODEN_PRESSURE_PLATES, 300);
            addFuel(map2, ItemTags.WOODEN_FENCES, 300);
            addFuel(map2, ItemTags.FENCE_GATES, 300);
            addFuel(map2, Blocks.NOTE_BLOCK, 300);
            addFuel(map2, Blocks.BOOKSHELF, 300);
            addFuel(map2, Blocks.CHISELED_BOOKSHELF, 300);
            addFuel(map2, Blocks.LECTERN, 300);
            addFuel(map2, Blocks.JUKEBOX, 300);
            addFuel(map2, Blocks.CHEST, 300);
            addFuel(map2, Blocks.TRAPPED_CHEST, 300);
            addFuel(map2, Blocks.CRAFTING_TABLE, 300);
            addFuel(map2, Blocks.DAYLIGHT_DETECTOR, 300);
            addFuel(map2, ItemTags.BANNERS, 300);
            addFuel(map2, Items.BOW, 300);
            addFuel(map2, Items.FISHING_ROD, 300);
            addFuel(map2, Blocks.LADDER, 300);
            addFuel(map2, ItemTags.SIGNS, 200);
            addFuel(map2, ItemTags.HANGING_SIGNS, 800);
            addFuel(map2, Items.WOODEN_SHOVEL, 200);
            addFuel(map2, Items.WOODEN_SWORD, 200);
            addFuel(map2, Items.WOODEN_HOE, 200);
            addFuel(map2, Items.WOODEN_AXE, 200);
            addFuel(map2, Items.WOODEN_PICKAXE, 200);
            addFuel(map2, ItemTags.WOODEN_DOORS, 200);
            addFuel(map2, ItemTags.BOATS, 1200);
            addFuel(map2, ItemTags.WOOL, 100);
            addFuel(map2, ItemTags.WOODEN_BUTTONS, 100);
            addFuel(map2, Items.STICK, 100);
            addFuel(map2, ItemTags.SAPLINGS, 100);
            addFuel(map2, Items.BOWL, 100);
            addFuel(map2, ItemTags.WOOL_CARPETS, 67);
            addFuel(map2, Blocks.DRIED_KELP_BLOCK, 4001);
            addFuel(map2, Items.CROSSBOW, 300);
            addFuel(map2, Blocks.BAMBOO, 50);
            addFuel(map2, Blocks.DEAD_BUSH, 100);
            addFuel(map2, Blocks.SCAFFOLDING, 50);
            addFuel(map2, Blocks.LOOM, 300);
            addFuel(map2, Blocks.BARREL, 300);
            addFuel(map2, Blocks.CARTOGRAPHY_TABLE, 300);
            addFuel(map2, Blocks.FLETCHING_TABLE, 300);
            addFuel(map2, Blocks.SMITHING_TABLE, 300);
            addFuel(map2, Blocks.COMPOSTER, 300);
            addFuel(map2, Blocks.AZALEA, 100);
            addFuel(map2, Blocks.FLOWERING_AZALEA, 100);
            addFuel(map2, Blocks.MANGROVE_ROOTS, 300);
            fuelTimes = map2;
            return map2;
        }
    }

    @SuppressWarnings("deprecation")
    private static boolean isNonFlammableWood(Item item) {
        return item.getRegistryEntry().isIn(ItemTags.NON_FLAMMABLE_WOOD);
    }

    private static void addFuel(Map<Item, Integer> fuelTimes, TagKey<Item> tag, int fuelTime) {
        Iterator var3 = Registries.ITEM.iterateEntries(tag).iterator();

        while(var3.hasNext()) {
            RegistryEntry<Item> registryEntry = (RegistryEntry)var3.next();
            if (!isNonFlammableWood(registryEntry.value())) {
                fuelTimes.put(registryEntry.value(), fuelTime);
            }
        }

    }

    private static void addFuel(Map<Item, Integer> fuelTimes, ItemConvertible item, int fuelTime) {
        Item item2 = item.asItem();
        if (isNonFlammableWood(item2)) {
            if (SharedConstants.isDevelopment) {
                throw Util.throwOrPause(new IllegalStateException("A developer tried to explicitly make fire resistant item " + item2.getName((ItemStack)null).getString() + " a furnace fuel. That will not work!"));
            }
        } else {
            fuelTimes.put(item2, fuelTime);
        }
    }

    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        Inventories.readNbt(nbt, this.inventory, registryLookup);
        this.burnTime = nbt.getShort("BurnTime");
        this.cookTime = nbt.getShort("CookTime");
        this.cookTimeTotal = nbt.getShort("CookTimeTotal");
        this.fuelTime = this.getFuelTime(this.inventory.get(1));
        NbtCompound nbtCompound = nbt.getCompound("RecipesUsed");
        Iterator var4 = nbtCompound.getKeys().iterator();

        while(var4.hasNext()) {
            String string = (String)var4.next();
            this.recipesUsed.put(Identifier.of(string), nbtCompound.getInt(string));
        }

    }

    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putShort("BurnTime", (short)this.burnTime);
        nbt.putShort("CookTime", (short)this.cookTime);
        nbt.putShort("CookTimeTotal", (short)this.cookTimeTotal);
        Inventories.writeNbt(nbt, this.inventory, registryLookup);
        NbtCompound nbtCompound = new NbtCompound();
        this.recipesUsed.forEach((identifier, count) -> {
            nbtCompound.putInt(identifier.toString(), count);
        });
        nbt.put("RecipesUsed", nbtCompound);
    }

    private boolean isBurning() {
        return this.burnTime > 0;
    }

    protected abstract int getCurrentConfigValue();

    public static void tick(World world, BlockPos pos, BlockState state, AbstractGenericFurnaceBlockEntity blockEntity) {

        int newMinusCookTimeTotal = blockEntity.getCurrentConfigValue();

        if (blockEntity.minusCookTimeTotal != newMinusCookTimeTotal) {
            blockEntity.isUpdating = true;
            blockEntity.updateCookTime(newMinusCookTimeTotal);
            blockEntity.minusCookTimeTotal = newMinusCookTimeTotal;
            return;
        }

        if (blockEntity.isUpdating) {
            if (blockEntity.burnTime > 0) {
                blockEntity.burnTime += 1;
            }
            blockEntity.isUpdating = false;
        }

        boolean bl = blockEntity.isBurning();
        boolean bl2 = false;

        if (blockEntity.isBurning()) {
            --blockEntity.burnTime;
        }

        ItemStack itemStack = blockEntity.inventory.get(1);
        ItemStack itemStack2 = blockEntity.inventory.get(0);
        boolean bl3 = !itemStack2.isEmpty();
        boolean bl4 = !itemStack.isEmpty();
        if (blockEntity.isBurning() || bl4 && bl3) {
            RecipeEntry recipeEntry;
            if (bl3) {
                recipeEntry = blockEntity.matchGetter.getFirstMatch(new SingleStackRecipeInput(itemStack2), world).orElse(null);
            } else {
                recipeEntry = null;
            }

            int i = blockEntity.getMaxCountPerStack();
            if (!blockEntity.isBurning() && canAcceptRecipeOutput(world.getRegistryManager(), recipeEntry, blockEntity.inventory, i)) {
                blockEntity.burnTime = blockEntity.getFuelTime(itemStack);
                blockEntity.fuelTime = blockEntity.burnTime;
                if (blockEntity.isBurning()) {
                    bl2 = true;
                    if (bl4) {
                        Item item = itemStack.getItem();
                        itemStack.decrement(1);
                        if (itemStack.isEmpty()) {
                            Item item2 = item.getRecipeRemainder();
                            blockEntity.inventory.set(1, item2 == null ? ItemStack.EMPTY : new ItemStack(item2));
                        }
                    }
                }
            }

            if (blockEntity.isBurning() && canAcceptRecipeOutput(world.getRegistryManager(), recipeEntry, blockEntity.inventory, i)) {
                ++blockEntity.cookTime;
                if (blockEntity.cookTime == blockEntity.cookTimeTotal) {
                    blockEntity.cookTime = 0;
                    blockEntity.cookTimeTotal = getCookTime(world, blockEntity) - blockEntity.minusCookTimeTotal;
                    if (craftRecipe(world.getRegistryManager(), recipeEntry, blockEntity.inventory, i)) {
                        blockEntity.setLastRecipe(recipeEntry);
                    }

                    bl2 = true;
                }
            } else {
                blockEntity.cookTime = 0;
            }
        } else if (!blockEntity.isBurning() && blockEntity.cookTime > 0) {
            blockEntity.cookTime = MathHelper.clamp(blockEntity.cookTime - 2, 0, blockEntity.cookTimeTotal);
        }

        if (bl != blockEntity.isBurning()) {
            bl2 = true;
            state = state.with(AbstractFurnaceBlock.LIT, blockEntity.isBurning());
            world.setBlockState(pos, state, Block.NOTIFY_ALL);
        }

        if (bl2) {
            markDirty(world, pos, state);
        }

    }

    private static boolean canAcceptRecipeOutput(DynamicRegistryManager registryManager, @Nullable RecipeEntry<?> recipe, DefaultedList<ItemStack> slots, int count) {
        if (!slots.get(0).isEmpty() && recipe != null) {
            ItemStack itemStack = recipe.value().getResult(registryManager);
            if (itemStack.isEmpty()) {
                return false;
            } else {
                ItemStack itemStack2 = slots.get(2);
                if (itemStack2.isEmpty()) {
                    return true;
                } else if (!ItemStack.areItemsAndComponentsEqual(itemStack2, itemStack)) {
                    return false;
                } else if (itemStack2.getCount() < count && itemStack2.getCount() < itemStack2.getMaxCount()) {
                    return true;
                } else {
                    return itemStack2.getCount() < itemStack.getMaxCount();
                }
            }
        } else {
            return false;
        }
    }

    private static boolean craftRecipe(DynamicRegistryManager registryManager, @Nullable RecipeEntry<?> recipe, DefaultedList<ItemStack> slots, int count) {
        if (recipe != null && canAcceptRecipeOutput(registryManager, recipe, slots, count)) {
            ItemStack itemStack = slots.get(0);
            ItemStack itemStack2 = recipe.value().getResult(registryManager);
            ItemStack itemStack3 = slots.get(2);
            if (itemStack3.isEmpty()) {
                slots.set(2, itemStack2.copy());
            } else if (ItemStack.areItemsAndComponentsEqual(itemStack3, itemStack2)) {
                itemStack3.increment(1);
            }

            if (itemStack.isOf(Blocks.WET_SPONGE.asItem()) && !slots.get(1).isEmpty() && slots.get(1).isOf(Items.BUCKET)) {
                slots.set(1, new ItemStack(Items.WATER_BUCKET));
            }

            itemStack.decrement(1);
            return true;
        } else {
            return false;
        }
    }

    protected int getFuelTime(ItemStack fuel) {
        if (fuel.isEmpty()) {
            return 0;
        } else {
            Item item = fuel.getItem();
            int vanillaFuelTime = createFuelTimeMap().getOrDefault(item, 0);

            if (vanillaFuelTime == 0)
                return 0;

            return (int)(vanillaFuelTime * (((float) 200 - this.minusCookTimeTotal) / 200.0f));
        }
    }

    private static int getCookTime(World world, AbstractGenericFurnaceBlockEntity furnace) {
        SingleStackRecipeInput singleStackRecipeInput = new SingleStackRecipeInput(furnace.getStack(0));
        return furnace.matchGetter.getFirstMatch(singleStackRecipeInput, world).map((recipe) ->
                recipe.value().getCookingTime()).orElse(200);
    }

    public static boolean canUseAsFuel(ItemStack stack) {
        return createFuelTimeMap().containsKey(stack.getItem());
    }

    public int[] getAvailableSlots(Direction side) {
        if (side == Direction.DOWN) {
            return BOTTOM_SLOTS;
        } else {
            return side == Direction.UP ? TOP_SLOTS : SIDE_SLOTS;
        }
    }

    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return this.isValid(slot, stack);
    }

    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        if (dir == Direction.DOWN && slot == 1) {
            return stack.isOf(Items.WATER_BUCKET) || stack.isOf(Items.BUCKET);
        } else {
            return true;
        }
    }

    @Override
    public boolean isEmpty() {
        Iterator var1 = this.inventory.iterator();

        ItemStack itemStack;
        do {
            if (!var1.hasNext()) {
                return true;
            }

            itemStack = (ItemStack)var1.next();
        } while(itemStack.isEmpty());

        return false;
    }

    public int size() {
        return this.inventory.size();
    }

    protected DefaultedList<ItemStack> getHeldStacks() {
        return this.inventory;
    }

    protected void setHeldStacks(DefaultedList<ItemStack> inventory) {
        this.inventory = inventory;
    }

    public void setStack(int slot, ItemStack stack) {
        ItemStack itemStack = this.inventory.get(slot);
        boolean bl = !stack.isEmpty() && ItemStack.areItemsAndComponentsEqual(itemStack, stack);
        this.inventory.set(slot, stack);
        stack.capCount(this.getMaxCount(stack));
        if (slot == 0 && !bl) {
            this.cookTimeTotal = getCookTime(this.world, this) - this.minusCookTimeTotal;
            this.cookTime = 0;
            this.markDirty();
        }

    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        assert this.world != null;
        if (this.world.getBlockEntity(this.pos) != this) {
            return false;
        } else {
            return player.squaredDistanceTo((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
        }
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return Inventories.splitStack(this.inventory, slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(this.inventory, slot);
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        if (slot == 2) {
            return false;
        } else if (slot != 1) {
            return true;
        } else {
            ItemStack itemStack = this.inventory.get(1);
            return canUseAsFuel(stack) || stack.isOf(Items.BUCKET) && !itemStack.isOf(Items.BUCKET);
        }
    }

    public void setLastRecipe(@Nullable RecipeEntry<?> recipe) {
        if (recipe != null) {
            Identifier identifier = recipe.id();
            this.recipesUsed.addTo(identifier, 1);
        }

    }

    @Override
    public void clear() {
        this.inventory.clear();
    }

    @Override
    @Nullable
    public RecipeEntry<?> getLastRecipe() {
        return null;
    }

    @Override
    public void unlockLastRecipe(PlayerEntity player, List<ItemStack> ingredients) {
    }


    public void dropExperienceForRecipesUsed(ServerPlayerEntity player) {
        List<RecipeEntry<?>> list = this.getRecipesUsedAndDropExperience(player.getServerWorld(), player.getPos());
        player.unlockRecipes(list);

        for (RecipeEntry<?> recipeEntry : list) {
            if (recipeEntry != null) {
                player.onRecipeCrafted(recipeEntry, this.inventory);
            }
        }

        this.recipesUsed.clear();
    }

    public List<RecipeEntry<?>> getRecipesUsedAndDropExperience(ServerWorld world, Vec3d pos) {
        List<RecipeEntry<?>> list = Lists.newArrayList();
        ObjectIterator<Object2IntMap.Entry<Identifier>> var4 = this.recipesUsed.object2IntEntrySet().iterator();

        while(var4.hasNext()) {
            Object2IntMap.Entry<Identifier> entry = (Object2IntMap.Entry)var4.next();
            world.getRecipeManager().get(entry.getKey()).ifPresent((recipe) -> {
                list.add(recipe);
                dropExperience(world, pos, entry.getIntValue(), ((AbstractCookingRecipe)recipe.value()).getExperience());
            });
        }

        return list;
    }

    private static void dropExperience(ServerWorld world, Vec3d pos, int multiplier, float experience) {
        int i = MathHelper.floor((float)multiplier * experience);
        float f = MathHelper.fractionalPart((float)multiplier * experience);
        if (f != 0.0F && Math.random() < (double)f) {
            ++i;
        }

        ExperienceOrbEntity.spawn(world, pos, i);
    }

    @Override
    public void provideRecipeInputs(RecipeMatcher finder) {

        for (ItemStack itemStack : this.inventory) {
            finder.addInput(itemStack);
        }
    }

    public void updateCookTime(int newMinusCookTime) {

        float cookProgressPercentage = 0;
        if (this.cookTimeTotal > 0) {
            cookProgressPercentage = (float)this.cookTime / this.cookTimeTotal;
        }


        float fuelRemainingPercentage = 0;
        if (this.fuelTime > 0) {
            fuelRemainingPercentage = (float)this.burnTime / this.fuelTime;
        }


        float speedRatio = (float)(DEFAULT_COOK_TIME - newMinusCookTime) / this.cookTimeTotal;


        this.cookTimeTotal = DEFAULT_COOK_TIME - newMinusCookTime;


        this.cookTime = Math.round(cookProgressPercentage * this.cookTimeTotal);

        if (this.fuelTime > 0) {
            this.fuelTime = Math.round(this.fuelTime * speedRatio);
            this.burnTime = Math.round(this.fuelTime * fuelRemainingPercentage);
        }

        markDirty();
    }
}