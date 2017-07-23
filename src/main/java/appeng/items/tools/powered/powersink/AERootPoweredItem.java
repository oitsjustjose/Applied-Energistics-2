/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.items.tools.powered.powersink;


import java.text.MessageFormat;
import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import appeng.api.config.AccessRestriction;
import appeng.api.config.PowerUnits;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.util.Platform;


public abstract class AERootPoweredItem extends AEBaseItem implements IAEItemPowerStorage
{
	private static final String POWER_NBT_KEY = "internalCurrentPower";
	private final double powerCapacity;

	public AERootPoweredItem( final double powerCapacity )
	{
		this.setMaxDamage( 32 );
		this.hasSubtypes = false;
		this.setFull3D();

		this.powerCapacity = powerCapacity;
	}

	@Override
	public void addCheckedInformation( final ItemStack stack, final World world, final List<String> lines, final boolean displayMoreInfo )
	{
		final NBTTagCompound tag = stack.getTagCompound();
		double internalCurrentPower = 0;
		final double internalMaxPower = this.getAEMaxPower( stack );

		if( tag != null )
		{
			internalCurrentPower = tag.getDouble( "internalCurrentPower" );
		}

		final double percent = internalCurrentPower / internalMaxPower;

		lines.add( GuiText.StoredEnergy.getLocal() + ':' + MessageFormat.format( " {0,number,#} ", internalCurrentPower ) + Platform
				.gui_localize( PowerUnits.AE.unlocalizedName ) + " - " + MessageFormat.format( " {0,number,#.##%} ", percent ) );
	}

	@Override
	public boolean isDamageable()
	{
		return true;
	}

	@Override
	protected void getCheckedSubItems( final CreativeTabs creativeTab, final NonNullList<ItemStack> itemStacks )
	{
		super.getCheckedSubItems( creativeTab, itemStacks );

		final ItemStack charged = new ItemStack( this, 1 );
		final NBTTagCompound tag = Platform.openNbtData( charged );
		tag.setDouble( "internalCurrentPower", this.getAEMaxPower( charged ) );
		tag.setDouble( "internalMaxPower", this.getAEMaxPower( charged ) );

		itemStacks.add( charged );
	}

	@Override
	public boolean isRepairable()
	{
		return false;
	}

	@Override
	public double getDurabilityForDisplay( final ItemStack is )
	{
		return 1 - this.getAECurrentPower( is ) / this.getAEMaxPower( is );
	}

	@Override
	public boolean isDamaged( final ItemStack stack )
	{
		return true;
	}

	@Override
	public void setDamage( final ItemStack stack, final int damage )
	{

	}

	private double getInternalBattery( final ItemStack is, final batteryOperation op, final double adjustment )
	{
		final NBTTagCompound data = Platform.openNbtData( is );

		double currentStorage = data.getDouble( POWER_NBT_KEY );
		final double maxStorage = this.getAEMaxPower( is );

		switch( op )
		{
			case INJECT:
				currentStorage += adjustment;
				if( currentStorage > maxStorage )
				{
					final double diff = currentStorage - maxStorage;
					data.setDouble( POWER_NBT_KEY, maxStorage );
					return diff;
				}
				data.setDouble( POWER_NBT_KEY, currentStorage );
				return 0;
			case EXTRACT:
				if( currentStorage > adjustment )
				{
					currentStorage -= adjustment;
					data.setDouble( POWER_NBT_KEY, currentStorage );
					return adjustment;
				}
				data.setDouble( POWER_NBT_KEY, 0 );
				return currentStorage;
			default:
				break;
		}

		return currentStorage;
	}

	/**
	 * Inject power into this item using an external unit.
	 */
	double injectExternalPower( PowerUnits inputUnit, final ItemStack is, final double amount, final boolean simulate )
	{
		if( simulate )
		{
			final int requiredExt = (int) PowerUnits.AE.convertTo( inputUnit, this.getAEMaxPower( is ) - this.getAECurrentPower( is ) );
			if( amount < requiredExt )
			{
				return 0;
			}
			return amount - requiredExt;
		}
		else
		{
			final double powerRemainder = this.injectAEPower( is, inputUnit.convertTo( PowerUnits.AE, amount ) );
			return PowerUnits.AE.convertTo( inputUnit, powerRemainder );
		}
	}

	@Override
	public double injectAEPower( final ItemStack is, final double amt )
	{
		return this.getInternalBattery( is, batteryOperation.INJECT, amt );
	}

	@Override
	public double extractAEPower( final ItemStack is, final double amt )
	{
		return this.getInternalBattery( is, batteryOperation.EXTRACT, amt );
	}

	@Override
	public double getAEMaxPower( final ItemStack is )
	{
		return this.powerCapacity;
	}

	@Override
	public double getAECurrentPower( final ItemStack is )
	{
		return this.getInternalBattery( is, batteryOperation.STORAGE, 0 );
	}

	@Override
	public AccessRestriction getPowerFlow( final ItemStack is )
	{
		return AccessRestriction.WRITE;
	}

	@Override
	public ICapabilityProvider initCapabilities( ItemStack stack, NBTTagCompound nbt )
	{
		return new PoweredItemCapabilities( stack, this );
	}

	private enum batteryOperation
	{
		STORAGE, INJECT, EXTRACT
	}
}
