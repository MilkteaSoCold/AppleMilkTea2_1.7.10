package mods.defeatedcrow.common.tile.energy;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.defeatedcrow.api.energy.IBattery;
import mods.defeatedcrow.common.tile.appliance.TileProsessor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.ItemStack;

public class ContainerBatBox extends Container {
	
	private TileChargerBase tile;
	private int lastGauge;
	
	public ContainerBatBox(EntityPlayer player, TileChargerBase par2TileEntity) {
		this.tile = par2TileEntity;
		
		/*スロットの生成*/
		//燃料
		this.addSlotToContainer(new Slot(this.tile, 0, 9, 9));
		//完成品
		this.addSlotToContainer(new SlotFurnace(player, this.tile, 1, 9, 55));
		//充電スロット
		int j;
		for (j = 0; j < 2; ++j)
		{
			for (int k = 0; k < 4; ++k)
			{
				this.addSlotToContainer(new Slot(this.tile, 2 + k + j * 4, 53 + k * 18, 30 + j * 18));
			}
		}
		
		//プレイヤーのインベントリ
		int i;
		// 1 ～ 3段目のインベントリ
		for (i = 0; i < 3; ++i)
		{
			for (int h = 0; h < 9; ++h)
			{
				this.addSlotToContainer(new Slot(player.inventory, h + i * 9 + 9, 8 + h * 18, 84 + i * 18));
			}
		}
 
		// 4段目のインベントリ
		for (i = 0; i < 9; ++i)
		{
			this.addSlotToContainer(new Slot(player.inventory, i, 8 + i * 18, 142));
		}
	}
	
	//チャージゲージの更新に使用
	public void addCraftingToCrafters(ICrafting par1ICrafting)
	{
		super.addCraftingToCrafters(par1ICrafting);
		par1ICrafting.sendProgressBarUpdate(this, 0, this.tile.getChargeAmount());
	}
	
	//更新を送る
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();
 
		for (int i = 0; i < this.crafters.size(); ++i)
		{
			ICrafting icrafting = (ICrafting)this.crafters.get(i);
 
			if (this.lastGauge != this.tile.getChargeAmount())
			{
				icrafting.sendProgressBarUpdate(this, 0, this.tile.getChargeAmount());
			}
		}
 
		this.lastGauge = this.tile.getChargeAmount();
	}
	
	//クライアント側で更新を受け取る
	@SideOnly(Side.CLIENT)
	public void updateProgressBar(int par1, int par2)
	{
		if (par1 == 0)
		{
			this.tile.setChargeAmount(par2);
		}
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return this.tile.isUseableByPlayer(player);
	}
	
	//Shiftクリックでの処理
	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2)
	{
		ItemStack itemstack = null;
		Slot slot = (Slot)this.inventorySlots.get(par2);
 
		if (slot != null && slot.getHasStack())
		{
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
 
			//カーソルを排出スロットにあわせているとき
			if (par2 == 1)
			{
				//アイテムの移動(スロット10～46へ)
				if (!this.mergeItemStack(itemstack1, 10, 46, true))
				{
					return null;
				}
 
				slot.onSlotChange(itemstack1, itemstack);
			}
			//カーソルをプレイヤーのインベントリにあわせている
			else if (par2 > 9)
			{
				//燃料である
				if (TileChargerBase.isItemFuel(itemstack))
				{
					//アイテムの移動(スロット0～1へ)
					if (!this.mergeItemStack(itemstack1, 0, 1, false))
					{
						//燃料欄がいっぱいの時、かつアイテムがIBatteryのとき
						if (itemstack1.getItem() instanceof IBattery)
						{
							//アイテムの移動(スロット2～9へ)
							IBattery bat = (IBattery) itemstack1.getItem();
							if (!this.mergeItemStack(itemstack1, 2, 9, false))
							{
								return null;
							}
						}
					}
				}
				else if (itemstack1.getItem() instanceof IBattery)//バッテリーアイテム
				{
					//アイテムの移動(スロット2～9へ)
					IBattery bat = (IBattery) itemstack1.getItem();
					if (!this.mergeItemStack(itemstack1, 2, 9, false))
					{
						return null;
					}
				}
				else//それ以外のアイテムは何もしない
				{
					return null;
				}
			}
			//アイテムの移動(スロット10～46へ)
			else if (!this.mergeItemStack(itemstack1, 10, 46, false))
			{
				return null;
			}
 
			if (itemstack1.stackSize == 0)
			{
				slot.putStack((ItemStack)null);
			}
			else
			{
				slot.onSlotChanged();
			}
 
			if (itemstack1.stackSize == itemstack.stackSize)
			{
				return null;
			}
 
			slot.onPickupFromSlot(par1EntityPlayer, itemstack1);
		}
 
		return itemstack;
	}

}
