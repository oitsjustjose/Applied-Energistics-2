/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.block.storage;


import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import appeng.api.AEApi;
import appeng.api.util.AEPartLocation;
import appeng.block.AEBaseTileBlock;
import appeng.client.UnlistedProperty;
import appeng.client.render.FacingToRotation;
import appeng.core.sync.GuiBridge;
import appeng.tile.storage.TileDrive;
import appeng.util.Platform;


public class BlockDrive extends AEBaseTileBlock
{

	public static final UnlistedProperty<DriveSlotsState> SLOTS_STATE = new UnlistedProperty<>("drive_slots_state", DriveSlotsState.class);

	public BlockDrive()
	{
		super( Material.IRON );
		this.setTileEntity( TileDrive.class );
	}

	@Override
	public BlockRenderLayer getBlockLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new ExtendedBlockState( this, getAEStates(), new IUnlistedProperty[] {
				SLOTS_STATE,
				FORWARD,
				UP
		} );
	}

	@Override
	public IBlockState getExtendedState( IBlockState state, IBlockAccess world, BlockPos pos )
	{
		TileDrive te = getTileEntity( world, pos );
		if( te == null )
		{
			return super.getExtendedState( state, world, pos );
		}

		IExtendedBlockState extState = (IExtendedBlockState) super.getExtendedState( state, world, pos );
		return extState.withProperty( SLOTS_STATE, DriveSlotsState.fromChestOrDrive( te ) );
	}

	private Vector3f rotateHitByFacing(float x, float y, float z, EnumFacing front, EnumFacing up)
	{
		//AELog.info( "in = "+x+", "+y+", "+z );
		Vector3f coord = new Vector3f(x,y,z);
		Matrix4f transform = FacingToRotation.get( front, up ).getMat();//new Matrix3f();
		transform.invert();
		transform.transform( coord );

		if ( coord.x < 0 )
			coord.x += 1;
		if ( coord.y < 0 )
			coord.y += 1;

		return new Vector3f( coord.x, coord.y, coord.z );
	}

	@Override
	public boolean onActivated( final World w, final BlockPos pos, final EntityPlayer p, final EnumHand hand, final ItemStack heldItem, final EnumFacing side, final float hitX, final float hitY, final float hitZ )
	{
		final TileDrive tg = this.getTileEntity( w, pos );
		if( tg != null )
		{
			Vector3f mapped = rotateHitByFacing( hitX, hitY, hitZ, tg.getForward(), tg.getUp() );
			if( p.isSneaking() )
			{
				if ( !w.isRemote && p.getHeldItem( hand ).isEmpty() && side == tg.getForward() )
				{
					return tg.tryAutoExtractDrive( mapped.x, mapped.y, p, hand );
				}
				return p.getHeldItem( hand ).isEmpty();
			}


			if( Platform.isServer() )
			{
				if ( side != tg.getForward() || heldItem.isEmpty() || !AEApi.instance().registries().cell().isCellHandled( heldItem ) || !tg.tryAutoInsertDrive( mapped.x, mapped.y, heldItem, p, hand ) )
					Platform.openGUI( p, tg, AEPartLocation.fromFacing( side ), GuiBridge.GUI_DRIVE );
			}
			return true;
		}
		return false;
	}
}
