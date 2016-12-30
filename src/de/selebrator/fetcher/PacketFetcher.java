package de.selebrator.fetcher;

import de.selebrator.Glow;
import de.selebrator.reflection.Reflection;
import net.minecraft.server.v1_11_R1.DataWatcher;
import net.minecraft.server.v1_11_R1.Packet;
import net.minecraft.server.v1_11_R1.PacketPlayOutEntity;
import net.minecraft.server.v1_11_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_11_R1.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_11_R1.PacketPlayOutSpawnEntityLiving;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

public class PacketFetcher {
	public static PacketPlayOutSpawnEntityLiving spawnEntityLiving(int entityId, UUID uuid, Location location, DataWatcher dataWatcher) {
		PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving();
		Reflection.getField(packet.getClass(), "a").set(packet, entityId);
		Reflection.getField(packet.getClass(), "b").set(packet, uuid);
		Reflection.getField(packet.getClass(), "c").set(packet, 69); //RegistryID of entityType
		Reflection.getField(packet.getClass(), "d").set(packet, location.getX() + 0.5D);
		Reflection.getField(packet.getClass(), "e").set(packet, location.getY());
		Reflection.getField(packet.getClass(), "f").set(packet, location.getZ() + 0.5D);
		Reflection.getField(packet.getClass(), "g").set(packet, 0); //motX
		Reflection.getField(packet.getClass(), "h").set(packet, 0); //motY
		Reflection.getField(packet.getClass(), "i").set(packet, 0); //motZ
		Reflection.getField(packet.getClass(), "j").set(packet, (byte) 0); //yaw
		Reflection.getField(packet.getClass(), "k").set(packet, (byte) 0); //pitch
		Reflection.getField(packet.getClass(), "l").set(packet, (byte) 0); //???
		Reflection.getField(packet.getClass(), "m").set(packet, dataWatcher);

		return packet;
	}

	public static PacketPlayOutEntityDestroy entityDestroy(int entityId) {
		PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy();
		Reflection.getField(packet.getClass(), "a").set(packet, new int[] { entityId });
		return packet;
	}

	public static PacketPlayOutEntity.PacketPlayOutRelEntityMove relEntityMove(int entityId, double x, double y, double z) {
		PacketPlayOutEntity.PacketPlayOutRelEntityMove packet = new PacketPlayOutEntity.PacketPlayOutRelEntityMove();
		Reflection.getField(packet.getClass().getSuperclass(), "a").set(packet, entityId);
		Reflection.getField(packet.getClass().getSuperclass(), "b").set(packet, rel(x));
		Reflection.getField(packet.getClass().getSuperclass(), "c").set(packet, rel(y));
		Reflection.getField(packet.getClass().getSuperclass(), "d").set(packet, rel(z));
		Reflection.getField(packet.getClass().getSuperclass(), "g").set(packet, true); //onGround
		Reflection.getField(packet.getClass().getSuperclass(), "h").set(packet, true);
		return packet;
	}

	public static PacketPlayOutScoreboardTeam scoreboardTeam(Glow.GlowingColor color, String nameTagVisibility, String collisionRule, Collection<String> members, Glow.ScoreboardTeamOperation mode) {
		PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();
		Reflection.getField(packet.getClass(), "a").set(packet, color.teamName); //Team name
		Reflection.getField(packet.getClass(), "b").set(packet, color.teamName); //Team displayName
		Reflection.getField(packet.getClass(), "c").set(packet, "§" + color.colorCode); //Team prefix
		Reflection.getField(packet.getClass(), "d").set(packet, ""); //Team suffix
		Reflection.getField(packet.getClass(), "e").set(packet, nameTagVisibility); //Team nameTagVisibility (EnumNameTagVisibility.e)
		Reflection.getField(packet.getClass(), "f").set(packet, collisionRule); //Team collisionRule (EnumTeamPush.e)
		Reflection.getField(packet.getClass(), "g").set(packet, color.id); //Team color (EnumChatFormat.C)
		Reflection.getField(packet.getClass(), "h").set(packet, members);
		Reflection.getField(packet.getClass(), "i").set(packet, mode.id); //Mode (0 = create team; 1 = remove team; 2 = update info; 3 = add players; remove players
		Reflection.getField(packet.getClass(), "j").set(packet, 0); //not sure what this does :/

		return packet;
	}

	public static void sendPackets(Player player, Packet<?>... packets) {
		for(Packet<?> packet : packets) {
			((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
		}
	}

	/**
	 * prepare relative coordinate for packet
	 */
	private static short rel(double value) {
		return (short) (4096 * value);
	}
}
