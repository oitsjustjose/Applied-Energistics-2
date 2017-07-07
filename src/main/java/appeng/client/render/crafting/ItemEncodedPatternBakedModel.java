package appeng.client.render.crafting;


import java.util.List;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Keyboard;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.common.model.TRSRTransformation;

import appeng.client.render.model.BaseBakedModel;
import appeng.items.misc.ItemEncodedPattern;


/**
 * This special model handles switching between rendering the crafting output of an encoded pattern (when shift is being held), and
 * showing the encoded pattern itself. Matters are further complicated by only wanting to show the crafting output when the pattern is being
 * rendered in the GUI, and not anywhere else.
 */
class ItemEncodedPatternBakedModel extends BaseBakedModel
{
	private final IBakedModel baseModel;

	private final ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms;

	private final CustomOverrideList overrides;

	ItemEncodedPatternBakedModel( IBakedModel baseModel, ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms )
	{
		super( baseModel );
		this.baseModel = baseModel;
		this.transforms = transforms;
		this.overrides = new CustomOverrideList();
	}

	@Override
	public List<BakedQuad> getQuads( @Nullable IBlockState state, @Nullable EnumFacing side, long rand )
	{
		return baseModel.getQuads( state, side, rand );
	}

	@Override
	public ItemOverrideList getOverrides()
	{
		return overrides;
	}

	/*@Override
	public Pair<? extends IBakedModel, Matrix4f> handlePerspective( ItemCameraTransforms.TransformType cameraTransformType )
	{
		if( baseModel instanceof IBakedModel )
		{
			return ( (IBakedModel) baseModel ).handlePerspective( cameraTransformType );
		}

		return PerspectiveMapWrapper.handlePerspective( this, transforms, cameraTransformType );
	}*/

	/**
	 * Since the ItemOverrideList handling comes before handling the perspective awareness (which is the first place where we
	 * know how we are being rendered) we need to remember the model of the crafting output, and make the decision on which to render later on.
	 * Sadly, Forge is pretty inconsistent when it will call the handlePerspective method, so some methods are called even on this interim-model.
	 * Usually those methods only matter for rendering on the ground and other cases, where we wouldn't render the crafting output model anyway,
	 * so in those cases we delegate to the model of the encoded pattern.
	 */
	private class ShiftHoldingModelWrapper extends BaseBakedModel
	{

		private final IBakedModel outputModel;

		private ShiftHoldingModelWrapper( IBakedModel outputModel )
		{
			super( baseModel );
			this.outputModel = outputModel;
		}

		@Override
		public Pair<? extends IBakedModel, Matrix4f> handlePerspective( ItemCameraTransforms.TransformType cameraTransformType )
		{
			final IBakedModel selectedModel;

			// No need to re-check for shift being held since this model is only handed out in that case
			if( cameraTransformType == ItemCameraTransforms.TransformType.GUI )
			{
				selectedModel = outputModel;
			}
			else
			{
				selectedModel = baseModel;
			}

			// Now retroactively handle the isGui3d call, for which we always return false below
			if( selectedModel.isGui3d() != baseModel.isGui3d() )
			{
				GlStateManager.enableLighting();
			}

			if( selectedModel instanceof IBakedModel )
			{
				return ( (IBakedModel) selectedModel ).handlePerspective( cameraTransformType );
			}

			return PerspectiveMapWrapper.handlePerspective( this, transforms, cameraTransformType );
		}

		// Other methods may be called for items on the ground, in which case we will always fall back to the pattern

		@Override
		public List<BakedQuad> getQuads( @Nullable IBlockState state, @Nullable EnumFacing side, long rand )
		{
			// This may be called for items on the ground, in which case we will always fall back to the pattern
			return baseModel.getQuads( state, side, rand );
		}
	}


	/**
	 * Item Override Lists are the only point during item rendering where we can access the item stack that is being rendered.
	 * So this is the point where we actually check if shift is being held, and if so, determine the crafting output model.
	 */
	private class CustomOverrideList extends ItemOverrideList
	{

		CustomOverrideList()
		{
			super( baseModel.getOverrides().getOverrides() );
		}

		@Override
		public IBakedModel handleItemState( IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity )
		{
			boolean shiftHeld = Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT );
			if( shiftHeld )
			{
				ItemEncodedPattern iep = (ItemEncodedPattern) stack.getItem();
				ItemStack output = iep.getOutput( stack );
				if (!output.isEmpty()) {
					IBakedModel realModel = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(output);
					// Give the item model a chance to handle the overrides as well
					realModel = realModel.getOverrides().handleItemState(realModel, output, world, entity);
					return new ShiftHoldingModelWrapper(realModel);
				}
			}

			return baseModel.getOverrides().handleItemState( originalModel, stack, world, entity );
		}
	}
}
