package tn.nightbeam.betterfarmingplus.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

import javax.annotation.Nullable;
import java.util.List;

public class HarvestIndicatorModelForge extends HarvestIndicatorModel {
	private final net.minecraft.client.resources.model.BakedModel wrapped;

	public HarvestIndicatorModelForge(net.minecraft.client.resources.model.BakedModel wrapped) {
		super(wrapped);
		this.wrapped = wrapped;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
		List<BakedQuad> quads = wrapped.getQuads(state, face, rand, data, renderType);
		if (!HarvestIndicators.showHarvestIndicators) {
			return quads.stream()
					.filter(q -> !q.getSprite().contents().name().getPath().equals("block/alert"))
					.toList();
		}
		return quads;
	}
}
