package mods.defeatedcrow.recipe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mods.defeatedcrow.api.recipe.IProsessorRecipe;
import mods.defeatedcrow.api.recipe.IProsessorRecipeRegister;
import mods.defeatedcrow.api.recipe.RecipeRegisterManager;
import mods.defeatedcrow.common.AMTLogger;
import mods.defeatedcrow.common.DCsAppleMilk;
import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.oredict.OreDictionary;

public class ProsessorRecipeRegister implements IProsessorRecipeRegister{

	private static List<ProsessorRecipe> recipes;
	
	public ProsessorRecipeRegister()
	{
		this.recipes = new ArrayList<ProsessorRecipe>();
	}
	
	public IProsessorRecipeRegister instance()
	{
		return RecipeRegisterManager.prosessorRecipe;
	}
	
	@Override
	public void addRecipe(ItemStack output, boolean flag, ItemStack secondary, float secondaryChance, Object... input){
		float c = MathHelper.clamp_float(0.0F, secondaryChance, 1.0F);
		recipes.add(new ProsessorRecipe(output, secondary, flag, c, input));
		AMTLogger.debugInfo("Add Prosessor recipe: output " + output.getDisplayName());
	}
	
	public void addRecipe(ItemStack output, boolean flag, ItemStack secondary, Object... input) {
		addRecipe(output, flag, secondary, 1.0F, input);
	}

	@Override
	public List<ProsessorRecipe> getRecipes() {
		return this.recipes;
	}
	
	public class ProsessorRecipe implements IProsessorRecipe{
		
		public final ItemStack output;
		public final ItemStack secondary;
		private final Object[] input;
		private final ArrayList<Object> processedInput;
		private final float chance;
		
		public final boolean foodRecipe;
		
		public ProsessorRecipe(ItemStack output, ItemStack sec, boolean flag, float secondaryChance, Object... inputs)
		{
			this.output = output;
			this.input = inputs;
			this.secondary = sec;
			this.foodRecipe = flag;
			this.chance = secondaryChance;
			this.processedInput = new ArrayList<Object>();
			for (int i = 0; i < inputs.length; i++) {
				if (inputs[i] instanceof String) {
					processedInput.add(OreDictionary.getOres((String) inputs[i]));
				} else if (inputs[i] instanceof ItemStack) {
					processedInput.add(((ItemStack)inputs[i]).copy());
				} else if (inputs[i] instanceof Item) {
					processedInput.add(new ItemStack((Item)inputs[i], 1, 0));
				} else if (inputs[i] instanceof Block) {
					processedInput.add(new ItemStack((Block)inputs[i], 1, 0));
				} else {
					throw new IllegalArgumentException("Unknown Object passed to recipe!");
				}
			}
		}
		
		@Override
		public Object[] getInput() {
			return this.input;
		}

		@Override
		public ItemStack getOutput() {
			return this.output.copy();
		}
		
		@Override
		public float getChance()
		{
			return this.chance;
		}
		
		@Override
		public ItemStack getSecondary() {
			if (this.secondary != null){
				return this.secondary.copy();
			}
			else {
				return null;
			}
		}
		
		@Override
		public ItemStack getContainerItem(List<ItemStack> items)
		{
			ItemStack cont = null;
			for (int i = 0 ; i < items.size() ; i++)
			{
				ItemStack next = items.get(i);
				if (next != null) {
					cont = next.getItem().getContainerItem(next);
					if (cont != null) {
						break;
					}
					else {
						cont = FluidContainerRegistry.drainFluidContainer(next);
						if (cont != null) {
							break;
						}
					}
				}
			}
			
			return cont == null ? null : cont;
		}
		
		@Override
		public boolean isFoodRecipe()
		{
			return this.foodRecipe;
		}
		
		@Override
		public List<Object> getProsessedInput()
		{
			return new ArrayList<Object>(this.processedInput);
		}
		
		@Override
		public int getRecipeSize()
		{
			return this.processedInput.size();
		}
		
		@Override
	    public boolean matches(List<ItemStack> items)
	    {
	        ArrayList<Object> required = new ArrayList<Object>(this.processedInput);
	        boolean food = this.isFoodRecipe();

	        for (int x = 0; x < items.size(); x++)
	        {
	            ItemStack slot = items.get(x);

	            if (slot != null)
	            {
	                boolean inRecipe = false;
	                Iterator<Object> req = required.iterator();
	                
	                if (slot.getItem() == DCsAppleMilk.slotPanel)
	                {
	                	inRecipe = true;
	                	continue;
	                }

	                while (req.hasNext())
	                {
	                    boolean match = false;

	                    Object next = req.next();

	                    if (next instanceof ItemStack)
	                    {
	                        match = OreDictionary.itemMatches((ItemStack)next, slot, false);
	                    }
	                    else if (next instanceof ArrayList)
	                    {
	                        Iterator<ItemStack> itr = ((ArrayList<ItemStack>)next).iterator();
	                        while (itr.hasNext() && !match)
	                        {
	                            match = OreDictionary.itemMatches(itr.next(), slot, false);
	                        }
	                    }

	                    if (match)
	                    {
	                        inRecipe = true;
	                        required.remove(next);
	                        break;
	                    }
	                }

	                if (!inRecipe)
	                {
	                    return false;
	                }
	            }
	        }

	        return required.isEmpty();
	    }
	}
}
