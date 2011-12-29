package invokedynamic;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;


public class Main {

	public static void main(String[] args) throws Throwable {
		
		Class<?> test = Class.forName("invokedynamic.TestPrint");
		
		MethodHandle main = MethodHandles.lookup().findStatic(test, "main", MethodType.methodType(void.class, String[].class));

		main.invokeExact((String[])args);
		
//		TestPrint.main(args);
	}
}
