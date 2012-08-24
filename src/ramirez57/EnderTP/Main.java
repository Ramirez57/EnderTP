package ramirez57.EnderTP;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventPriority;

public class Main extends JavaPlugin implements Listener {

	Server mc;
	Logger logger;
	JavaPlugin plugin;
	FileConfiguration config;
	Block block;
	Sign sign;
	Material material;
	Location loc;
	Player player;
	FileConfiguration teleports;
	File teleportsFile;
	File configFile;
	FixedMetadataValue metastub;
	String signature;
	String szNoPermission;
	byte al;
	
	public void onEnable() {
		signature = ChatColor.GREEN + "[EnderTP]";
		mc = Bukkit.getServer();
		logger = this.getLogger();
		plugin = this;
		config = this.getConfig();
		configFile = new File(getDataFolder(),"config.yml");
		config.options().copyDefaults(true);
		mc.getPluginManager().registerEvents(this, this);
		metastub = new FixedMetadataValue(this,1);
		szNoPermission = ChatColor.RED + "No permission to use this.";
		teleportsFile = new File(getDataFolder(),"TELEPORT");
		teleports = YamlConfiguration.loadConfiguration(teleportsFile);
		this.saveConfig();
		saveteleports();
	}
	
	public void onDisable() {
		
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
    	boolean isPlayer = sender instanceof Player;
    	if(isPlayer) sender = (Player) sender;
    	if(cmd.getName().equalsIgnoreCase("endertp")) {
    		if(args.length < 1) {
    			return false;
    		}
    		if(args[0].equalsIgnoreCase("reload")) {
    			config = YamlConfiguration.loadConfiguration(configFile);
    			reloadteleports();
    			logger.info("Configurations reloaded.");
    			if(isPlayer) sender.sendMessage(ChatColor.GREEN + "Configurations reloaded.");
    			return true;
    		}
    	}
    	return false;
    }
	
