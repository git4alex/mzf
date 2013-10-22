package com.zonrong.core.util;

import java.util.Random;

/**
 * date: 2010-12-28
 *
 * version: 1.0
 * commonts: ......
 */
public class SimpleUtils {
	public static final int randomInt() {
		Random random = new Random();
		
		return random.nextInt(99999999) + 900000000;
	}
}


