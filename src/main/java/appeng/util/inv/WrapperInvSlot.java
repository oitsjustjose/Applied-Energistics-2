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

package appeng.util.inv;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;


public class WrapperInvSlot
{

	private final IInventory inv;

	public WrapperInvSlot( final IInventory inv )
	{
		this.inv = inv;
	}

	public IInventory getWrapper( final int slot )
	{
		return new InternalInterfaceWrapper( this.inv, slot );
	}

	protected boolean isItemValid( final ItemStack itemstack )
	{
		return true;
	}

	private class InternalInterfaceWrapper implements IInventory
	{

		private final IInventory inv;
		private final int slot;

		public InternalInterfaceWrapper( final IInventory target, final int slot )
		{
			this.inv = target;
			this.slot = slot;
		}

		@Override
		public int getSizeInventory()
		{
			return 1;
		}

		@Override
		public ItemStack getStackInSlot( final int i )
		{
			return this.inv.getStackInSlot( this.slot );
		}

		@Override
		public ItemStack decrStackSize( final int i, final int num )
		{
			return this.inv.decrStackSize( this.slot, num );
		}

		@Override
		public ItemStack removeStackFromSlot( final int i )
		{
			return this.inv.removeStackFromSlot( this.slot );
		}

		@Override
		public void setInventorySlotContents( final int i, final ItemStack itemstack )
		{
			this.inv.setInventorySlotContents( this.slot, itemstack );
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
		}

		@Override
		public boolean isUsableByPlayer( final EntityPlayer entityplayer )
		{
			return this.inv.isUsableByPlayer( entityplayer );
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
		public void clear()
		{
			this.inv.clear();
		}

		@Override
		public int getField( final int id )
		{
			return this.inv.getField( id );
		}

		@Override
		public int getFieldCount()
		{
			return this.inv.getFieldCount();
		}

		@Override
		public boolean isItemValidForSlot( final int i, final ItemStack itemstack )
		{
			return WrapperInvSlot.this.isItemValid( itemstack ) && this.inv.isItemValidForSlot( this.slot, itemstack );
		}

		@Override
		public ITextComponent getDisplayName()
		{
			return this.inv.getDisplayName();
		}

		@Override
		public void setField( final int id, final int value )
		{
			this.inv.setField( id, value );
		}

		@Override
		public boolean isEmpty()
		{
			// TODO Auto-generated method stub
			return false;
		}
	}
}