	public void saveteleports() {
		try {
			teleports.save(teleportsFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return;
	}
	
	public void reloadteleports() {
		teleports = YamlConfiguration.loadConfiguration(teleportsFile);
		return;
	}
	
	public void mark(Block _block, String _str) {
		_block.setMetadata(_str, metastub);
		return;
	}
	
	public boolean hasmark(Block _block, String _str) {
		return _block.hasMetadata(_str);
	}
	
	public int getBMType(Block block) {
		if(block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN) {
			int _x = 0;
			int _y = 0;
			int _z = 0;
			switch(block.getData()) {
		    case 2:
		    	_z=1;
		    	break;
		    case 3:
		    	_z=-1;
		    	break;
		    case 4:
		    	_x=1;
		    	break;
		    case 5:
		    	_x=-1;
		    	break;
		    default:
		    	return -1;
		    }
			if (teleporter_detect(block.getRelative(_x,_y,_z))) return 1;
	    	if (homeport_detect(block.getRelative(_x,_y,_z))) return 0;
	    	if (baseport_detect(block.getRelative(_x,_y,_z))) return 2;
		}
		return -1;
	}
	
	public void teleporter_create(Block _block, SignChangeEvent event, Player _placer) {
		//_block.setMetadata("teleports", new FixedMetadataValue(this, _placer.getDisplayName()));
		event.setLine(0, signature);
		event.setLine(1,"Teleport to");
		event.setLine(2,"spawn.");
		event.setLine(3,"");
		return;
	}
	
	public boolean teleporter_detect(Block _block) {
		if(_block.getType() == Material.matchMaterial(config.getString("spawn_material")) && _block.getRelative(0,-1,0).getType() == Material.matchMaterial(config.getString("ender_material"))) {
			return true;
		}
		return false;
	}
	
	public void homeport_create(Block _block, SignChangeEvent event, Player _placer) {
		//_block.setMetadata("teleporth", new FixedMetadataValue(this, _placer.getDisplayName()));
		event.setLine(0, signature);
		event.setLine(1,"Teleport to");
		event.setLine(2,"your home.");
		event.setLine(3,"");
		return;
	}
	
	public boolean homeport_detect(Block _block) {
		if(_block.getType() == Material.matchMaterial(config.getString("home_material")) && _block.getRelative(0,-1,0).getType() == Material.matchMaterial(config.getString("ender_material"))) {
			return true;
		}
		return false;
	}
	
	public boolean baseport_exists(String _basename) {
		String basename;
		if(config.getBoolean("case_sensitive_bases")) basename = _basename; else basename = _basename.toLowerCase();
		if(!teleports.isConfigurationSection(basename)) return false;
		Block _checkblk = mc.getWorld(teleports.getString(basename + ".world")).getBlockAt(teleports.getInt(basename + ".x"),teleports.getInt(basename + ".y"),teleports.getInt(basename + ".z"));
		if(_checkblk.getType() == Material.SIGN_POST || _checkblk.getType() == Material.WALL_SIGN) {
			if(getBMType(_checkblk) == 2) {
				Sign sign = (Sign)_checkblk.getState();
				return sign.getLine(2).equalsIgnoreCase(basename);
			}
		}
		return false;
	}
	
	public void baseport_create(Block _block, SignChangeEvent event, Player _placer) {
		String _basename = event.getLine(0);
		String basename;
		if(config.getBoolean("case_sensitive_bases")) basename = _basename; else basename = _basename.toLowerCase();
		if(_basename.isEmpty()) {
			player.sendMessage("Please enter the name of the base on the first line of the sign.");
			return;
		}
		if(teleports.isConfigurationSection(basename)) {
			if(baseport_exists(_basename)) {
				event.setLine(0, signature);
				event.setLine(1, "Teleport to");
				event.setLine(2,_basename);
				event.setLine(3,"");
			} else {
				baseport_create2(_basename, _block, event);
				return;
			}
		} else {
			baseport_create2(_basename, _block, event);
			return;
		}
		return;
	}
	
	public void baseport_create2(String _basename, Block _block, SignChangeEvent event) {
		String basename = "";
		if(config.getBoolean("case_sensitive_bases")) basename = _basename; else basename = _basename.toLowerCase();
		teleports.createSection(basename);
		teleports.set(basename + ".x", _block.getX());
		teleports.set(basename + ".y", _block.getY());
		teleports.set(basename + ".z", _block.getZ());
		teleports.set(basename + ".world", _block.getWorld().getName());
		saveteleports();
		event.setLine(0, signature);
		event.setLine(1, "Base of");
		event.setLine(2,_basename);
		event.setLine(3,"");
		return;
	}
	
	public boolean baseport_detect(Block _block) {
		if(_block.getType() == Material.matchMaterial(config.getString("base_material")) && _block.getRelative(0,-1,0).getType() == Material.matchMaterial(config.getString("ender_material"))) {
			return true;
		}
		return false;
	}
	
	@EventHandler
	public void block_break(BlockBreakEvent event) {
		player = event.getPlayer();
		block = event.getBlock();
		if(block == null) return;
	}
	
	@EventHandler
	public void block_place(BlockPlaceEvent event) {
		player = event.getPlayer();
		block = event.getBlock();
		if(block == null) return;
		Block _block = event.getBlockAgainst();
		Material _type = event.getBlockAgainst().getType();
		if(_type == Material.SIGN_POST || _type == Material.WALL_SIGN) {
			if(getBMType(_block) > -1) {
				event.setCancelled(true);
				return;
			}
		}
		if(block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN) {
			int _x = 0;
			int _z = 0;
			int _y = 0;
			switch(block.getData()) {
		    case 2:
		    	_z = 1;
		    	break;
		    case 3:
		    	_z = -1;
		    	break;
		    case 4:
		    	_x = 1;
		    	break;
		    case 5:
		    	_x = -1;
		    	break;
		    default:
		    	break;
		    }
			if (teleporter_detect(block.getRelative(_x,_y,_z))) mark(block, "teleports");
	    	if (homeport_detect(block.getRelative(_x,_y,_z))) mark(block, "teleporth");
	    	if (baseport_detect(block.getRelative(_x,_y,_z))) mark(block,"baseport");
			return;
		}
	}
	
	@EventHandler
	public void endertp_use(PlayerInteractEvent event) {
		player = event.getPlayer();
		block = event.getClickedBlock();
		int _bmtype = 0;
		if(block == null) return;
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			_bmtype = getBMType(block);
			switch(_bmtype) {
			case 0:
				if(player.hasPermission("endertp.use.home")) {
					if(player.getBedSpawnLocation() == null || player.getBedSpawnLocation().getBlock().getType() != Material.BED_BLOCK) {
						player.sendMessage("You have not slept in a bed or it has been destroyed.");
						break;
					}
					player.teleport(player.getBedSpawnLocation().add(0.5,1,0.5));
				} else {
					player.sendMessage(szNoPermission);
				}
				break;
			case 1:
				if(player.hasPermission("endertp.use.spawn")) {
					Location _loc = player.getWorld().getSpawnLocation();
					while(_loc.getBlock().getType() != Material.AIR || _loc.getBlock().getRelative(0,1,0).getType() != Material.AIR) {
						_loc.add(0,1,0);
					}
					player.teleport(_loc.add(0.5,0,0.5));
				} else {
					player.sendMessage(szNoPermission);
				}
				break;
			case 2:
				if(!player.hasPermission("endertp.use.base")) {
					player.sendMessage(szNoPermission);
					break;
				}
				Sign sign = (Sign)block.getState();
				String _basename = sign.getLine(2);
				if(!config.getBoolean("case_sensitive_bases")) _basename = _basename.toLowerCase();
				if(baseport_exists(_basename)) {
					Location _loc = new Location(mc.getWorld(teleports.getString(_basename + ".world")),teleports.getDouble(_basename + ".x")+0.5,teleports.getDouble(_basename + ".y"),teleports.getDouble(_basename + ".z")+0.5);
					player.teleport(_loc);
				} else {
					player.sendMessage("Base " + _basename + " no longer exists");
				}
				break;
			default:
				break;
			}
			return;
		}
		return;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void signplace(SignChangeEvent event) {
		block = event.getBlock();
		player = event.getPlayer();
		if(hasmark(block, "teleports")) teleporter_create(block,event,player);
		if(hasmark(block, "teleporth")) homeport_create(block,event,player);
		if(hasmark(block, "baseport")) baseport_create(block,event,player);
	}
}
