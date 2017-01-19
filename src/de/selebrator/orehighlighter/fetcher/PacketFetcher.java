package de.selebrator.orehighlighter.fetcher;

import de.selebrator.orehighlighter.Glow;
import de.selebrator.orehighlighter.reflection.ConstructorAccessor;
import de.selebrator.orehighlighter.reflection.FieldAccessor;
import de.selebrator.orehighlighter.reflection.MethodAccessor;
import de.selebrator.orehighlighter.reflection.Reflection;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PacketFetcher {
	//Minecraft classes
	private static final Class<?> CLASS_PacketPlayOutEntityDestroy = Reflection.getMinecraftClass("PacketPlayOutEntityDestroy");
	private static final Class<?> CLASS_PacketPlayOutRelEntityMove = Reflection.getMinecraftClass("PacketPlayOutEntity$PacketPlayOutRelEntityMove");
	private static final Class<?> CLASS_PacketPlayOutSpawnEntityLiving = Reflection.getMinecraftClass("PacketPlayOutSpawnEntityLiving");
	private static final Class<?> CLASS_PacketPlayOutScoreboardTeam = Reflection.getMinecraftClass("PacketPlayOutScoreboardTeam");
	private static final Class<?> CLASS_EntityPlayer = Reflection.getMinecraftClass("EntityPlayer");
	private static final Class<?> CLASS_PlayerConnection = Reflection.getMinecraftClass("PlayerConnection");
	private static final Class<?> CLASS_Packet = Reflection.getMinecraftClass("Packet");

	//CraftBukkit classes
	private static final Class<?> CLASS_CraftPlayer = Reflection.getCraftBukkitClass("entity.CraftPlayer");

	private static final ConstructorAccessor<Object> CONSTRUCTOR_PacketPlayOutEntityDestroy = Reflection.getConstructor(CLASS_PacketPlayOutEntityDestroy);
	private static final ConstructorAccessor<Object> CONSTRUCTOR_PacketPlayOutRelEntityMove = Reflection.getConstructor(CLASS_PacketPlayOutRelEntityMove);
	private static final ConstructorAccessor<Object> CONSTRUCTOR_PacketPlayOutSpawnEntityLiving = Reflection.getConstructor(CLASS_PacketPlayOutSpawnEntityLiving);
	private static final ConstructorAccessor<Object> CONSTRUCTOR_PacketPlayOutScoreboardTeam = Reflection.getConstructor(CLASS_PacketPlayOutScoreboardTeam);

	private static final MethodAccessor METHOD_CraftPlayer_getHandle = Reflection.getMethod(CLASS_CraftPlayer, CLASS_EntityPlayer, "getHandle");
	private static final MethodAccessor METHOD_PlayerConnection_sendPacket = Reflection.getMethod(CLASS_PlayerConnection, null, "sendPacket", CLASS_Packet);

	private static final FieldAccessor FIELD_EntityPlayer_playerConnection = Reflection.getField(CLASS_EntityPlayer, CLASS_PlayerConnection, "playerConnection");


	public static Object spawnEntityLiving(int entityId, UUID uuid, Location location, Object dataWatcher) {
		Map<String, Object> fields = new HashMap<>();
		fields.put("a", entityId);
		fields.put("b", uuid);
		fields.put("c", 69); //RegistryID of entityType
		fields.put("d", location.getX() + 0.5D);
		fields.put("e", location.getY());
		fields.put("f", location.getZ() + 0.5D);
		fields.put("g", 0); //motX
		fields.put("h", 0); //motY
		fields.put("i", 0); //motZ
		fields.put("j", (byte) 0); //yaw
		fields.put("k", (byte) 0); //pitch
		fields.put("l", (byte) 0); //???
		fields.put("m", dataWatcher);
		return packet(CONSTRUCTOR_PacketPlayOutSpawnEntityLiving, fields);
	}

	public static Object entityDestroy(int entityId) {
		Map<String, Object> fields = new HashMap<>();
		fields.put("a", new int[] { entityId });
		return packet(CONSTRUCTOR_PacketPlayOutEntityDestroy, fields);
	}

	public static Object relEntityMove(int entityId, double x, double y, double z) {
		Map<String, Object> fields = new HashMap<>();
		fields.put("a", entityId);
		fields.put("b", rel(x));
		fields.put("c", rel(y));
		fields.put("d", rel(z));
		fields.put("g", true); //onGround
		fields.put("h", true);
		return packet(CONSTRUCTOR_PacketPlayOutRelEntityMove, fields);
	}

	public static Object scoreboardTeam(Glow.GlowingColor color, String nameTagVisibility, String collisionRule, Collection<String> members, Glow.ScoreboardTeamOperation mode) {
		Map<String, Object> fields = new HashMap<>();
		fields.put("a", color.teamName); //Team name
		fields.put("b", color.teamName); //Team displayName
		fields.put("c", "§" + color.colorCode); //Team prefix
		fields.put("d", ""); //Team suffix
		fields.put("e", nameTagVisibility); //Team nameTagVisibility (EnumNameTagVisibility.e)
		fields.put("f", collisionRule); //Team collisionRule (EnumTeamPush.e)
		fields.put("g", color.id); //Team color (EnumChatFormat.C)
		fields.put("h", members);
		fields.put("i", mode.id); //Mode (0 = create team; 1 = remove team; 2 = update info; 3 = add players; remove players
		fields.put("j", 0); //not sure what this does :/
		return packet(CONSTRUCTOR_PacketPlayOutScoreboardTeam, fields);
	}

	@SuppressWarnings("unchecked")
	public static Object packet(ConstructorAccessor<Object> constructorAccessor, Map<String, Object> values) {
		Object packet = constructorAccessor.newInstance();
		values.forEach((fieldName, value) -> Reflection.getField(packet.getClass(), fieldName).set(packet, value));
		return packet;
	}


	public static void sendPackets(Player player, Object... packets) {
		for(Object packet : packets) {
			METHOD_PlayerConnection_sendPacket.invoke(FIELD_EntityPlayer_playerConnection.get(METHOD_CraftPlayer_getHandle.invoke(player)), packet);
		}
	}

	/**
	 * prepare relative coordinate for packet
	 */
	private static short rel(double value) {
		return (short) (4096 * value);
	}
}
