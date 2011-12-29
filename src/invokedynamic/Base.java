package invokedynamic;

/**
 * We will generate a class which will using invokedynamic to call the println method
 * 
 * var base = new Base();
 * base.println("hello world");
 * 
 */

public class Base {

	public void println(String message) {
		System.out.println(message);
	}
	
	public String replace(char old, char n) {
		return "hello";
	}
}
