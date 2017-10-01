package de.selebrator.orehighlighter.reflection;

import org.bukkit.Bukkit;

public enum ServerPackage {

	NMS("net.minecraft.server." + getVersion()),
	MOJANG_AUTHLIB("com.mojang.authlib"),
	OBC(Bukkit.getServer().getClass().getPackage().getName());

	private final String name;

	ServerPackage(String source) {
		this.name = source;
	}

	public static String getVersion() {
		return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
	}

	@Override
	public String toString() {
		return name;
	}
}
