package sync.plugin;

import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;





public class Main extends JavaPlugin {

	
	public static boolean hotbar;
	public static Inventory inv;
	private static Main instance;
	public static Main getInstance() {
		return instance;
	}
	

	public void onEnable() {
		instance = this;
		getServer().getPluginManager().registerEvents(new Listeners(this), this);
		inv = Bukkit.createInventory(null, InventoryType.PLAYER, "Shared Inventory");


	}
		

	public void onDisable() {
		
		instance = null;
	}
	
	boolean cando = true;
    @Override
    public boolean onCommand(CommandSender sender,
                             Command command,
                             String label,
                             String[] args) {
        if (command.getName().equalsIgnoreCase("command")) {
            sender.sendMessage("This can be changed");
            return true;
        } 
        //Add commands here
        else if(command.getName().equalsIgnoreCase("toggleHotbar")) {
        	hotbar = hotbar ? false : true;
        }
        
        
        return false;
    }

}
