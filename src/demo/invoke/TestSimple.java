package demo.invoke;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import static java.lang.invoke.MethodType.*;

@SuppressWarnings("unused")
public class TestSimple {

	public static final int LOOPS = 1024 * 1024 * 128;

	public static void main(String[] args) throws Throwable {

		testDirect();

		testInvoker();

		testReflect();

		testConstantCallSiteDynamic();

		testConstantMutableSiteDynamic();

	}

	private static void testConstantMutableSiteDynamic() throws Throwable {
		Class<?> clazz = Class
				.forName("demo.invoke.TestInvokeDynamicMutableCallSite");

		MethodHandle main = MethodHandles.lookup().findStatic(clazz,
				"testInvokeDynamic", methodType(void.class));
		main.invokeExact();

	}

	private static void testConstantCallSiteDynamic() throws Throwable {

		Class<?> clazz = Class
				.forName("demo.invoke.TestInvokeDynamicConstantCallSite");

		MethodHandle main = MethodHandles.lookup().findStatic(clazz,
				"testInvokeDynamic", methodType(void.class));
		main.invokeExact();

	}

	static void testReflect() throws Throwable {
		String test = "Hello World";

		long start = System.currentTimeMillis();

		Method method = String.class.getMethod("replace", char.class,
				char.class);

		for (int i = 0; i < LOOPS; i++) {

			// test.replace('o', 'O');
			// System.out.println("Invoke " + i);
			String res = (String) method.invoke(test, 'o', 'O');

		}

		long end = System.currentTimeMillis();

		System.out.println("TestReflect Total time is " + (end - start));

	}

	static void testInvoker() throws Throwable {
		String test = "Hello World";

		MethodHandles.Lookup lookup = MethodHandles.lookup();
		MethodType methodType = MethodType.methodType(String.class, char.class,
				char.class);

		MethodHandle mh = lookup.findVirtual(String.class, "replace",
				methodType);

		long start = System.currentTimeMillis();

		for (int i = 0; i < LOOPS; i++) {

			String res = (String) mh.invoke(test, 'o', 'O');

		}

		long end = System.currentTimeMillis();

		System.out.println("TestInvoke Total time is " + (end - start));

	}

	static void testDirect() {
		String test = "Hello World";

		long start = System.currentTimeMillis();

		for (int i = 0; i < LOOPS; i++) {

			String str = test.replace('o', 'O');

		}

		long end = System.currentTimeMillis();

		System.out.println("TestDirect Total time is " + (end - start));

	}

}
