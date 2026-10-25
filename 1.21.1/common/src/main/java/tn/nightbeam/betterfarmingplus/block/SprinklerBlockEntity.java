package tn.nightbeam.betterfarmingplus.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import tn.nightbeam.betterfarmingplus.content.ModContent;
import tn.nightbeam.betterfarmingplus.content.ModTags;
import tn.nightbeam.betterfarmingplus.core.FarmingConfig;
import tn.nightbeam.betterfarmingplus.core.Rules;

/**
 * Every interval: waters the farmland around the sprinkler (two blocks below it to one above) and gives each boosted
 * plant (one below to one above) a chance of an extra random tick, so each plant grows by its own rules.
 */
public class SprinklerBlockEntity extends BlockEntity {
    /** How deep below the sprinkler it finds water: a block hidden under the field (where it hydrates nothing) works. */
    public static final int WATER_DEPTH = 4;

    public SprinklerBlockEntity(BlockPos pos, BlockState state) {
        super(ModContent.SPRINKLER_ENTITY.get(), pos, state);
    }

    /** Water within {@link #WATER_DEPTH} blocks straight below the sprinkler, or beside it. */
    public static boolean hasWater(BlockGetter level, BlockPos pos) {
        for (int down = 1; down <= WATER_DEPTH; down++) {
            if (level.getFluidState(pos.below(down)).is(FluidTags.WATER)) {
                return true;
            }
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (level.getFluidState(pos.relative(direction)).is(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }

    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        FarmingConfig.Sprinkler config = FarmingConfig.get().sprinkler();
        if (Math.floorMod(level.getGameTime() + pos.asLong(), config.intervalTicks()) != 0) {
            return;
        }
        boolean active = !config.needsWater() || hasWater(level, pos);
        if (state.getValue(SprinklerBlock.ACTIVE) != active) {
            level.setBlock(pos, state.setValue(SprinklerBlock.ACTIVE, active), Block.UPDATE_ALL);
        }
        if (active) {
            water(level, pos, config);
        }
    }

    /** One sprinkler cycle. Returns how many plants got a growth tick. */
    public static int water(ServerLevel level, BlockPos pos, FarmingConfig.Sprinkler config) {
        int boosted = 0;
        for (int[] offset : Rules.square(config.radius())) {
            for (int dy = -2; dy <= 1; dy++) {
                BlockPos target = pos.offset(offset[0], dy, offset[1]);
                BlockState state = level.getBlockState(target);
                if (config.hydrateFarmland() && state.getBlock() instanceof FarmBlock
                        && state.getValue(FarmBlock.MOISTURE) < FarmBlock.MAX_MOISTURE) {
                    level.setBlock(target, state.setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE), Block.UPDATE_CLIENTS);
                } else if (dy >= -1 && state.is(ModTags.SPRINKLER_BOOSTED) && state.isRandomlyTicking()
                        && level.getRandom().nextDouble() < config.growthChance()) {
                    state.randomTick(level, target, level.getRandom());
                    boosted++;
                }
            }
        }
        return boosted;
    }
}
