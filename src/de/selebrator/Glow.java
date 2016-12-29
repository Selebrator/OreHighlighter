package de.selebrator;

import com.google.common.collect.Lists;
import de.selebrator.reflection.Reflection;
import net.minecraft.server.v1_11_R1.PacketPlayOutScoreboardTeam;
import org.bukkit.entity.Player;

import java.util.Collection;

public class Glow {

	public static String TEAM_TAG_VISIBILITY = "always";
	public static String COLLISION_RULE = "never";

	public static PacketPlayOutScoreboardTeam createTeam(GlowingColor color) {
		PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();
		Reflection.getField(packet.getClass(), "a").set(packet, color.teamName); //Team name
		Reflection.getField(packet.getClass(), "b").set(packet, color.teamName); //Team displayName
		Reflection.getField(packet.getClass(), "c").set(packet, "§" + color.colorCode); //Team prefix
		Reflection.getField(packet.getClass(), "d").set(packet, ""); //Team suffix
		Reflection.getField(packet.getClass(), "e").set(packet, TEAM_TAG_VISIBILITY); //Team nameTagVisibility (EnumNameTagVisibility.e)
		Reflection.getField(packet.getClass(), "f").set(packet, COLLISION_RULE); //Team collisionRule (EnumTeamPush.e)
		Reflection.getField(packet.getClass(), "g").set(packet, color.id); //Team color (EnumChatFormat.C)
		Reflection.getField(packet.getClass(), "h").set(packet, Lists.newArrayList());
		Reflection.getField(packet.getClass(), "i").set(packet, 0); //Mode (0 = create team; 1 = remove team; 2 = update info; 3 = add players; remove players
		Reflection.getField(packet.getClass(), "j").set(packet, 0); //not sure what this does :/

		return packet;
	}

	public static PacketPlayOutScoreboardTeam add(GlowingColor color, String entityReference) { //entityReference = name for players and uuid for entities
		Collection<String> modifiedEnities = Lists.newArrayList();
		modifiedEnities.add(entityReference);
		PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();
		Reflection.getField(packet.getClass(), "a").set(packet, color.teamName); //Team name
		Reflection.getField(packet.getClass(), "b").set(packet, color.teamName); //Team displayName
		Reflection.getField(packet.getClass(), "c").set(packet, "§" + color.colorCode); //Team prefix
		Reflection.getField(packet.getClass(), "d").set(packet, ""); //Team suffix
		Reflection.getField(packet.getClass(), "e").set(packet, TEAM_TAG_VISIBILITY); //Team nameTagVisibility (EnumNameTagVisibility.e)
		Reflection.getField(packet.getClass(), "f").set(packet, COLLISION_RULE); //Team collisionRule (EnumTeamPush.e)
		Reflection.getField(packet.getClass(), "g").set(packet, color.id); //Team color (EnumChatFormat.C)
		Reflection.getField(packet.getClass(), "h").set(packet, modifiedEnities);
		Reflection.getField(packet.getClass(), "i").set(packet, 3); //Mode (0 = create team; 1 = remove team; 2 = update info; 3 = add players; remove players
		Reflection.getField(packet.getClass(), "j").set(packet, 0); //not sure what this does :/

		return packet;
	}

	public static PacketPlayOutScoreboardTeam remove(GlowingColor color, String entityReference) { //entityReference = name for players and uuid for entities
		Collection<String> modifiedEnities = Lists.newArrayList();
		modifiedEnities.add(entityReference);
		PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();
		Reflection.getField(packet.getClass(), "a").set(packet, color.teamName); //Team name
		Reflection.getField(packet.getClass(), "b").set(packet, color.teamName); //Team displayName
		Reflection.getField(packet.getClass(), "c").set(packet, "§" + color.colorCode); //Team prefix
		Reflection.getField(packet.getClass(), "d").set(packet, ""); //Team suffix
		Reflection.getField(packet.getClass(), "e").set(packet, TEAM_TAG_VISIBILITY); //Team nameTagVisibility (EnumNameTagVisibility.e)
		Reflection.getField(packet.getClass(), "f").set(packet, COLLISION_RULE); //Team collisionRule (EnumTeamPush.e)
		Reflection.getField(packet.getClass(), "g").set(packet, color.id); //Team color (EnumChatFormat.C)
		Reflection.getField(packet.getClass(), "h").set(packet, modifiedEnities);
		Reflection.getField(packet.getClass(), "i").set(packet, 4); //Mode (0 = create team; 1 = remove team; 2 = update info; 3 = add players; remove players
		Reflection.getField(packet.getClass(), "j").set(packet, 0); //not sure what this does :/

		return packet;
	}

	public static void initTeams(Player receiver) {
		for(GlowingColor color : GlowingColor.values()) {
			FakeShulker.sendPackets(receiver, createTeam(color));
		}
	}

	public enum GlowingColor {
		BLACK(0, '0'),
		DARK_BLUE(1, '1'),
		DARK_GREEN(2, '2'),
		DARK_AQUA(3, '3'),
		DARK_RED(4, '4'),
		DARK_PURPLE(5, '5'),
		GOLD(6, '6'),
		GRAY(7, '7'),
		DARK_GRAY(8, '8'),
		BLUE(9, '9'),
		GREEN(10, 'a'),
		AQUA(11, 'b'),
		RED(12, 'c'),
		LIGHT_PURPLE(13, 'd'),
		YELLOW(14, 'e'),
		WHITE(15, 'f'),
		NONE(-1, 'r');

		public int id;
		public char colorCode;
		public String teamName;

		GlowingColor(int id, char colorCode) {
			this.id = id;
			this.colorCode = colorCode;
			this.teamName = "GLOWING_" + this.id;
		}
	}
}
