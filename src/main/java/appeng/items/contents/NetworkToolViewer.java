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

package appeng.items.contents;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.networking.IGridHost;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;


public class NetworkToolViewer implements INetworkTool
{

	private final AppEngInternalInventory inv;
	private final ItemStack is;
	private final IGridHost gh;

	public NetworkToolViewer( final ItemStack is, final IGridHost gHost )
	{
		this.is = is;
		this.gh = gHost;
		this.inv = new AppEngInternalInventory( null, 9 );
		if( is.hasTagCompound() ) // prevent crash when opening network status screen.
		{
			this.inv.readFromNBT( Platform.openNbtData( is ), "inv" );
		}
	}

	@Override
	public int getSizeInventory()
	{
		return this.inv.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot( final int i )
	{
		return this.inv.getStackInSlot( i );
	}

	@Override
	public ItemStack decrStackSize( final int i, final int j )
	{
		return this.inv.decrStackSize( i, j );
	}

	@Override
	public ItemStack removeStackFromSlot( int i )
	{
		return this.inv.removeStackFromSlot( i );
	}

	@Override
	public void setInventorySlotContents( final int i, final ItemStack itemstack )
	{
		this.inv.setInventorySlotContents( i, itemstack );
	}

	@Override
	public String getName()
	{
		return this.inv.getName();
	}

	@Override
	public boolean hasCustomName()
	{
		return this.inv.hasCustomName();
	}

	@Override
	public int getInventoryStackLimit()
	{
		return this.inv.getInventoryStackLimit();
	}

	@Override
	public void markDirty()
	{
		this.inv.markDirty();
		this.inv.writeToNBT( Platform.openNbtData( this.is ), "inv" );
	}

	@Override
	public boolean isUsableByPlayer( final EntityPlayer entityplayer )
	{
		return this.inv.isUsableByPlayer( entityplayer );
	}

	@Override
	public void openInventory( final EntityPlayer player )
	{
		this.inv.openInventory( player );
	}

	@Override
	public void closeInventory( final EntityPlayer player )
	{
		this.inv.closeInventory( player );
	}

	@Override
	public boolean isItemValidForSlot( final int i, final ItemStack itemstack )
	{
		return this.inv.isItemValidForSlot( i, itemstack ) && itemstack.getItem() instanceof IUpgradeModule && ( (IUpgradeModule) itemstack.getItem() ).getType( itemstack ) != null;
	}

	@Override
	public ItemStack getItemStack()
	{
		return this.is;
	}

	@Override
	public IGridHost getGridHost()
	{
		return this.gh;
	}

	@Override
	public int getField( final int id )
	{
		return this.inv.getField( id );
	}

	@Override
	public void setField( final int id, final int value )
	{
		this.inv.setField( id, value );
	}

	@Override
	public int getFieldCount()
	{
		return this.inv.getFieldCount();
	}

	@Override
	public void clear()
	{
		this.inv.clear();
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return this.inv.getDisplayName();
	}

	@Override
	public boolean isEmpty()
	{
		return false;
	}
}
