package invokedynamic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;


public class InvokeDynamicUtils {
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface InvokeDynamicBootstrap {

		Class<?> type();

		String name();

		Class<?>[] args();
		
	}

	public static MethodHandle BootStrap(Object ...args) {
		throw new UnsupportedOperationException();
	}
}
