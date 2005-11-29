/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   Copyright (C) 2005 - Matteo Merli - matteo.merli@gmail.com            *
 *                                                                         *
 ***************************************************************************/

/*
 * $Id$
 * 
 * $URL$
 * 
 */
package rtspproxy.lib.number;

import java.util.Arrays;

import junit.framework.TestCase;

/**
 * @author Matteo Merli
 */
public class UnsignedLongTest extends TestCase {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(UnsignedLongTest.class);
	}

	public void test1() {
		UnsignedLong n = new UnsignedLong(0xFFFFFFFFFFFFFFFFL);

		assertEquals(0xFFFFFFFFFFFFFFFFL, n.longValue());
		assertEquals("0xFFFFFFFFFFFFFFFF", n.toHexString());
		assertEquals("18446744073709551615", n.toString());
		assertTrue(Arrays.equals(new byte[] { (byte) 0xFF, (byte) 0xFF,
				(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
				(byte) 0xFF, (byte) 0xFF }, n.getBytes()));
	}

	public void test2() {
		UnsignedLong n = new UnsignedLong(0xFFFFFFFFL);

		assertEquals(0xFFFFFFFF, n.intValue());
		assertEquals(0xFFFFFFFFL, n.longValue());
		assertEquals("0x00000000FFFFFFFF", n.toHexString());
		assertEquals("4294967295", n.toString());
		assertTrue(Arrays.equals(new byte[] { 0, 0, 0, 0, (byte) 0xFF,
				(byte) 0xFF, (byte) 0xFF, (byte) 0xFF }, n.getBytes()));

	}

	public void test3() {
		UnsignedLong n = UnsignedLong.fromString("0xFFFFFFFFFFFFFFFF");

		assertEquals(0xFFFFFFFFFFFFFFFFL, n.longValue());
		assertEquals("0xFFFFFFFFFFFFFFFF", n.toHexString());
		assertEquals("18446744073709551615", n.toString());
		assertTrue(Arrays.equals(new byte[] { (byte) 0xFF, (byte) 0xFF,
				(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
				(byte) 0xFF, (byte) 0xFF }, n.getBytes()));
	}

	public void test4() {
		UnsignedLong n = UnsignedLong.fromString("18446744073709551615");

		assertEquals(0xFFFFFFFFFFFFFFFFL, n.longValue());
		assertEquals("0xFFFFFFFFFFFFFFFF", n.toHexString());
		assertEquals("18446744073709551615", n.toString());
		assertTrue(Arrays.equals(new byte[] { (byte) 0xFF, (byte) 0xFF,
				(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
				(byte) 0xFF, (byte) 0xFF }, n.getBytes()));
	}

	public void test5() {
		UnsignedLong n = UnsignedLong.fromBytes(new byte[] { (byte) 0xFF,
				(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
				(byte) 0xFF, (byte) 0xFF, (byte) 0xFF });

		assertEquals(0xFFFFFFFFFFFFFFFFL, n.longValue());
		assertEquals("0xFFFFFFFFFFFFFFFF", n.toHexString());
		assertEquals("18446744073709551615", n.toString());
		assertTrue(Arrays.equals(new byte[] { (byte) 0xFF, (byte) 0xFF,
				(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
				(byte) 0xFF, (byte) 0xFF }, n.getBytes()));
	}

	public void test6() {
		UnsignedLong n1 = new UnsignedLong(0);
		UnsignedLong n2 = new UnsignedLong(0xFFFF);
		UnsignedLong n3 = new UnsignedLong(0xFFFFFFFFL);
		UnsignedLong n4 = new UnsignedLong(0xFFFFFFFFFFFFFFFFL);
		UnsignedLong n5 = UnsignedLong.fromBytes(new byte[] { (byte) 0xFF,
				(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
				(byte) 0xFF, (byte) 0xFF, (byte) 0xFF });

		assertTrue(n1.compareTo(n1) == 0);
		assertTrue(n1.compareTo(n2) < 0);
		assertTrue(n1.compareTo(n3) < 0);
		assertTrue(n1.compareTo(n4) < 0);
		assertTrue(n1.compareTo(n5) < 0);
		assertTrue(n2.compareTo(n1) > 0);
		assertTrue(n2.compareTo(n2) == 0);
		assertTrue(n2.compareTo(n3) < 0);
		assertTrue(n2.compareTo(n4) < 0);
		assertTrue(n2.compareTo(n5) < 0);
		assertTrue(n3.compareTo(n1) > 0);
		assertTrue(n3.compareTo(n2) > 0);
		assertTrue(n3.compareTo(n3) == 0);
		assertTrue(n3.compareTo(n4) < 0);
		assertTrue(n3.compareTo(n5) < 0);
		assertTrue(n4.compareTo(n1) > 0);
		assertTrue(n4.compareTo(n2) > 0);
		assertTrue(n4.compareTo(n3) > 0);
		assertTrue(n4.compareTo(n4) == 0);
		assertTrue(n4.compareTo(n5) == 0);
		assertTrue(n5.compareTo(n1) > 0);
		assertTrue(n5.compareTo(n2) > 0);
		assertTrue(n5.compareTo(n3) > 0);
		assertTrue(n5.compareTo(n4) == 0);
		assertTrue(n5.compareTo(n5) == 0);
	}
}
