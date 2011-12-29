package demo.invoke;

// This class is a template for ASM code generate

public class TestInvokeDynamicPerformanceTemplate {

	static void testInvokeDynamic() throws Throwable {
		String test = "Hello World";

		long start = System.currentTimeMillis();

		for (int i = 0; i < TestSimple.LOOPS; i++) {

			String str = test.replace('o', 'O');

		}

		long end = System.currentTimeMillis();

		System.out.println("TestReflect Total time is " + (end - start));

	}

}
