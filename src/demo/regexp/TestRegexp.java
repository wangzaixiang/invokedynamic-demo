package demo.regexp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRegexp {

	public static void main(String[] args) {
		
		Pattern p = Pattern.compile("(?<name>\\w+)\\s\\1");
		
		Matcher matcher = p.matcher("wang wang");
		if(matcher.matches()){
			System.out.println("it is matches");
			String name = matcher.group("name");
			System.out.println("name = " + name);
		}
		else {
			System.out.println("not matches");
		}
			
		
	}
}
