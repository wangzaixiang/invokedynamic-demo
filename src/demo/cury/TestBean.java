package demo.cury;

import java.util.Arrays;

public class TestBean {

	public static String toString4(int a, int b, int c, int d) {
		return Arrays.toString(new int[]{a, b, c, d});
	}
	
	public static String toString(String message, int[] nums) {
		return message + ":" + Arrays.toString(nums);
	}
	
}
