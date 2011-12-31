package demo.invoke;

import static invokedynamic.InvokeDynamicUtils.BootStrap;
import invokedynamic.BootstrapUtils;
import invokedynamic.InvokeDynamicUtils.InvokeDynamicBootstrap;

import java.lang.invoke.MethodHandle;

/**
 * the template for InvokeDynamic
 */
public class TestInvokeDynamicConstantCallSite_plain {

	@InvokeDynamicBootstrap(type=BootstrapUtils.class,
			name="simpleCall",
			args=Class.class)
	private final static MethodHandle BSM = BootStrap(	String.class);
	
	static void testInvokeDynamic() throws Throwable {
		String test = "Hello World";

		long start = System.currentTimeMillis();

		for (int i = 0; i < TestSimple.LOOPS; i++) {

			String str = (String) BSM.invokeExact("replace", test, 'o', 'O');

		}

		long end = System.currentTimeMillis();

		System.out.println("TestReflect Total time is " + (end - start));

	}

	
}
