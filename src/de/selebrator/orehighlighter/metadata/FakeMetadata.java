package de.selebrator.orehighlighter.metadata;

import de.selebrator.orehighlighter.PluginMain;
import de.selebrator.orehighlighter.reflection.ConstructorAccessor;
import de.selebrator.orehighlighter.reflection.FieldAccessor;
import de.selebrator.orehighlighter.reflection.MethodAccessor;
import de.selebrator.orehighlighter.reflection.Reflection;
import de.selebrator.orehighlighter.reflection.ServerPackage;

public class FakeMetadata {

	private Object dataWatcher;

	private byte status;

	private static final Class<?> CLASS_DataWatcher = Reflection.getMinecraftClass("DataWatcher");
	private static final Class<?> CLASS_Entity = Reflection.getMinecraftClass("Entity");
	private static final Class<?> CLASS_DataWatcherObject = Reflection.getMinecraftClass("DataWatcherObject");
	private static final ConstructorAccessor CONSTRUCTOR_DataWatcher = Reflection.getConstructor(CLASS_DataWatcher, CLASS_Entity);
	private static final MethodAccessor METHOD_DataWatcher_registerObject = Reflection.getMethod(CLASS_DataWatcher, null, "registerObject", CLASS_DataWatcherObject, Object.class);

	public FakeMetadata() {
		this.dataWatcher = CONSTRUCTOR_DataWatcher.newInstance(new Object[] { null });
	}

	public void set(DataWatcherObject dataWatcherObject, Object value) {
		METHOD_DataWatcher_registerObject.invoke(this.dataWatcher, dataWatcherObject.getObject(), value);
	}

	public Object getDataWatcher() {
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
		ENTITY_STATUS_BITMASK_00("Entity", "Z", 0);

		private Object object;

		DataWatcherObject(String parent, String fieldName, int index) {
			Class<?> parentClazz = Reflection.getMinecraftClass(parent);
			FieldAccessor field;
			if(ServerPackage.getVersion().equals(PluginMain.VERSION))
				field = Reflection.getField(parentClazz, fieldName);
			else
				field = Reflection.getField(parentClazz, CLASS_DataWatcherObject, index);

			this.object = field.get(null);
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
