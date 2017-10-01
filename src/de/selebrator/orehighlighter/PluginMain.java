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
import org.bukkit.configuration.ConfigurationSection;
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

import java.util.*;
import java.util.stream.Collectors;

public class PluginMain extends JavaPlugin implements Listener, CommandExecutor {

	public static final String VERSION = "v1_12_R1";
	private static final String PERMISSION_PARENT = "ore.";
	public int defaultRange;
	public int minRange;
	public int maxRange;
	public int maxBlocks;
	public Map<Material, Glow.GlowingColor> blockColorMap = new HashMap<>();
	public List<Material> defaultBlockTypes;
	private Map<Player, List<FakeShulker>> players = new HashMap<>();

	private static boolean checkPermission(CommandSender sender, String permission) {
		return checkPermission(sender, permission, "§cYou don't have the permission to perform this command.");
	}

	private static boolean checkPermission(CommandSender sender, String permission, String message) {
		if(sender.hasPermission(permission))
			return true;
		else {
			if(!message.equals(""))
				sender.sendMessage(message);
			return false;
		}
	}

	@Override
	public void onEnable() {
		loadConfig();

		if(!ServerPackage.getVersion().equals(VERSION)) {
			this.getLogger().warning("Server version: " + ServerPackage.getVersion() + ", Recommended version: " + VERSION);
			//Bukkit.getPluginManager().disablePlugin(this);
			//return;
		}

		Bukkit.getPluginManager().registerEvents(this, this);
		getCommand("ore").setExecutor(this);
		Bukkit.getOnlinePlayers().forEach(player -> this.players.put(player, new ArrayList<>()));
	}

	private void loadConfig() {
		this.saveDefaultConfig();

		this.defaultRange = this.getConfig().getInt("default_range");
		this.minRange = this.getConfig().getInt("min_range");
		this.maxRange = this.getConfig().getInt("max_range");
		this.maxBlocks = this.getConfig().getInt("max_blocks");

		ConfigurationSection colors = this.getConfig().getConfigurationSection("colors");
		Map<String, Object> BlockColorPairs = colors.getValues(false);
		for(String materialName : BlockColorPairs.keySet()) {
			Material blockType = Material.getMaterial(materialName.toUpperCase());
			char colorCode = ((String) BlockColorPairs.get(materialName)).charAt(1);
			Glow.GlowingColor color = Glow.GlowingColor.BY_COLOR_CODE.get(colorCode);
			this.blockColorMap.put(blockType, color);
		}

		this.defaultBlockTypes = this.getConfig().getStringList("default_blocks").stream()
				.map(String::toUpperCase)
				.map(Material::getMaterial)
				.collect(Collectors.toList());
	}

	@Override
	public void onDisable() {
		Bukkit.getOnlinePlayers().forEach(this::undoSpelunking);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player target = null;
		int range = this.defaultRange;
		List<Material> blockTypes = new ArrayList<>();

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
		}
		if(args.length >= 1) {
			if(!checkPermission(sender, PERMISSION_PARENT + "other"))
				return true;

			target = Bukkit.getPlayer(args[0]);
			if(target == null) {
				sender.sendMessage("§cPlayer §r" + args[0].toLowerCase() + "§c not online");
				return true;
			}
		}
		if(args.length >= 2) {
			range = Integer.parseInt(args[1]);
		}
		if(args.length >= 3) {
			blockTypes = Arrays.asList(args).subList(2, args.length).stream()
					.map(String::toUpperCase)
					.map(Material::getMaterial)
					.collect(Collectors.toList());
		}

		if(target == null) {
			sender.sendMessage("§cPlayer not found");
			return true;
		}

		range = Math.max(this.minRange, Math.min(range, this.maxRange));

		if(this.players.get(target).isEmpty()) {
			doSpelunking(target, range, blockTypes.isEmpty() ? defaultBlockTypes : blockTypes);
			return true;
		} else {
			undoSpelunking(target);
			return true;
		}
	}

	private void initPlayer(Player player) {
		if(!this.players.containsKey(player)) {
			Glow.initTeams(player);
			this.players.put(player, new ArrayList<>());
		}
	}

	private void doSpelunking(Player player, int range, List<Material> blockTypes) {
		this.initPlayer(player);
		Block playerBlock = player.getLocation().getBlock();
		for(int x = -range; x < range; x++) {
			for(int y = -range; y < range; y++) {
				for(int z = -range; z < range; z++) {
					if(players.get(player).size() > this.maxBlocks)
						return;

					Block block = playerBlock.getRelative(x, y, z);
					if(blockTypes.contains(block.getType()))
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
		if(blockColorMap.containsKey(blockType) && checkPermission(player, PERMISSION_PARENT + "see." + blockType, "")) {
			FakeShulker shulker = new FakeShulker();
			shulker.spawn(player, location);
			shulker.setGlowColor(blockColorMap.get(blockType));
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
}
