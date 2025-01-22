package net.piofox4.foxfurnace.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.recipe.RecipeType;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.piofox4.foxfurnace.block.ModBlocks;
import net.piofox4.foxfurnace.util.AbstractGenericFurnaceBlockEntity;
import net.piofox4.foxfurnace.util.Ref;

public class NetheriteFurnaceBlockEntity extends AbstractGenericFurnaceBlockEntity {

    public NetheriteFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.NETHERITE_FURNACE_ENTITY_TYPE, pos, state, RecipeType.SMELTING,Ref.minusTotalCookTimeNetherite);
    }

    public static <T extends BlockEntity> void tick(World world, BlockPos blockPos, BlockState state, T t) {
        AbstractGenericFurnaceBlockEntity.tick(world,blockPos,state,(NetheriteFurnaceBlockEntity)t);
    }

    @Override
    public Text getContainerName() {
        return world != null ? world.getBlockState(this.pos).getBlock().getName() : null;
    }

    @Override
    protected int getCurrentConfigValue() {
        return Ref.minusTotalCookTimeNetherite;
    }
}
