package tn.nightbeam.betterfarmingplus.client;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Vanilla BakedModel wrapper that strips alert/harvest-indicator geometry when disabled.
 */
public class HarvestIndicatorModel implements BakedModel {
	private final BakedModel wrapped;

	public HarvestIndicatorModel(BakedModel wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, RandomSource rand) {
		List<BakedQuad> quads = wrapped.getQuads(state, face, rand);
		if (!HarvestIndicators.showHarvestIndicators) {
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
}
