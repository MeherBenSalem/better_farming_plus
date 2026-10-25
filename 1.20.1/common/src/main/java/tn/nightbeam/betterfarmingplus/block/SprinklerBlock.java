package tn.nightbeam.betterfarmingplus.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tn.nightbeam.betterfarmingplus.content.ModContent;
import tn.nightbeam.betterfarmingplus.core.FarmingConfig;

/** A copper sprinkler head on a short pipe. {@code active} while it has water. */
public class SprinklerBlock extends BaseEntityBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    private static final VoxelShape SHAPE = Shapes.or(Block.box(6, 0, 6, 10, 9, 10), Block.box(3, 9, 3, 13, 12, 13), Block.box(7, 12, 7, 9, 14, 9));

    public SprinklerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean water = SprinklerBlockEntity.hasWater(context.getLevel(), context.getClickedPos());
        return defaultBlockState().setValue(ACTIVE, water || !FarmingConfig.get().sprinkler().needsWater());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SprinklerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, ModContent.SPRINKLER_ENTITY.get(),
                (tickLevel, pos, tickState, sprinkler) -> sprinkler.serverTick((ServerLevel) tickLevel, pos, tickState));
    }

    /** The spray: droplets fanning out from the head and falling over the watered square. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(ACTIVE)) {
            return;
        }
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.85;
        double z = pos.getZ() + 0.5;
        for (int i = 0; i < 6; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 0.15 + random.nextDouble() * 0.2;
            level.addParticle(ParticleTypes.SPLASH, x, y, z, Math.cos(angle) * speed, 0.25, Math.sin(angle) * speed);
        }
        int radius = Math.max(1, FarmingConfig.get().sprinkler().radius());
        for (int i = 0; i < 4; i++) {
            double dx = (random.nextDouble() * 2 - 1) * (radius + 0.5);
            double dz = (random.nextDouble() * 2 - 1) * (radius + 0.5);
            level.addParticle(ParticleTypes.FALLING_WATER, x + dx, y + 0.6 + random.nextDouble() * 0.4, z + dz, 0, 0, 0);
        }
    }
}
