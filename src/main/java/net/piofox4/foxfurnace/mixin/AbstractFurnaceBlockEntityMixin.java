package net.piofox4.foxfurnace.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.piofox4.foxfurnace.util.FurnaceSpeedManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceBlockEntityMixin {
    @Unique
    private static final int DEFAULT_COOK_TIME = 200;

    @Shadow
    int cookTime;

    @Shadow
    int cookTimeTotal;

    @Shadow
    int burnTime;

    @Shadow
    int fuelTime;

    @Unique
    private int foxfurnace$minusCookTimeTotal = 0;

    @Unique
    private boolean foxfurnace$isUpdating = false;


    @ModifyReturnValue(method = "getCookTime(Lnet/minecraft/world/World;Lnet/minecraft/block/entity/AbstractFurnaceBlockEntity;)I", at = @At("RETURN"))
    private static int modifyGetCookTime(int original, World world, AbstractFurnaceBlockEntity blockEntity) {
        if (world == null || blockEntity == null) return original;
        return FurnaceSpeedManager.getAdjustedCookTime(original, blockEntity);
    }


    @ModifyReturnValue(method = "getFuelTime", at = @At("RETURN"))
    private int modifyGetFuelTime(int original, ItemStack fuel) {
        if (original == 0) return 0;

        AbstractFurnaceBlockEntity self = (AbstractFurnaceBlockEntity) (Object) this;
        int speedReduction = FurnaceSpeedManager.getFurnaceSpeedReduction(self);

        if (speedReduction > 0) {
            float speedFactor = ((float) (DEFAULT_COOK_TIME - speedReduction)) / DEFAULT_COOK_TIME;
            return Math.max(1, Math.round(original * speedFactor));
        }

        return original;
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private static void onTickStart(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.BlockState state, AbstractFurnaceBlockEntity blockEntity, CallbackInfo ci) {
        if (world == null || world.isClient || blockEntity == null) return;

        AbstractFurnaceBlockEntityMixin mixin = (AbstractFurnaceBlockEntityMixin) (Object) blockEntity;
        int newSpeedReduction = FurnaceSpeedManager.getFurnaceSpeedReduction(blockEntity);

        if (mixin.foxfurnace$minusCookTimeTotal != newSpeedReduction) {
            mixin.foxfurnace$isUpdating = true;
            mixin.foxfurnace$updateCookTime(newSpeedReduction);
            mixin.foxfurnace$minusCookTimeTotal = newSpeedReduction;
            return;
        }

        if (mixin.foxfurnace$isUpdating) {
            if (mixin.burnTime > 0) {
                mixin.burnTime += 1;
            }
            mixin.foxfurnace$isUpdating = false;
        }
    }

    @Unique
    private void foxfurnace$updateCookTime(int newSpeedReduction) {
        float cookProgressPercentage = 0;
        if (this.cookTimeTotal > 0) {
            cookProgressPercentage = (float) this.cookTime / this.cookTimeTotal;
        }

        float fuelRemainingPercentage = 0;
        if (this.fuelTime > 0) {
            fuelRemainingPercentage = (float) this.burnTime / this.fuelTime;
        }

        float speedRatio = (float) (DEFAULT_COOK_TIME - newSpeedReduction) / this.cookTimeTotal;

        this.cookTimeTotal = DEFAULT_COOK_TIME - newSpeedReduction;

        this.cookTime = Math.round(cookProgressPercentage * this.cookTimeTotal);

        if (this.fuelTime > 0) {
            this.fuelTime = Math.round(this.fuelTime * speedRatio);
            this.burnTime = Math.round(this.fuelTime * fuelRemainingPercentage);
        }

        AbstractFurnaceBlockEntity self = (AbstractFurnaceBlockEntity) (Object) this;
        self.markDirty();
    }

    @Inject(method = "setStack", at = @At("HEAD"))
    public void onSetStack(int slot, ItemStack stack, CallbackInfo ci) {
        if (slot == 0 || slot == 1) {
            AbstractFurnaceBlockEntity self = (AbstractFurnaceBlockEntity) (Object) this;
            int currentSpeedReduction = FurnaceSpeedManager.getFurnaceSpeedReduction(self);
            if (slot == 0) {
                this.cookTimeTotal = DEFAULT_COOK_TIME - currentSpeedReduction;
                this.cookTime = 0;
                this.foxfurnace$minusCookTimeTotal = currentSpeedReduction;
            }
        }
    }
}