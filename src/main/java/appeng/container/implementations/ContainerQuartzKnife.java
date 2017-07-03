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

package appeng.container.implementations;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;

import appeng.api.AEApi;
import appeng.container.AEBaseContainer;
import appeng.container.slot.QuartzKnifeOutput;
import appeng.container.slot.SlotRestrictedInput;
import appeng.items.contents.QuartzKnifeObj;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;


public class ContainerQuartzKnife extends AEBaseContainer implements IAEAppEngInventory, IInventory
{

	private final QuartzKnifeObj toolInv;

	private final AppEngInternalInventory inSlot = new AppEngInternalInventory( this, 1 );
	private final SlotRestrictedInput metals;
	private final QuartzKnifeOutput output;
	private String myName = "";

	public ContainerQuartzKnife( final InventoryPlayer ip, final QuartzKnifeObj te )
	{
		super( ip, null, null );
		this.toolInv = te;

		this.metals = new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.METAL_INGOTS, this.inSlot, 0, 94, 44, ip );
		this.addSlotToContainer( this.metals );

		this.output = new QuartzKnifeOutput( this, 0, 134, 44, -1 );
		this.addSlotToContainer( this.output );

		this.lockPlayerInventorySlot( ip.currentItem );

		this.bindPlayerInventory( ip, 0, 184 - /* height of player inventory */82 );
	}

	public void setName( final String value )
	{
		this.myName = value;
	}

	@Override
	public void detectAndSendChanges()
	{
		final ItemStack currentItem = this.getPlayerInv().getCurrentItem();
		final ItemStack offhandItem = this.getPlayerInv().offHandInventory.get( 0 );
		final ItemStack toolItem = this.toolInv.getItemStack();

		if( currentItem != toolItem && offhandItem != toolItem )
		{
			if ( !currentItem.isEmpty() && Platform.itemComparisons().isEqualItem( toolItem, currentItem ) ) {
				this.getPlayerInv().setInventorySlotContents( this.getPlayerInv().currentItem, toolItem );
			}
			else if ( !offhandItem.isEmpty() && Platform.itemComparisons().isEqualItem(toolItem, offhandItem) )
			{
				this.getPlayerInv().offHandInventory.set( 0, toolItem );
			}
			else
			{
				this.setValidContainer( false );
			}
		}

		super.detectAndSendChanges();
	}

	@Override
	public void onContainerClosed( final EntityPlayer par1EntityPlayer )
	{
		if (!this.inSlot.getStackInSlot(0).isEmpty()) {
			par1EntityPlayer.dropItem(this.inSlot.getStackInSlot(0), false);
		}
	}

	@Override
	public void saveChanges()
	{

	}

	@Override
	public void onChangeInventory( final IInventory inv, final int slot, final InvOperation mc, final ItemStack removedStack, final ItemStack newStack )
	{

	}

	@Override
	public int getSizeInventory()
	{
		return 1;
	}

	@Override
	public ItemStack getStackInSlot( final int var1 )
	{
		final ItemStack input = this.inSlot.getStackInSlot( 0 );
		if( input == ItemStack.EMPTY )
		{
			return ItemStack.EMPTY;
		}

		if( SlotRestrictedInput.isMetalIngot( input ) )
		{
			if( this.myName.length() > 0 )
			{
				return AEApi.instance().definitions().materials().namePress().maybeStack( 1 ).map( namePressStack ->
				{
					final NBTTagCompound compound = Platform.openNbtData( namePressStack );
					compound.setString( "InscribeName", this.myName );

					return namePressStack;
				} ).orElse(ItemStack.EMPTY);
			}
		}

		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack decrStackSize( final int var1, final int var2 )
	{
		final ItemStack is = this.getStackInSlot( 0 );
		if (!is.isEmpty()) {
			if (this.makePlate()) {
				return is;
			}
		}
		return ItemStack.EMPTY;
	}

	private boolean makePlate()
	{
		if (!this.inSlot.decrStackSize(0, 1).isEmpty()) {
			final ItemStack item = this.toolInv.getItemStack();
			item.damageItem(1, this.getPlayerInv().player);

			if (item.getCount() == 0) {
				this.getPlayerInv().mainInventory.add(this.getPlayerInv().currentItem, ItemStack.EMPTY);
				MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(this.getPlayerInv().player, item, null));
			}

			return true;
		}
		return false;
	}

	@Override
	public ItemStack removeStackFromSlot( final int var1 )
	{
		return ItemStack.EMPTY;
	}

	@Override
	public void setInventorySlotContents( final int var1, final ItemStack var2 )
	{
		if(var2.isEmpty() && Platform.isServer() )
		{
			this.makePlate();
		}
	}

	@Override
	public String getName()
	{
		return "Quartz Knife Output";
	}

	@Override
	public boolean hasCustomName()
	{
		return false;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 1;
	}

	@Override
	public void markDirty()
	{

	}

	@Override
	public boolean isUsableByPlayer( EntityPlayer player )
	{
		return false;
	}

	@Override
	public void openInventory( final EntityPlayer player )
	{

	}

	@Override
	public void closeInventory( final EntityPlayer player )
	{

	}

	@Override
	public boolean isItemValidForSlot( final int var1, final ItemStack var2 )
	{
		return false;
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return null;
	}

	@Override
	public int getField( final int id )
	{
		return 0;
	}

	@Override
	public void setField( final int id, final int value )
	{

	}

	@Override
	public int getFieldCount()
	{
		return 0;
	}

	@Override
	public void clear()
	{
		this.inSlot.setInventorySlotContents(0, ItemStack.EMPTY);
	}

	@Override
	public boolean isEmpty()
	{
		// TODO Auto-generated method stub
		return false;
	}
}
