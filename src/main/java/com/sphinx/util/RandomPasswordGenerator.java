package com.sphinx.util;

import java.security.SecureRandom;

public class RandomPasswordGenerator {

	private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "abcdefghijklmnopqrstuvwxyz" + "0123456789" + "@#$!";
	private static final String DIGITS = "0123456789";

	private static final SecureRandom random = new SecureRandom();

	public static String generatePassword(int length) {
		StringBuilder password = new StringBuilder(length);

		for (int i = 0; i < length; i++) {
			int index = random.nextInt(CHARACTERS.length());
			password.append(CHARACTERS.charAt(index));
		}

		return password.toString();
	}

	public static String generateSecurityCode(int length) {
		StringBuilder otp = new StringBuilder();

		for (int i = 0; i < length; i++) {
			otp.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
		}
		return otp.toString();
	}

}