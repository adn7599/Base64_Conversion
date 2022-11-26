package org.advait.base64.example;

import org.advait.base64.Base64;
import java.util.Scanner;

public class ExampleSimple {

	public static void main(String[] args) {
		
		Scanner sc = new Scanner(System.in);
		
		System.out.println("Enter the plain text:");
		
		String plainStr = sc.nextLine();
		sc.close();
		
		//Conversion to Base64
		String base64Str = Base64.convertToBase64(plainStr.getBytes());
		
		System.out.println("\nBase64 String: ");
		System.out.println(base64Str);
		
		//Conversion to plain
		String plainRes = new String(Base64.convertToPlain(base64Str));

		System.out.println("\nPlain String: ");
		System.out.println(plainRes);
	}

}
