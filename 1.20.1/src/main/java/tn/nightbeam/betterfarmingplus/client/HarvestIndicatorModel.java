package tn.nightbeam.betterfarmingplus.client;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraft.client.renderer.RenderType;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A BakedModel wrapper that strips out the alert/harvest-indicator geometry
 * when the show_harvest_indicators config option is disabled.
 * Alert quads are identified by their sprite path containing "alert".
 */
@OnlyIn(Dist.CLIENT)
public class HarvestIndicatorModel implements BakedModel {

    private final BakedModel wrapped;

    public HarvestIndicatorModel(BakedModel wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
        List<BakedQuad> quads = wrapped.getQuads(state, face, rand, data, renderType);
        if (!ClientModEvents.showHarvestIndicators) {
            return quads.stream()
                    .filter(q -> !q.getSprite().contents().name().getPath().equals("block/alert"))
                    .toList();
        }
        return quads;
    }

    // Legacy vanilla fallback (called by vanilla code paths that don't pass ModelData/RenderType)
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, RandomSource rand) {
        List<BakedQuad> quads = wrapped.getQuads(state, face, rand);
        if (!ClientModEvents.showHarvestIndicators) {
            return quads.stream()
                    .filter(q -> !q.getSprite().contents().name().getPath().equals("block/alert"))
                    .toList();
        }
        return quads;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return wrapped.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return wrapped.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return wrapped.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return wrapped.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return wrapped.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return wrapped.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides() {
        return wrapped.getOverrides();
    }

    @Override
    public TextureAtlasSprite getParticleIcon(ModelData data) {
        return wrapped.getParticleIcon(data);
    }

    @Override
    public ModelData getModelData(net.minecraft.world.level.BlockAndTintGetter level, net.minecraft.core.BlockPos pos, BlockState state, ModelData modelData) {
        return wrapped.getModelData(level, pos, state, modelData);
    }
}
