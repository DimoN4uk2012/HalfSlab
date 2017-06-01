package halfslab;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javafx.geometry.Point3D;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import static org.bukkit.event.EventPriority.HIGHEST;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class HalfSlab extends JavaPlugin implements Listener {
    
    int version = 2;
    int minecraft = 0;
    //Крок перевірки пересічення вектора з заданим блоком
    //Дане значення впливає на продуктивність, чим менше тим точніше, але більше навантаження
    double step = 0.05;
    
    boolean dontFallPlayer = true;
    boolean needSilkTouch = true;
    
    boolean useShiftHalfSlab = true;
    boolean useShiftSmothSlab = true;
    boolean useShiftToSlab = true;
    
    boolean needPermissionHalfSlab = false;
    boolean needPermissionSmothSlab = false;
    boolean needPermissionToSlab = false;
    
    boolean enableHalfSlab = true;
    boolean enableSmothSlab = true;
    boolean enableRecipe = true;
    boolean enableToSlab = true;
    
    boolean compareToBlock = false;
    
    Map<String, Boolean> recipeList = new HashMap<>();
    
    
    @Override
    public void onEnable(){
        Bukkit.getPluginManager().registerEvents(this, this);
        this.minecraft = this.serverVersion();
        Bukkit.getLogger().info("[HalfSlab] Detect Minecraft VERSION - 1." + this.minecraft);
        this.reloadConfigPlugin();
        
        if (this.enableRecipe){
            Recipe.addRecipeSlab(this.minecraft, this.recipeList);
        }
    }
    
    private int serverVersion(){
        String v = Bukkit.getVersion();
        String[] ve = v.split("MC: ");
        String ver = ve[1];
        String vers = ver.replace(")", "");
        String[] versi = vers.split("\\.");
        String versio = versi[1];
        return Integer.parseInt(versio);
    }
    
    @Override
    public void onDisable(){
        Bukkit.getLogger().info("[HalfSlab] Plugin - Disable.");
    }
    
    @EventHandler(priority=HIGHEST, ignoreCancelled=true)
    public void blockPlaceEvent(BlockPlaceEvent event){
        Block block = event.getBlockPlaced();
        Material material = block.getType();
        if (this.enableSmothSlab && 
                material.toString().contains("DOUBLE_") && 
                !material.toString().equals("DOUBLE_PLANT")){
            Player player = event.getPlayer();
            if (!this.useShiftSmothSlab || player.isSneaking()){
                if (!this.needPermissionSmothSlab || player.hasPermission("halfslab.player.placesmoth")){
                    byte blockData = block.getData();
                    if (material.equals(Material.DOUBLE_STEP) && blockData <= 1){
                        block.setData( (byte) (blockData + 8));
                    }else
                    if (this.minecraft >= 8 && material.toString().equals("DOUBLE_STONE_SLAB2") && blockData == 0){
                        block.setData( (byte) (blockData + 8));
                    }
                }
            } 
        }
    }      
            
    @EventHandler(priority=HIGHEST, ignoreCancelled=true)
    public void blockBreakEvent(BlockBreakEvent event){
        Block block = event.getBlock();
        Material material = block.getType();
        if (this.enableHalfSlab && 
                material.toString().contains("DOUBLE_") && 
                !material.toString().equals("DOUBLE_PLANT")){
            Player player = event.getPlayer();
            if (!this.useShiftHalfSlab || player.isSneaking()){
                if (!this.needPermissionHalfSlab || player.hasPermission("halfslab.player.breakhalf")){
                    event.setCancelled(true);
                    //Координати блока
                    Point3D blockPoint = new Point3D(block.getX(), block.getY(), block.getZ());
                    Point3D blockCenterPoint = blockPoint.add(0.5, 0.5, 0.5);
                    //Інші змінні
                    BlockFace face = BlockFace.UP;

                    Location eye = player.getEyeLocation();
                    Point3D eyePoint = new Point3D(eye.getX(), eye.getY(), eye.getZ());

                    Vector vector = eye.getDirection();
                    //Направлення вектора
                    double vX = vector.getX();
                    double vY = vector.getY();
                    double vZ = vector.getZ();
                    //Дистанція від голови до центра блока
                    double distance = this.distanceToBlock(blockCenterPoint, eyePoint);
                    Point3D pos = eyePoint;
                    if (    (distance - 
                                this.distanceToBlock(blockCenterPoint, pos.add(this.step * vX, this.step * vY, this.step * vZ))
                            ) > 0){
                        if (distance > 0.7){
                            double dis = distance - 0.7;
                            pos.add(dis * vX, dis * vY, dis * vZ);
                        }
                        int i = 0;
                        while (!this.inBlock(blockPoint, pos) && i < 1.5/this.step){
                            i++;
                            pos = pos.add(this.step * vX, this.step * vY, this.step * vZ);
                        }
                        double blockY = block.getY() + 0.5;
                        if (pos.getY() >= blockY){
                            face = BlockFace.UP;
                        }else{
                            face = BlockFace.DOWN;
                        }
                    }else{
                        face = BlockFace.UP;
                    }
                    byte dataMaterial = block.getData();
                    String materialName = material.toString().replaceAll("DOUBLE_", "");
                    Material newMaterial = Material.valueOf(materialName);
                    if (!player.getGameMode().equals(GameMode.CREATIVE)){
                        ItemStack hand; 
                        if (this.minecraft >= 9){
                            hand = player.getInventory().getItemInMainHand();
                        }else{
                            hand = player.getItemInHand();
                        }
                        Collection<ItemStack> drops = block.getDrops(hand);
                        if (drops != null && drops.size() > 0){
                            for (ItemStack drop : drops){
                                drop.setAmount(1);
                                Location blockLocationCenter = block.getLocation().add(0.5, 0.6, 0.5);
                                block.getWorld().dropItemNaturally(blockLocationCenter, drop);
                                break;
                            }
                        }
                    }
                    block.setType(newMaterial);
                    if (face.equals(BlockFace.UP)){
                        if (dataMaterial < 8){
                            block.setData(dataMaterial);
                        }else{
                            block.setData( (byte) (dataMaterial - 8));
                        }
                    }else{
                        if (dataMaterial < 8){
                            block.setData( (byte) (dataMaterial + 8));
                        }else{
                            block.setData(dataMaterial);
                        }
                        Location playerLocation = player.getLocation();
                        Point3D playerPoint = new Point3D(playerLocation.getX(), playerLocation.getY(), playerLocation.getZ());
                        Point3D blockPointUp = blockPoint.add(0.5, 1, 0.5);
                        if (this.dontFallPlayer && (blockPoint.getY() + 0.99) < playerPoint.getY() && 
                                this.distanceToBlock(blockPointUp, playerPoint) < 1.14){
                            player.teleport(player.getLocation());
                        }
                    }
                }
            }
        }
    }
    
    public double distanceToBlock(Point3D blockCenterPoint, Point3D eyePoint){
        //Координати центра блока
        double X = blockCenterPoint.getX();
        double Y = blockCenterPoint.getY();
        double Z = blockCenterPoint.getZ();
        //Координати очей гравця
        double eX = eyePoint.getX();
        double eY = eyePoint.getY();
        double eZ = eyePoint.getZ();
        //Координати цетра блока
        double xy = Math.sqrt(
                        Math.pow((eX - X), 2) 
                                + 
                        Math.pow((eY - Y), 2)
                    );
        double distance = Math.sqrt(
                    Math.pow(xy, 2) 
                            + 
                    Math.pow((eZ - Z), 2)
                );
        return distance;
    }
    
    public boolean inBlock(Point3D blockPoint, Point3D pos){
        //Координати блока
        double bX = blockPoint.getX();
        double bY = blockPoint.getY();
        double bZ = blockPoint.getZ();
        //Координати довільної позиції
        double pX = pos.getX();
        double pY = pos.getY();
        double pZ = pos.getZ();
        //На скільки далеко вектор від блока
        double dX = pX - bX;
        double dY = pY - bY;
        double dZ = pZ - bZ;
        
        //Якщо вектор в середині блоку повертає true
        if ((dX >= 0 && dX <= 1) &&
                (dY >= 0 && dY <= 1) &&
                (dZ >= 0 && dZ <= 1)
                ){
            return true;
        }else{return false;}
    }
    
    private void reloadConfigPlugin(){
        File cfgFile = new File(this.getDataFolder(), "config.yml");
        if (!cfgFile.exists()) {
            this.getConfig().options().copyDefaults(true);
            this.saveDefaultConfig();
            Bukkit.getLogger().info("[HalfSlab] Load Default Config File.");
        }else{
                File cfgFileOld = new File(this.getDataFolder(), "configOld-v" + this.getConfig().getInt("version") + ".yml");
                if (this.version != this.getConfig().getInt("version")){
                    cfgFile.renameTo(cfgFileOld);
                    cfgFile.delete();
                    if (!cfgFile.exists()) {
                        this.getConfig().options().copyDefaults(true);
                        this.saveDefaultConfig();
                        Bukkit.getLogger().info("[HalfSlab] Load Default Config File.");
                    }
                    Bukkit.getLogger().info("[HalfSlab] Load Default Config File. Config version != " + this.version);
                }
            }
        this.reloadConfig();
        if (cfgFile.exists()) {
            FileConfiguration c = this.getConfig();
            
            //Витягує усі параметри з конфігу
            if (c.getDouble("calculateStep") <= 0.2){
                this.step = c.getDouble("calculateStep");
            }else{
                this.step = 0.05;
                Bukkit.getLogger().info("[HalfSlab] ERROR config \"calculateStep:\" > 0.2.");
            }
            
            this.dontFallPlayer = c.getBoolean("dontFallPlayer");
            this.needSilkTouch = c.getBoolean("blockToSlab.silkTouch");
    
            this.useShiftHalfSlab = c.getBoolean("breakHalfSlab.useShift");
            this.useShiftSmothSlab = c.getBoolean("placeSmoothSlab.useShift");
            this.useShiftToSlab = c.getBoolean("blockToSlab.useShift");

            this.needPermissionHalfSlab = c.getBoolean("breakHalfSlab.needPermission");
            this.needPermissionSmothSlab = c.getBoolean("placeSmoothSlab.needPermission");
            this.needPermissionToSlab = c.getBoolean("blockToSlab.needPermission");
            
            this.enableRecipe = c.getBoolean("unCraftSlab.enable");
            this.enableHalfSlab = c.getBoolean("breakHalfSlab.enable");
            this.enableSmothSlab = c.getBoolean("placeSmoothSlab.enable");
            this.enableToSlab = c.getBoolean("blockToSlab.enable");
            this.compareToBlock = c.getBoolean("compareToBlock");
            
            this.recipeList.clear();
            this.recipeList.put("stone", c.getBoolean("unCraftSlab.material.stone"));
            this.recipeList.put("sandstone", c.getBoolean("unCraftSlab.material.sandstone"));
            this.recipeList.put("cobblestone", c.getBoolean("unCraftSlab.material.cobblestone"));
            this.recipeList.put("brick", c.getBoolean("unCraftSlab.material.brick"));
            this.recipeList.put("smothbrick", c.getBoolean("unCraftSlab.material.smothbrick"));
            this.recipeList.put("netherbrick", c.getBoolean("unCraftSlab.material.netherbrick"));
            this.recipeList.put("quartz", c.getBoolean("unCraftSlab.material.quartz"));
            this.recipeList.put("redsendstone", c.getBoolean("unCraftSlab.material.redsendstone"));
            this.recipeList.put("purpurblock", c.getBoolean("unCraftSlab.material.purpurblock"));
            this.recipeList.put("wood", c.getBoolean("unCraftSlab.material.wood"));
            
            Bukkit.getLogger().info("[HalfSlab] Config Reloaded.");
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String com = "halfslab";
        if (sender.hasPermission("halfslab.admin.reload")){
            if (args.length == 1 && command.getName().equals(com) && args[0].equals("reload")){
                this.reloadConfig();
                this.reloadConfigPlugin();
                sender.sendMessage("[HalfSlab] Reloaded.");
                return true;
            }
        }
        return false;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> complitions = new LinkedList<>();
        if (sender.hasPermission("halfslab.admin.reload")){
            if (args.length == 1){
                complitions.add("reload");
            }
        }
        return complitions;
    }
}