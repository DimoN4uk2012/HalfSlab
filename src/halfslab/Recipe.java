
package halfslab;

import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class Recipe {
    
    private final HalfSlab plugin;
    
    Recipe(HalfSlab plugin) {
        this.plugin = plugin;
    }
    
    public static void addRecipeSlab(int minecraft, Map<String, Boolean> recipeList){
//stone
        if (recipeList.get("stone")){
            ShapedRecipe stone = new ShapedRecipe(new ItemStack(Material.STONE));
            stone.shape("SS");
            stone.setIngredient('S', Material.STEP);
            Bukkit.getServer().addRecipe(stone);
        }
//sandstone
        if (recipeList.get("sandstone")){
            ShapedRecipe sandstone = new ShapedRecipe(new ItemStack(Material.SANDSTONE));
            sandstone.shape("SS");
            sandstone.setIngredient('S', Material.STEP, 1);
            Bukkit.getServer().addRecipe(sandstone);
        }
//cobblestone
        if (recipeList.get("cobblestone")){
            ShapedRecipe cobblestone = new ShapedRecipe(new ItemStack(Material.COBBLESTONE));
            cobblestone.shape("SS");
            cobblestone.setIngredient('S', Material.STEP, 3);
            Bukkit.getServer().addRecipe(cobblestone);
        }
//brick
        if (recipeList.get("brick")){
            ShapedRecipe brick = new ShapedRecipe(new ItemStack(Material.BRICK));
            brick.shape("SS");
            brick.setIngredient('S', Material.STEP, 4);
            Bukkit.getServer().addRecipe(brick);
        }
//smothbrick
        if (recipeList.get("smothbrick")){
            ShapedRecipe smothbrick = new ShapedRecipe(new ItemStack(Material.SMOOTH_BRICK));
            smothbrick.shape("SS");
            smothbrick.setIngredient('S', Material.STEP, 5);
            Bukkit.getServer().addRecipe(smothbrick);
        }
//netherbrick
        if (recipeList.get("netherbrick")){
            ShapedRecipe netherbrick = new ShapedRecipe(new ItemStack(Material.NETHER_BRICK));
            netherbrick.shape("SS");
            netherbrick.setIngredient('S', Material.STEP, 6);
            Bukkit.getServer().addRecipe(netherbrick);
        }
        if (minecraft >= 5){
//quartz
            if (recipeList.get("quartz")){
                ShapedRecipe quartz = new ShapedRecipe(new ItemStack(Material.QUARTZ_BLOCK));
                quartz.shape("SS");
                quartz.setIngredient('S', Material.STEP, 7);
                Bukkit.getServer().addRecipe(quartz);
            }
        }
        if (minecraft >= 8){
//redsendstone
            if (recipeList.get("redsendstone")){
                ShapedRecipe redsendstone = new ShapedRecipe(new ItemStack(Material.RED_SANDSTONE));
                redsendstone.shape("SS");
                redsendstone.setIngredient('S', Material.STONE_SLAB2);
                Bukkit.getServer().addRecipe(redsendstone);
            }
        }
        if (minecraft >= 9){
//purpurblock
            if (recipeList.get("purpurblock")){
                ShapedRecipe purpurblock = new ShapedRecipe(new ItemStack(Material.PURPUR_BLOCK));
                purpurblock.shape("SS");
                purpurblock.setIngredient('S', Material.PURPUR_SLAB);
                Bukkit.getServer().addRecipe(purpurblock);
            }
        }
        if (minecraft >= 7){
//wood
            if (recipeList.get("wood")){
                int i = 0;
                while (i < 6){
                    ShapedRecipe wood = new ShapedRecipe(new ItemStack(Material.WOOD, 1,(short) i));
                    wood.shape("SS");
                    wood.setIngredient('S', Material.WOOD_STEP, i);
                    Bukkit.getServer().addRecipe(wood);
                    i++;
                }
            }
        }
    }
    
}
