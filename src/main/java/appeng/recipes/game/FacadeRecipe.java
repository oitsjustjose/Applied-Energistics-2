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

package appeng.recipes.game;


import javax.annotation.Nullable;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

import appeng.api.AEApi;
import appeng.api.definitions.IComparableDefinition;
import appeng.api.definitions.IDefinitions;
import appeng.items.parts.ItemFacade;


public final class FacadeRecipe implements IRecipe
{
	private final IComparableDefinition anchor;
	private final ItemFacade facade;

	public FacadeRecipe( ItemFacade facade )
	{
		this.facade = facade;
		final IDefinitions definitions = AEApi.instance().definitions();

		this.anchor = definitions.parts().cableAnchor();
	}

	@Override
	public boolean matches( final InventoryCrafting inv, final World w )
	{
		return this.getOutput( inv, false ) != null;
	}

	@Nullable
	private ItemStack getOutput( final IInventory inv, final boolean createFacade )
	{
		if( inv.getStackInSlot( 0 ) == null && inv.getStackInSlot( 2 ) == null && inv.getStackInSlot( 6 ) == null && inv.getStackInSlot( 8 ) == null )
		{
			if( this.anchor.isSameAs( inv.getStackInSlot( 1 ) ) && this.anchor.isSameAs( inv.getStackInSlot( 3 ) ) && this.anchor.isSameAs( inv.getStackInSlot( 5 ) ) && this.anchor.isSameAs( inv.getStackInSlot( 7 ) ) )
			{
				final ItemStack facades = facade.createFacadeForItem( inv.getStackInSlot( 4 ), !createFacade );
				if( facades != null && createFacade )
				{
					facades.setCount( 4 );
				}
				return facades;
			}
		}

		return null;
	}

	@Override
	public ItemStack getCraftingResult( final InventoryCrafting inv )
	{
		return this.getOutput( inv, true );
	}

	@Override
	public int getRecipeSize()
	{
		return 9;
	}

	@Override
	public ItemStack getRecipeOutput() // no default output..
	{
		return null;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems( final InventoryCrafting inv )
	{
		return ForgeHooks.defaultRecipeGetRemainingItems( inv );
	}

}