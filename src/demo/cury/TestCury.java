package demo.cury;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;

import static java.lang.invoke.MethodHandles.*;

import static java.lang.invoke.MethodType.*;

@SuppressWarnings("unused")
public class TestCury {

	public static void main(String[] args) throws Throwable {
		
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		
		MethodHandle sort = lookup.findStatic(Arrays.class, "sort", methodType(void.class, int[].class));
		MethodHandle toString4 = lookup.findStatic(TestBean.class, "toString4", 
				methodType(String.class, int.class, int.class, int.class, int.class));
		MethodHandle toString = lookup.findStatic(TestBean.class, "toString", 
				methodType(String.class, String.class, int[].class));
				
//		sort.invoke(ints);
//		System.out.println(Arrays.toString(ints));
		System.out.println(toString4.invoke(new Integer(4),3,2,1) );
		System.out.println(toString4.invokeWithArguments(new Integer(4),3,2,1));
		
		
		MethodHandle lessMH = toString4.asSpreader(Integer[].class, 3);
		System.out.println("call with asSpreader:" + lessMH.invoke(4,new Integer[]{3,2,1}) );
		
//		System.out.println("toString calls " + toString.invoke("Hello", 7,8,9,10));
		
		MethodHandle asVarargsCollector = toString.asVarargsCollector(int[].class);
		System.out.println(asVarargsCollector + "<-old->" + toString);
		System.out.println(asVarargsCollector.invoke("Hello:", 5,4,3,2,1));
	}
}
