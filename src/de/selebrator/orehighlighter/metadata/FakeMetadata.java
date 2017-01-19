package de.selebrator.orehighlighter.metadata;

import de.selebrator.orehighlighter.reflection.IMethodAccessor;
import de.selebrator.orehighlighter.reflection.Reflection;
import de.selebrator.orehighlighter.reflection.ServerPackage;
import net.minecraft.server.v1_11_R1.DataWatcher;

public class FakeMetadata {

	private DataWatcher dataWatcher;

	private byte status;

	private static final IMethodAccessor METHOD_DataWatcher_registerObject = Reflection.getMethod(DataWatcher.class, "registerObject", Reflection.getClass(ServerPackage.NMS, "DataWatcherObject"), Object.class);

	public FakeMetadata() {
		this.dataWatcher = new DataWatcher(null);
	}

	public void set(DataWatcherObject dataWatcherObject, Object value) {
		METHOD_DataWatcher_registerObject.invoke(this.dataWatcher, dataWatcherObject.getObject(), value);
	}

	public DataWatcher getDataWatcher() {
		return this.dataWatcher;
	}

	public void setInvisible(boolean state) {
		this.status = setBit(this.status, Status.INVISIBLE.getId(), state);
		this.set(DataWatcherObject.ENTITY_STATUS_BITMASK_00, this.status);
	}

	public void setGlowing(boolean state) {
		this.status = setBit(this.status, Status.GLOW.getId(), state);
		this.set(DataWatcherObject.ENTITY_STATUS_BITMASK_00, this.status);
	}

	public enum DataWatcherObject {
		ENTITY_STATUS_BITMASK_00("Entity", "Z");

		private Object object;

		DataWatcherObject(String parent, String field) {
			Class<?> parentClazz = Reflection.getClass(ServerPackage.NMS, parent);
			this.object = Reflection.getField(parentClazz, field).get(null);
		}

		public Object getObject() {
			return object;
		}
	}

	public enum Status {
		INVISIBLE(5),
		GLOW(6);

		byte id;

		Status(int id) {
			this.id = (byte) id;
		}

		public byte getId() {
			return id;
		}
	}

	public static byte setBit(byte bitMask, int bit, boolean state) {
		return state ? (byte) (bitMask | (1 << bit)) : (byte) (bitMask & ~(1 << bit));
	}
}
