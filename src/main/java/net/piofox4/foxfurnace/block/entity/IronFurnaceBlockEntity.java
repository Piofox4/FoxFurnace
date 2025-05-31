package net.piofox4.foxfurnace.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.FurnaceScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.piofox4.foxfurnace.block.ModBlocks;

public class IronFurnaceBlockEntity extends AbstractFurnaceBlockEntity {

    public IronFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.IRON_FURNACE_ENTITY_TYPE, pos, state, RecipeType.SMELTING);
    }

    public static <T extends BlockEntity> void tick(World world, BlockPos blockPos, BlockState state, T t) {
        AbstractFurnaceBlockEntity.tick(world,blockPos,state,(IronFurnaceBlockEntity)t);
    }

    @Override
    public Text getContainerName() {
        return world != null ? world.getBlockState(this.pos).getBlock().getName() : null;
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new FurnaceScreenHandler(syncId,playerInventory,this,this.propertyDelegate);
    }
}
