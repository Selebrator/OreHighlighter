package de.selebrator.orehighlighter;

import com.google.common.collect.Lists;
import de.selebrator.orehighlighter.fetcher.PacketFetcher;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;

public class Glow {

	public static String TEAM_TAG_VISIBILITY = "always";
	public static String COLLISION_RULE = "never";

	public static void createTeam(Player observer, GlowingColor color) {
		PacketFetcher.sendPackets(observer, PacketFetcher.scoreboardTeam(color, TEAM_TAG_VISIBILITY, COLLISION_RULE, new ArrayList<>(), ScoreboardTeamOperation.CREATE_TEAM));
	}

	public static void addTeamMember(Player observer, GlowingColor color, String entityReference) { //entityReference = name for players and uuid for entities
		Collection<String> modifiedEntities = Lists.newArrayList();
		modifiedEntities.add(entityReference);
		PacketFetcher.sendPackets(observer, PacketFetcher.scoreboardTeam(color, TEAM_TAG_VISIBILITY, COLLISION_RULE, modifiedEntities, ScoreboardTeamOperation.ADD_PLAYERS));
	}

	public static void initTeams(Player observer) {
		for(GlowingColor color : GlowingColor.values()) {
			createTeam(observer, color);
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

	public enum ScoreboardTeamOperation {
		//0 = create team; 1 = remove team; 2 = update info; 3 = add players; remove players
		CREATE_TEAM(0),
		ADD_PLAYERS(3);

		public int id;

		ScoreboardTeamOperation(int id) {
			this.id = id;
		}
	}
}
