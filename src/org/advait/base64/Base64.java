package org.advait.base64;

import java.security.InvalidParameterException;

public class Base64 {

	// Defining constants

	private static final char PADDING_CONST = '=';

	private static final byte FIRST_SIX_BIT_MASK = (byte) 0xfc; // 11111100 [252]
	private static final byte LAST_TWO_BIT_MASK = (byte) 0x03; // 00000011 [3]
	private static final byte FIRST_FOUR_BIT_MASK = (byte) 0xf0; // 11110000 [240]
	private static final byte LAST_FOUR_BIT_MASK = (byte) 0x0f; // 00001111 [15]
	private static final byte FIRST_TWO_BIT_MASK = (byte) 0xc0; // 11000000 [192]
	private static final byte LAST_SIX_BIT_MASK = (byte) 0x3f; // 00111111 [63]

	private static final byte BIT_MASK_3_4 = (byte) 0x30; // 00110000
	private static final byte BIT_MASK_3_4_5_6 = (byte) 0x3c; // 00111100

	public static String convertToBase64(byte[] str) {
		/*
		 * -----PLAIN BYTES-----
		 * 
		 * 1st BYTE : 11111100
		 * 
		 * 2nd BYTE: 00001111
		 * 
		 * 3rd BYTE: 11000000
		 */

		/*
		 * -----BASE64 CHARACTER-----
		 * 
		 * 1st = '00' + FIRST_SIX_BIT_MASK(FIRST_BYTE)
		 * 
		 * 2nd = LAST_TWO_BIT_MASK(FIRST_BYTE) + FIRST_FOUR_BIT_MASK(SECOND_BYTE)
		 * 
		 * 3rd = LAST_FOUR_BIT_MASK(SECOND_BYTE) + FIRST_TWO_BIT_MASK(THIRD_BYTE)
		 * 
		 * 4th = '00' + LAST_SIX_BIT_MASK(THIRD_BYTE)
		 * 
		 */

		byte[] base64;

		int additionalStrLen = 0;

		if (str.length % 3 != 0) {
			int mul = Math.ceilDiv(str.length, 3); // ceiling division
			additionalStrLen = 3 * mul - str.length;
		}

		int base64Len = ((str.length + additionalStrLen) / 3) * 4;
		base64 = new byte[base64Len];

		// Processing in group of three

		int i = 0;
		int b = 0;
		int base64TempLen = base64Len;

		boolean EXTRA_ONE = false;
		boolean EXTRA_TWO = false;

		while (true) {
			// converting 3 str bytes to 4 base64 bytes
			byte[] strGroup = new byte[3];
			byte[] base64Group = new byte[4];

			if (i + 2 < str.length) {
				// the group is within array range
				strGroup[0] = str[i];
				strGroup[1] = str[i + 1];
				strGroup[2] = str[i + 2];

				base64Group[0] = (byte) ((strGroup[0] & FIRST_SIX_BIT_MASK) >> 2);
				base64Group[1] = (byte) (((strGroup[0] & LAST_TWO_BIT_MASK) << 4)
						| ((strGroup[1] & FIRST_FOUR_BIT_MASK) >> 4));
				base64Group[2] = (byte) (((strGroup[1] & LAST_FOUR_BIT_MASK) << 2)
						| ((strGroup[2] & FIRST_TWO_BIT_MASK) >> 6));
				base64Group[3] = (byte) (strGroup[2] & LAST_SIX_BIT_MASK);

				for (int j = 0; j < 4; j++) {
					base64[b] = base64Group[j];
					b++;
				}

			} else if (i + 1 < str.length) {
				/*
				 * Only two elements of the group within array range
				 *
				 * 10101010 10101010 + extra byte(00000000) [first 2 bits 00 and last six bit
				 * '=']
				 *
				 * 101010 101010 1010(00) 000000
				 *
				 * 101010 101010 1010(00) =
				 */
				strGroup[0] = str[i];
				strGroup[1] = str[i + 1];

				base64Group[0] = (byte) ((strGroup[0] & FIRST_SIX_BIT_MASK) >> 2);
				base64Group[1] = (byte) (((strGroup[0] & LAST_TWO_BIT_MASK) << 4)
						| ((strGroup[1] & FIRST_FOUR_BIT_MASK) >> 4));
				base64Group[2] = (byte) ((strGroup[1] & LAST_FOUR_BIT_MASK) << 2);

				for (int j = 0; j < 3; j++) {
					base64[b] = base64Group[j];
					b++;
				}

				EXTRA_ONE = true;
				base64TempLen -= 1;
				break;
			} else if (i < str.length) {
				/*
				 * Only one element of the group within array range
				 * 
				 * Need to add extra blank bytes to make 3 total
				 *
				 * 10101010 + extra(00000000) + extra(00000000)
				 *
				 * 101010 10(0000) 000000 000000
				 *
				 * 101010 10(0000) = =
				 */
				strGroup[0] = str[i];

				base64Group[0] = (byte) ((strGroup[0] & FIRST_SIX_BIT_MASK) >> 2);
				base64Group[1] = (byte) ((strGroup[0] & LAST_TWO_BIT_MASK) << 4);

				for (int j = 0; j < 3; j++) {
					base64[b] = base64Group[j];
					b++;
				}

				EXTRA_TWO = true;
				base64TempLen -= 2;
				break;
			} else {
				// array exhausted
				break;
			}
			i = i + 3;
		}
		// We got bytes with range [0-64]
		// converting them to ascii bytes with base64 characters

		for (b = 0; b < base64TempLen; b++) {

			if (base64[b] >= 0 && base64[b] <= 25) {
				base64[b] = (byte) (base64[b] + 65);
			} else if (base64[b] >= 26 && base64[b] <= 51) {
				base64[b] = (byte) (base64[b] - 26 + 97);
			} else if (base64[b] >= 52 && base64[b] <= 61) {
				base64[b] = (byte) (base64[b] - 52 + 48);
			} else if (base64[b] == 62) {
				base64[b] = '+';
			} else if (base64[b] == 63) {
				base64[b] = '/';
			}
		}

		if (EXTRA_ONE) {
			base64[b] = PADDING_CONST;
		} else if (EXTRA_TWO) {
			base64[b] = PADDING_CONST;
			base64[b + 1] = PADDING_CONST;
		}

		return new String(base64);
	}

