package de.selebrator.orehighlighter;

import de.selebrator.orehighlighter.reflection.ServerPackage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginMain extends JavaPlugin implements Listener, CommandExecutor {

	private static final String PERMISSION_PARENT = "ore.";
	public static final String VERSION = "v1_11_R1";

	private static final Map<Material, Glow.GlowingColor> oreMap = new HashMap<>();
	private Map<Player, List<FakeShulker>> players = new HashMap<>();

	static {
		oreMap.put(Material.COAL_ORE, Glow.GlowingColor.DARK_GRAY);
		oreMap.put(Material.IRON_ORE, Glow.GlowingColor.GRAY);
		oreMap.put(Material.GOLD_ORE, Glow.GlowingColor.YELLOW);
		oreMap.put(Material.DIAMOND_ORE, Glow.GlowingColor.AQUA);
		oreMap.put(Material.REDSTONE_ORE, Glow.GlowingColor.DARK_RED);
		oreMap.put(Material.GLOWING_REDSTONE_ORE, Glow.GlowingColor.DARK_RED);
		oreMap.put(Material.LAPIS_ORE, Glow.GlowingColor.DARK_BLUE);
		oreMap.put(Material.EMERALD_ORE, Glow.GlowingColor.DARK_GREEN);
		oreMap.put(Material.QUARTZ_ORE, Glow.GlowingColor.WHITE);
	}

	@Override
	public void onEnable() {
		if(!ServerPackage.getVersion().equals(VERSION)) {
			this.getLogger().warning("Server version: " + ServerPackage.getVersion() + ", Recommended version: " + VERSION);
			//Bukkit.getPluginManager().disablePlugin(this);
			//return;
		}

		Bukkit.getPluginManager().registerEvents(this, this);
		getCommand("ore").setExecutor(this);
		Bukkit.getOnlinePlayers().forEach(player -> this.players.put(player, new ArrayList<>()));
	}

	@Override
	public void onDisable() {
		Bukkit.getOnlinePlayers().forEach(this::undoSpelunking);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player target = null;

		if(args.length == 0) {
			if(!checkPermission(sender, PERMISSION_PARENT + "self"))
				return true;

			if(sender instanceof Player)
				target = (Player) sender;
			else if(sender instanceof ConsoleCommandSender) {
				sender.sendMessage("You must specify which player you wish to perform this action on");
				return true;
			} else {
				sender.sendMessage("§c/ore is not supported by this CommandSender");
				return true;
			}
		} else if(args.length == 1) {
			if(!checkPermission(sender, PERMISSION_PARENT + "other"))
				return true;

			target = Bukkit.getPlayer(args[0]);
			if(target == null) {
				sender.sendMessage("§cPlayer " + args[0].toLowerCase() + " not online");
				return true;
			}
		} else if(args.length > 1) {
			return false;
		}

		if(target == null) {
			sender.sendMessage("§cPlayer not found");
			return true;
		}

		if(this.players.get(target).equals(new ArrayList<>())) {
			doSpelunking(target, 16);
			return true;
		} else {
			undoSpelunking(target);
			return  true;
		}
	}

	private void initPlayer(Player player) {
		if(!this.players.containsKey(player)) {
			Glow.initTeams(player);
			this.players.put(player, new ArrayList<>());
		}
	}

	private void doSpelunking(Player player, int range) {
		this.initPlayer(player);
		Block playerBlock = player.getLocation().getBlock();
		for(int x = -range; x < range; x++) {
			for(int y = -range; y < range; y++) {
				for(int z = -range; z < range; z++) {
					Block block = playerBlock.getRelative(x, y, z);
					addBlock(player, block.getLocation(), block.getType());
				}
			}
		}
	}

	private void undoSpelunking(Player player) {
		this.players.get(player).forEach(FakeShulker::despawn);
		this.players.put(player, new ArrayList<>());
	}

	private boolean addBlock(Player player, Location location, Material blockType) {
		if(oreMap.containsKey(blockType) && player.hasPermission(PERMISSION_PARENT + "see." + blockType)) {
			FakeShulker shulker = new FakeShulker();
			shulker.spawn(player, location);
			shulker.setGlowColor(oreMap.get(blockType));
			this.players.get(player).add(shulker);
			return true;
		}
		return false;
	}

	private boolean removeBlock(Player player, Location location) {
		List<FakeShulker> shulkers = this.players.get(player);
		for(FakeShulker shulker : shulkers) {
			if(location.equals(shulker.location)) {
				shulker.despawn();
				shulkers.remove(shulker);
				return true;
			}
		}
		return false;
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		this.initPlayer(event.getPlayer());
	}

	@EventHandler
	public void onLeave(PlayerQuitEvent event) {
		this.players.remove(event.getPlayer());
	}

	@EventHandler
	public void onBlockMine(BlockBreakEvent event) {
		Bukkit.getOnlinePlayers().forEach(player -> removeBlock(player, event.getBlock().getLocation()));
	}

	@EventHandler
	public void onBlockExplode(EntityExplodeEvent event) {
		Bukkit.getOnlinePlayers().forEach(player -> event.blockList().forEach(block -> removeBlock(player, block.getLocation())));
	}

	@EventHandler
	public void onPistonPush(BlockPistonExtendEvent event) {
		Bukkit.getOnlinePlayers().forEach(player -> event.getBlocks().forEach(block -> removeBlock(player, block.getLocation())));
	}

	@EventHandler
	public void onPistonPull(BlockPistonRetractEvent event) {
		if(event.isSticky())
			Bukkit.getOnlinePlayers().forEach(player -> event.getBlocks().forEach(block -> removeBlock(player, block.getLocation())));
	}


	private static boolean checkPermission(CommandSender sender, String permission) {
		return checkPermission(sender, permission, "§cYou don't have the permission to perform this command.");
	}

	private static boolean checkPermission(CommandSender sender, String permission, String message) {
		if(sender.hasPermission(permission))
			return true;
		else {
			sender.sendMessage(message);
			return false;
		}
	}
}
