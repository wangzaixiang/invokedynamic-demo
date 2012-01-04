package snipcode;


public class HelloWorld {
	
	public HelloWorld(){
		
		System.out.println(super.toString());
		System.out.println(this.toString());
		
	}

	
	@Override
	public String toString() {
		return "Class of HelloWorld";
	}
	
}