	public static byte[] convertToPlain(String base64) throws InvalidParameterException {

		/*
		 * ------------BASE64 BYTES-------------------
		 * 
		 * 1st Byte = 00101010
		 * 
		 * 2nd Byte = 00101010
		 * 
		 * 3rd Byte = 00101010
		 * 
		 * 4th Byte = 00101010
		 *
		 * Real data present in last 6 bits
		 * 
		 * ------------PLAIN BYTES---------------------
		 * 
		 * 1st plain = LAST_SIX(FIRST_BYTE) + 3_4(SECOND_BYTE)
		 * 
		 * 2nd plain = LAST_FOUR(SECOND_BYTE) + 3_4_5_6(THIRD_BYTE)
		 * 
		 * 3rd plain = LAST_TWO(THIRD_BYTE) + LAST_SIX(FOURTH_BYTE)
		 * 
		 */

		byte[] str = null;
		byte[] tempStr = new byte[base64.length()];

		int numOfPaddingChars = 0;

		// converting from ascii to base64
		for (int i = 0; i < base64.length(); i++) {
			char curChar = base64.charAt(i);
			if (curChar == '=') {
				tempStr[i] = '=';
				numOfPaddingChars++;
			} else {
				if (curChar >= 'A' && curChar <= 'Z') {
					tempStr[i] = (byte) (curChar - 'A');
				} else if (curChar >= 'a' && curChar <= 'z') {
					tempStr[i] = (byte) (curChar - 'a' + 26);
				} else if (curChar >= '0' && curChar <= '9') {
					tempStr[i] = (byte) (curChar - '0' + 52);
				} else if (curChar == '+') {
					tempStr[i] = (byte) 62;
				} else if (curChar == '/') {
					tempStr[i] = (byte) 63;
				} else {
					throw new InvalidParameterException("Not a base64 character at index " + i);
				}
			}
		}

		int lenStr = (tempStr.length / 4) * 3;
		str = new byte[lenStr];
		// int lenTempStrReal = tempStr.length - numOfPaddingChars;

		// converting 4 six bits to 3 bytes
		// converting 4 bytes to 3 bytes
		int maxContProcessing = tempStr.length;

		if (numOfPaddingChars != 0) {
			// Reserving last 4 bytes to be processed separately for '=' case
			maxContProcessing -= 4;
		}

		int i = 0;
		int s = 0;
		while (i + 3 < maxContProcessing) {

			byte[] base64Group = new byte[4];
			byte[] plainGroup = new byte[3];

			base64Group[0] = tempStr[i];
			base64Group[1] = tempStr[i + 1];
			base64Group[2] = tempStr[i + 2];
			base64Group[3] = tempStr[i + 3];

			plainGroup[0] = (byte) (((base64Group[0] & LAST_SIX_BIT_MASK) << 2)
					| ((base64Group[1] & BIT_MASK_3_4) >> 4));
			plainGroup[1] = (byte) (((base64Group[1] & LAST_FOUR_BIT_MASK) << 4)
					| ((base64Group[2] & BIT_MASK_3_4_5_6) >> 2));
			plainGroup[2] = (byte) (((base64Group[2] & LAST_TWO_BIT_MASK) << 6) | (base64Group[3] & LAST_SIX_BIT_MASK));

			str[s] = plainGroup[0];
			str[s + 1] = plainGroup[1];
			str[s + 2] = plainGroup[2];

			i = i + 4;
			s = s + 3;
		}

		if (numOfPaddingChars == 2) {
			byte[] base64Group = new byte[2];

			base64Group[0] = tempStr[i];
			base64Group[1] = tempStr[i + 1];

			str[s] = (byte) (((base64Group[0] & LAST_SIX_BIT_MASK) << 2) | ((base64Group[1] & BIT_MASK_3_4) >> 4));

		} else if (numOfPaddingChars == 1) {

			byte[] base64Group = new byte[3];
			byte[] plainGroup = new byte[2];

			base64Group[0] = tempStr[i];
			base64Group[1] = tempStr[i + 1];
			base64Group[2] = tempStr[i + 2];

			plainGroup[0] = (byte) (((base64Group[0] & LAST_SIX_BIT_MASK) << 2)
					| ((base64Group[1] & BIT_MASK_3_4) >> 4));
			plainGroup[1] = (byte) (((base64Group[1] & LAST_FOUR_BIT_MASK) << 4)
					| ((base64Group[2] & BIT_MASK_3_4_5_6) >> 2));
			
			str[s] = plainGroup[0];
			str[s + 1] = plainGroup[1];

		}

		return str;
	}

}
