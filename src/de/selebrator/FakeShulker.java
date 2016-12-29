package de.selebrator;

import net.minecraft.server.v1_11_R1.EntityLiving;
import net.minecraft.server.v1_11_R1.EntityShulker;
import net.minecraft.server.v1_11_R1.Packet;
import net.minecraft.server.v1_11_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_11_R1.PacketPlayOutSpawnEntityLiving;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class FakeShulker {

	public Player observer;

	private final int entityId;
	public final UUID uuid;
	public Location location;
	public EntityLiving entityLiving;

	public FakeShulker(Location location) {
		this.location = location;
		EntityShulker shulker = new EntityShulker(((CraftWorld) this.location.getWorld()).getHandle());
		this.entityId = shulker.getId();
		this.uuid = shulker.getUniqueID();
		shulker.setInvisible(true);
		shulker.setFlag(6, true);
		shulker.setPosition(this.location.getBlockX() + 0.5D, this.location.getBlockY(), this.location.getBlockZ() + 0.5D);
		shulker.setNoGravity(true);
		this.entityLiving = shulker;
	}

	public void spawn(Player observer) {
		this.observer = observer;
		sendPackets(this.observer, new PacketPlayOutSpawnEntityLiving(this.entityLiving));
	}

	public void despawn() {
		sendPackets(this.observer, new PacketPlayOutEntityDestroy(this.entityId));
	}

	public static void sendPackets(Player player, Packet<?>... packets) {
		for(Packet<?> packet : packets) {
			((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
		}
	}
}
