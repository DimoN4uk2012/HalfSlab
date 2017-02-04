package halfslab;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class HalfSlab extends JavaPlugin implements Listener {
    
    int version = 1;
    
    //Крок перевірки пересічення вектора з заданим блоком
    //Дане значення впливає на продуктивність, чим менше тим точніше, але більше навантаження
    double step = 0.05;
    
    boolean dontFallPlayer = true;
    boolean useShift = true;
    boolean needPermission = false;
    
    @Override
    public void onEnable(){
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getLogger().info("[HalfSlab] Plugin - Enable!");
        this.reloadConfigPlugin();
    }
    
    @Override
    public void onDisable(){
        Bukkit.getLogger().info("[HalfSlab] Plugin - Disable.");
    }
    
    
    @EventHandler(priority=HIGHEST, ignoreCancelled=true)
    public void blockBreakEvent(BlockBreakEvent event){
        Block block = event.getBlock();
        Material material = block.getType();
        if (material.toString().contains("DOUBLE_") && !material.equals(Material.DOUBLE_PLANT)){
            Player player = event.getPlayer();
            if (!this.useShift || player.isSneaking()){
                if (!this.needPermission || player.hasPermission("halfslab.player.break")){
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

                        while (!this.inBlock(blockPoint, pos)){
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
                        Collection<ItemStack> drops = block.getDrops(player.getInventory().getItemInMainHand());
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
                        block.setData(dataMaterial);
                    }else{
                        block.setData( (byte) (dataMaterial + 8));
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
            if (c.getDouble("calculateStep") >= 0.2){
                this.step = c.getDouble("calculateStep");
            }else{
                this.step = 0.05;
                Bukkit.getLogger().info("[HalfSlab] ERROR config \"calculateStep:\" > 0.2.");
            }
            this.dontFallPlayer = c.getBoolean("dontFallPlayer");
            this.useShift = c.getBoolean("useShift");
            this.needPermission = c.getBoolean("needPermission");
            
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