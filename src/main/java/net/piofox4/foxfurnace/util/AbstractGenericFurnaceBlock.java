package net.piofox4.foxfurnace.util;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public abstract class AbstractGenericFurnaceBlock extends BlockWithEntity {

    public static final DirectionProperty FACING;
    public static final BooleanProperty LIT;

    protected AbstractGenericFurnaceBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(LIT, false));
    }

    protected abstract MapCodec<? extends AbstractGenericFurnaceBlock> getCodec();

    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient && player.isSneaking() && player instanceof ServerPlayerEntity serverPlayer) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof AbstractGenericFurnaceBlockEntity furnaceBlockEntity) {
                furnaceBlockEntity.dropExperienceForRecipesUsed(serverPlayer);
                return ActionResult.SUCCESS;
            }
        }
        if (world.isClient) {
            return ActionResult.SUCCESS;
        } else {
            this.openScreen(world, pos, player);
            return ActionResult.CONSUME;
        }
    }

    protected abstract void openScreen(World world, BlockPos pos, PlayerEntity player);

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof AbstractGenericFurnaceBlockEntity) {
                if (world instanceof ServerWorld) {
                    ItemScatterer.spawn(world, pos, (AbstractGenericFurnaceBlockEntity)blockEntity);
                    ((AbstractGenericFurnaceBlockEntity)blockEntity).getRecipesUsedAndDropExperience((ServerWorld)world, Vec3d.ofCenter(pos));
                }

                super.onStateReplaced(state, world, pos, newState, moved);
                world.updateComparators(pos, this);
            } else {
                super.onStateReplaced(state, world, pos, newState, moved);
            }

        }
    }

    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
    }

    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Nullable
    protected static <T extends BlockEntity> BlockEntityTicker<T> validateTicker(World world, BlockEntityType<T> givenType, BlockEntityType<? extends AbstractGenericFurnaceBlockEntity> expectedType) {
        return world.isClient ? null : validateTicker(givenType, expectedType, AbstractGenericFurnaceBlockEntity::tick);
    }

    static {
        FACING = HorizontalFacingBlock.FACING;
        LIT = Properties.LIT;
    }

    public static void appendTooltip(int minusCookTimeTotal, List<Text> tooltip) {

        float speedPercentage = (200.0f / (200 - minusCookTimeTotal)) * 100;

        String formattedSpeed = String.format("%.1f%%", speedPercentage);

        tooltip.add(Text.translatable("tooltip.foxfurnace.speed", formattedSpeed)
                .formatted(Formatting.GRAY));
    }
}
