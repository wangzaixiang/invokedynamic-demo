package invokedynamic;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;

public class BootstrapUtils {

	/**
	 * mName: println mType: (Object,String)V declaringClass: Base.class
	 */
	public static CallSite simpleCall(MethodHandles.Lookup lookup,
			String mName, MethodType mType, Class<?> declaringClass) {

		try {
			// MethodType virtualType = MethodType.methodType(Void.TYPE,
			// String.class);
			MethodType virtualType = mType.dropParameterTypes(0, 1);
			MethodHandle mHandle = lookup.findVirtual(declaringClass, mName,
					virtualType);

			MethodHandle asType = mHandle.asType(mType);
			CallSite callSite = new ConstantCallSite(asType);
			return callSite;
		} catch (NoSuchMethodException | IllegalAccessException e) {
			throw new LinkageError("Link error:", e);
		}

	}

	public static CallSite mutableCallSite(MethodHandles.Lookup lookup,
			String name, MethodType type) {

		InliningCacheCallSite callSite = new InliningCacheCallSite(lookup,
				name, type);

		return callSite;
	}

	public static class InliningCacheCallSite extends MutableCallSite {

		private final String name;

		private final MethodHandles.Lookup lookup;

		private MethodHandle fallback;

		public InliningCacheCallSite(MethodHandles.Lookup lookup, String name,
				MethodType type) {
			super(type);
			this.lookup = lookup;
			this.name = name;

			MethodHandle fallback = FALLBACK.bindTo(this);
			fallback = fallback.asCollector(Object[].class,
					type.parameterCount());
			fallback = fallback.asType(type);
			this.fallback = fallback;

			setTarget(fallback);

		}

		public Object fallback(Object[] args) throws Throwable {
			Class<?> receiverClass = args[0].getClass();

			MethodHandle target = lookup.findVirtual(receiverClass, name, type().dropParameterTypes(0, 1));
			target = target.asType(type());

			MethodHandle test = CHECK_CLASS.bindTo(receiverClass);
			test = test.asType(MethodType.methodType(boolean.class, type().parameterType(0)));
			MethodHandle guard = MethodHandles.guardWithTest(test, target, fallback);
			setTarget(guard);

			return target.invokeWithArguments(args);
		}
	}

	public static boolean checkClass(Class<?> type, Object o) {
		return type == o.getClass();
	}

	static MethodHandle CHECK_CLASS, FALLBACK;
	static {

		try {
			CHECK_CLASS = MethodHandles.publicLookup().findStatic(
					BootstrapUtils.class,
					"checkClass",
					MethodType.methodType(boolean.class, Class.class,
							Object.class));
			FALLBACK = MethodHandles.publicLookup().findVirtual(
					InliningCacheCallSite.class, "fallback",
					MethodType.methodType(Object.class, Object[].class));
		} catch (NoSuchMethodException | IllegalAccessException e) {

		}
	}
}
