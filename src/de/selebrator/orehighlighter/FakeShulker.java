package de.selebrator.orehighlighter;

import de.selebrator.orehighlighter.fetcher.PacketFetcher;
import de.selebrator.orehighlighter.metadata.FakeMetadata;
import de.selebrator.orehighlighter.reflection.Reflection;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.UUID;

public class FakeShulker {

	public Player observer;

	public final int entityId;
	public final UUID uuid;
	public Location location;
	public FakeMetadata meta;

	public FakeShulker() {
		Class<?> entityClass = Reflection.getMinecraftClass("Entity");
		this.entityId = (int) Reflection.getField(entityClass, "entityCount").get(null);
		Reflection.getField(entityClass, int.class, "entityCount").set(null, this.entityId + 1);

		Random random = new Random();
		long mostSigBits = random.nextLong() & 0xffffffffffff0fffL | 0x4000L;
		long leastSigBits = random.nextLong() & 0x3fffffffffffffffL | 0x8000000000000000L;
		this.uuid = new UUID(mostSigBits, leastSigBits);

		this.meta = new FakeMetadata();
		this.meta.setInvisible(true);
		this.meta.setGlowing(true);
	}

	public void spawn(Player observer, Location location) {
		this.observer = observer;
		this.location = location;
		this.location.setX(this.location.getBlockX());
		this.location.setY(this.location.getBlockY());
		this.location.setZ(this.location.getBlockZ());
		PacketFetcher.sendPackets(this.observer, PacketFetcher.spawnEntityLiving(this.entityId, this.uuid, this.location.clone(), this.meta.getDataWatcher()));
	}

	public void despawn() {
		PacketFetcher.sendPackets(this.observer, PacketFetcher.entityDestroy(this.entityId));
	}

	public void move(double x, double y, double z) {
		if(Math.abs(x) >= 8 || Math.abs(y) >=8 || Math.abs(z) >= 8) {
			System.out.println("Error in move input: difference cant be > 8");
			return;
		}

		PacketFetcher.sendPackets(this.observer, PacketFetcher.relEntityMove(this.entityId, x, y, z));
		this.location.add(x, y, z);
	}

	public void move(Location location) {
		//make coordinates relative
		double x = this.location.getX() - location.getX();
		double y = this.location.getY() - location.getY();
		double z = this.location.getZ() - location.getZ();
		this.move(x, y, z);
	}

	public void setGlowColor(Glow.GlowingColor color) {
		Glow.addTeamMember(this.observer, color, this.uuid.toString());
	}
}
