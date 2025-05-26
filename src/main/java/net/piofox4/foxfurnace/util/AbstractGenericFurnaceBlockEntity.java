package net.piofox4.foxfurnace.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.fabricmc.fabric.api.registry.FuelRegistry;
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

    // Cached fuel time map - uses volatile for thread safety
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

    /**
     * Clears the cached fuel times map, forcing it to be rebuilt on next access
     */
    public static void clearFuelTimes() {
        fuelTimes = null;
    }

    /**
     * Creates a comprehensive fuel time map that includes all registered fuels
     * This method is completely generic and automatically detects:
     * - All vanilla Minecraft fuels
     * - All mod-added fuels registered through Fabric's FuelRegistry
     *
     * @return Map containing all fuel items and their burn times in ticks
     */
    public static Map<Item, Integer> createFuelTimeMap() {
        Map<Item, Integer> map = fuelTimes;
        if (map != null) {
            return map;
        } else {
            Map<Item, Integer> map2 = Maps.newLinkedHashMap();

            // Load all registered fuels (vanilla + mods) from Fabric's FuelRegistry
            // This is completely generic and works with any registered fuel
            addAllRegisteredFuels(map2);

            fuelTimes = map2;
            return map2;
        }
    }

    /**
     * Adds all fuels registered in Fabric's FuelRegistry to the provided map
     * This includes both vanilla fuels and any fuels added by mods
     *
     * @param fuelMap The map to populate with fuel items and their burn times
     */
    private static void addAllRegisteredFuels(Map<Item, Integer> fuelMap) {
        try {
            // Iterate through all items in the item registry
            for (Item item : Registries.ITEM) {
                // Check if the item is registered as fuel in FuelRegistry
                Integer fuelTime = FuelRegistry.INSTANCE.get(item);
                if (fuelTime != null && fuelTime > 0) {
                    // Respect the non-flammable wood rule
                    if (!isNonFlammableWood(item)) {
                        fuelMap.put(item, fuelTime);
                    }
                }
            }
        } catch (Exception e) {
            // Fallback to essential vanilla fuels in case of error
            addVanillaFuelsFallback(fuelMap);
        }
    }

    /**
     * Fallback method to add essential vanilla fuels in case FuelRegistry access fails
     * This ensures the furnace can still function with basic fuels
     *
     * @param fuelMap The map to populate with essential fuel items
     */
    private static void addVanillaFuelsFallback(Map<Item, Integer> fuelMap) {
        // Only the most essential fuels as fallback
        addFuel(fuelMap, Items.LAVA_BUCKET, 20000);
        addFuel(fuelMap, Blocks.COAL_BLOCK, 16000);
        addFuel(fuelMap, Items.BLAZE_ROD, 2400);
        addFuel(fuelMap, Items.COAL, 1600);
        addFuel(fuelMap, Items.CHARCOAL, 1600);
        addFuel(fuelMap, ItemTags.LOGS, 300);
        addFuel(fuelMap, ItemTags.PLANKS, 300);
        addFuel(fuelMap, Items.STICK, 100);
    }

    /**
     * Checks if an item is non-flammable wood (like crimson/warped wood)
     *
     * @param item The item to check
     * @return true if the item is non-flammable wood, false otherwise
     */
    @SuppressWarnings("deprecation")
    private static boolean isNonFlammableWood(Item item) {
        return item.getRegistryEntry().isIn(ItemTags.NON_FLAMMABLE_WOOD);
    }

    /**
     * Adds fuel items from a tag to the fuel map
     *
     * @param fuelTimes The fuel map to populate
     * @param tag The item tag containing fuel items
     * @param fuelTime The burn time for items in this tag
     */
    private static void addFuel(Map<Item, Integer> fuelTimes, TagKey<Item> tag, int fuelTime) {
        Iterator<RegistryEntry<Item>> var3 = Registries.ITEM.iterateEntries(tag).iterator();

        while(var3.hasNext()) {
            RegistryEntry<Item> registryEntry = var3.next();
            if (!isNonFlammableWood(registryEntry.value())) {
                fuelTimes.put(registryEntry.value(), fuelTime);
            }
        }
    }

    /**
     * Adds a specific fuel item to the fuel map
     *
     * @param fuelTimes The fuel map to populate
     * @param item The item to add as fuel
     * @param fuelTime The burn time for this item
     */
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
        Iterator<String> var4 = nbtCompound.getKeys().iterator();

        while(var4.hasNext()) {
            String string = var4.next();
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

    /**
     * Checks if the furnace is currently burning fuel
     *
     * @return true if burn time is greater than 0
     */
    private boolean isBurning() {
        return this.burnTime > 0;
    }

    /**
     * Abstract method to get the current configuration value for cook time reduction
     * This should be implemented by subclasses to provide their specific config value
     *
     * @return The amount to subtract from default cook time
     */
    protected abstract int getCurrentConfigValue();

    /**
     * Main tick method that handles furnace logic
     * This method manages fuel consumption, cooking progress, and config updates
     *
     * @param world The world the furnace is in
     * @param pos The position of the furnace
     * @param state The block state of the furnace
     * @param blockEntity The furnace block entity instance
     */
    public static void tick(World world, BlockPos pos, BlockState state, AbstractGenericFurnaceBlockEntity blockEntity) {
        int newMinusCookTimeTotal = blockEntity.getCurrentConfigValue();

        // Check if config has changed and update accordingly
        if (blockEntity.minusCookTimeTotal != newMinusCookTimeTotal) {
            blockEntity.isUpdating = true;
            blockEntity.updateCookTime(newMinusCookTimeTotal);
            blockEntity.minusCookTimeTotal = newMinusCookTimeTotal;
            return;
        }

        // Handle post-update tick adjustment
        if (blockEntity.isUpdating) {
            if (blockEntity.burnTime > 0) {
                blockEntity.burnTime += 1;
            }
            blockEntity.isUpdating = false;
        }

        boolean wasLit = blockEntity.isBurning();
        boolean dirty = false;

        // Consume fuel
        if (blockEntity.isBurning()) {
            --blockEntity.burnTime;
        }

        ItemStack fuelStack = blockEntity.inventory.get(1);
        ItemStack inputStack = blockEntity.inventory.get(0);
        boolean hasInput = !inputStack.isEmpty();
        boolean hasFuel = !fuelStack.isEmpty();

        if (blockEntity.isBurning() || hasFuel && hasInput) {
            RecipeEntry recipeEntry;
            if (hasInput) {
                recipeEntry = blockEntity.matchGetter.getFirstMatch(new SingleStackRecipeInput(inputStack), world).orElse(null);
            } else {
                recipeEntry = null;
            }

            int maxStackSize = blockEntity.getMaxCountPerStack();

            // Try to start burning if not already burning and can accept recipe output
            if (!blockEntity.isBurning() && canAcceptRecipeOutput(world.getRegistryManager(), recipeEntry, blockEntity.inventory, maxStackSize)) {
                blockEntity.burnTime = blockEntity.getFuelTime(fuelStack);
                blockEntity.fuelTime = blockEntity.burnTime;
                if (blockEntity.isBurning()) {
                    dirty = true;
                    if (hasFuel) {
                        Item fuelItem = fuelStack.getItem();
                        fuelStack.decrement(1);
                        if (fuelStack.isEmpty()) {
                            Item remainder = fuelItem.getRecipeRemainder();
                            blockEntity.inventory.set(1, remainder == null ? ItemStack.EMPTY : new ItemStack(remainder));
                        }
                    }
                }
            }

            // Cook the item if burning and can accept output
            if (blockEntity.isBurning() && canAcceptRecipeOutput(world.getRegistryManager(), recipeEntry, blockEntity.inventory, maxStackSize)) {
                ++blockEntity.cookTime;
                if (blockEntity.cookTime == blockEntity.cookTimeTotal) {
                    blockEntity.cookTime = 0;
                    blockEntity.cookTimeTotal = getCookTime(world, blockEntity) - blockEntity.minusCookTimeTotal;
                    if (craftRecipe(world.getRegistryManager(), recipeEntry, blockEntity.inventory, maxStackSize)) {
                        blockEntity.setLastRecipe(recipeEntry);
                    }
                    dirty = true;
                }
            } else {
                blockEntity.cookTime = 0;
            }
        } else if (!blockEntity.isBurning() && blockEntity.cookTime > 0) {
            // Slowly decrease cook time when not burning
            blockEntity.cookTime = MathHelper.clamp(blockEntity.cookTime - 2, 0, blockEntity.cookTimeTotal);
        }

        // Update block state if lit status changed
        if (wasLit != blockEntity.isBurning()) {
            dirty = true;
            state = state.with(AbstractFurnaceBlock.LIT, blockEntity.isBurning());
            world.setBlockState(pos, state, Block.NOTIFY_ALL);
        }

        if (dirty) {
            markDirty(world, pos, state);
        }
    }

    /**
     * Checks if the furnace can accept the output of a recipe
     *
     * @param registryManager The dynamic registry manager
     * @param recipe The recipe to check
     * @param slots The inventory slots
     * @param count The max count per stack
     * @return true if the recipe output can be accepted
     */
    private static boolean canAcceptRecipeOutput(DynamicRegistryManager registryManager, @Nullable RecipeEntry<?> recipe, DefaultedList<ItemStack> slots, int count) {
        if (!slots.get(0).isEmpty() && recipe != null) {
            ItemStack recipeOutput = recipe.value().getResult(registryManager);
            if (recipeOutput.isEmpty()) {
                return false;
            } else {
                ItemStack outputSlot = slots.get(2);
                if (outputSlot.isEmpty()) {
                    return true;
                } else if (!ItemStack.areItemsAndComponentsEqual(outputSlot, recipeOutput)) {
                    return false;
                } else if (outputSlot.getCount() < count && outputSlot.getCount() < outputSlot.getMaxCount()) {
                    return true;
                } else {
                    return outputSlot.getCount() < recipeOutput.getMaxCount();
                }
            }
        } else {
            return false;
        }
    }

    /**
     * Crafts a recipe and updates the inventory
     *
     * @param registryManager The dynamic registry manager
     * @param recipe The recipe to craft
     * @param slots The inventory slots
     * @param count The max count per stack
     * @return true if the recipe was successfully crafted
     */
    private static boolean craftRecipe(DynamicRegistryManager registryManager, @Nullable RecipeEntry<?> recipe, DefaultedList<ItemStack> slots, int count) {
        if (recipe != null && canAcceptRecipeOutput(registryManager, recipe, slots, count)) {
            ItemStack inputStack = slots.get(0);
            ItemStack recipeOutput = recipe.value().getResult(registryManager);
            ItemStack outputStack = slots.get(2);

            if (outputStack.isEmpty()) {
                slots.set(2, recipeOutput.copy());
            } else if (ItemStack.areItemsAndComponentsEqual(outputStack, recipeOutput)) {
                outputStack.increment(1);
            }

            // Special case for wet sponge + bucket = water bucket
            if (inputStack.isOf(Blocks.WET_SPONGE.asItem()) && !slots.get(1).isEmpty() && slots.get(1).isOf(Items.BUCKET)) {
                slots.set(1, new ItemStack(Items.WATER_BUCKET));
            }

            inputStack.decrement(1);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets the fuel time for an item stack, adjusted by furnace speed
     *
     * @param fuel The fuel item stack
     * @return The adjusted burn time in ticks
     */
    protected int getFuelTime(ItemStack fuel) {
        if (fuel.isEmpty()) {
            return 0;
        } else {
            Item item = fuel.getItem();
            int vanillaFuelTime = createFuelTimeMap().getOrDefault(item, 0);

            if (vanillaFuelTime == 0)
                return 0;

            // Adjust fuel time based on furnace speed configuration
            return (int)(vanillaFuelTime * (((float) 200 - this.minusCookTimeTotal) / 200.0f));
        }
    }

    /**
     * Gets the cook time for the current recipe
     *
     * @param world The world
     * @param furnace The furnace instance
     * @return The cook time in ticks
     */
    private static int getCookTime(World world, AbstractGenericFurnaceBlockEntity furnace) {
        SingleStackRecipeInput singleStackRecipeInput = new SingleStackRecipeInput(furnace.getStack(0));
        return furnace.matchGetter.getFirstMatch(singleStackRecipeInput, world).map((recipe) ->
                recipe.value().getCookingTime()).orElse(200);
    }

    /**
     * Checks if an item stack can be used as fuel
     *
     * @param stack The item stack to check
     * @return true if the stack can be used as fuel
     */
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
        for (ItemStack itemStack : this.inventory) {
            if (!itemStack.isEmpty()) {
                return false;
            }
        }
        return true;
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
        ItemStack currentStack = this.inventory.get(slot);
        boolean stacksMatch = !stack.isEmpty() && ItemStack.areItemsAndComponentsEqual(currentStack, stack);
        this.inventory.set(slot, stack);
        stack.capCount(this.getMaxCount(stack));
        if (slot == 0 && !stacksMatch) {
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
            return false; // Output slot - no insertion allowed
        } else if (slot != 1) {
            return true; // Input slot - accept any item
        } else {
            // Fuel slot - only accept fuel items or buckets
            ItemStack currentFuel = this.inventory.get(1);
            return canUseAsFuel(stack) || stack.isOf(Items.BUCKET) && !currentFuel.isOf(Items.BUCKET);
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

    /**
     * Drops experience orbs for recipes used and unlocks recipes for the player
     *
     * @param player The player to grant experience and unlock recipes for
     */
    public void dropExperienceForRecipesUsed(ServerPlayerEntity player) {
        List<RecipeEntry<?>> recipes = this.getRecipesUsedAndDropExperience(player.getServerWorld(), player.getPos());
        player.unlockRecipes(recipes);

        for (RecipeEntry<?> recipeEntry : recipes) {
            if (recipeEntry != null) {
                player.onRecipeCrafted(recipeEntry, this.inventory);
            }
        }

        this.recipesUsed.clear();
    }

    /**
     * Gets all recipes used and drops experience orbs at the specified position
     *
     * @param world The server world
     * @param pos The position to drop experience orbs
     * @return List of recipe entries that were used
     */
    public List<RecipeEntry<?>> getRecipesUsedAndDropExperience(ServerWorld world, Vec3d pos) {
        List<RecipeEntry<?>> recipes = Lists.newArrayList();
        ObjectIterator<Object2IntMap.Entry<Identifier>> iterator = this.recipesUsed.object2IntEntrySet().iterator();

        while(iterator.hasNext()) {
            Object2IntMap.Entry<Identifier> entry = iterator.next();
            world.getRecipeManager().get(entry.getKey()).ifPresent((recipe) -> {
                recipes.add(recipe);
                dropExperience(world, pos, entry.getIntValue(), ((AbstractCookingRecipe)recipe.value()).getExperience());
            });
        }

        return recipes;
    }

    /**
     * Drops experience orbs at the specified position
     *
     * @param world The server world
     * @param pos The position to drop experience
     * @param multiplier The number of times the recipe was used
     * @param experience The experience per recipe use
     */
    private static void dropExperience(ServerWorld world, Vec3d pos, int multiplier, float experience) {
        int totalExperience = MathHelper.floor((float)multiplier * experience);
        float fractionalPart = MathHelper.fractionalPart((float)multiplier * experience);
        if (fractionalPart != 0.0F && Math.random() < (double)fractionalPart) {
            ++totalExperience;
        }

        ExperienceOrbEntity.spawn(world, pos, totalExperience);
    }

    @Override
    public void provideRecipeInputs(RecipeMatcher finder) {
        for (ItemStack itemStack : this.inventory) {
            finder.addInput(itemStack);
        }
    }

    /**
     * Updates cook times when furnace speed configuration changes
     * This method preserves cooking progress and fuel remaining percentages
     *
     * @param newMinusCookTime The new cook time reduction value
     */
    public void updateCookTime(int newMinusCookTime) {
        // Calculate current cooking progress percentage
        float cookProgressPercentage = 0;
        if (this.cookTimeTotal > 0) {
            cookProgressPercentage = (float)this.cookTime / this.cookTimeTotal;
        }

        // Calculate remaining fuel percentage
        float fuelRemainingPercentage = 0;
        if (this.fuelTime > 0) {
            fuelRemainingPercentage = (float)this.burnTime / this.fuelTime;
        }

        // Calculate speed ratio for fuel adjustment
        float speedRatio = (float)(DEFAULT_COOK_TIME - newMinusCookTime) / this.cookTimeTotal;

        // Update cook time total
        this.cookTimeTotal = DEFAULT_COOK_TIME - newMinusCookTime;

        // Preserve cooking progress
        this.cookTime = Math.round(cookProgressPercentage * this.cookTimeTotal);

        // Adjust fuel times to maintain balance
        if (this.fuelTime > 0) {
            this.fuelTime = Math.round(this.fuelTime * speedRatio);
            this.burnTime = Math.round(this.fuelTime * fuelRemainingPercentage);
        }

        markDirty();
    }
}