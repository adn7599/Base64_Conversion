package org.advait.base64.example;

import org.advait.base64.Base64;

public class ExampleSimple {

	public static void main(String[] args) {
		//Conversion to Base64
		String plainStr = "Hello world";
		String base64Str = Base64.convertToBase64(plainStr.getBytes());
		
		System.out.println("Base64 Str: ");
		System.out.println(base64Str);
	}

}
