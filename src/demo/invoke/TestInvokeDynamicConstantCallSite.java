package demo.invoke;

import static invokedynamic.InvokeDynamicUtils.BootStrap;
import invokedynamic.BootstrapUtils;
import invokedynamic.InvokeDynamicUtils.InvokeDynamicBootstrap;
import invokedynamic.InvokeDynamicUtils.InvokeDynamicTransformation;

import java.lang.invoke.MethodHandle;

/**
 * the template for InvokeDynamic
 */
@InvokeDynamicTransformation
public class TestInvokeDynamicConstantCallSite{

	@InvokeDynamicBootstrap(type=BootstrapUtils.class,
			name="simpleCall",
			args=Class.class)
	private final static MethodHandle BSM = BootStrap(	String.class);
	
	public static void main(String[] args) throws Throwable {
		System.out.println("Run the InvokeDynamic Test");
		testInvokeDynamic();
	}
	
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
