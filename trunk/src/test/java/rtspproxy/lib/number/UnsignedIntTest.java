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
public class UnsignedIntTest extends TestCase {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(UnsignedIntTest.class);
	}
	
	public void test1() {
		UnsignedInt n = new UnsignedInt(0xFFFFFFFF);

		assertEquals(0xFFFFFFFFL, n.longValue());
		assertEquals("0xFFFFFFFF", n.toHexString());
		assertEquals("4294967295", n.toString());
		assertTrue(Arrays.equals(new byte[] { (byte) 0xFF, (byte) 0xFF,
				(byte) 0xFF, (byte) 0xFF }, n.getBytes()));

	}

	public void test2() {
		UnsignedInt n = new UnsignedInt(0xFFFFFFFFL);

		assertEquals(0xFFFFFFFFL, n.longValue());
		assertEquals("0xFFFFFFFF", n.toHexString());
		assertEquals("4294967295", n.toString());
		assertTrue(Arrays.equals(new byte[] { (byte) 0xFF, (byte) 0xFF,
				(byte) 0xFF, (byte) 0xFF }, n.getBytes()));

	}

	public void test3() {
		UnsignedInt n = UnsignedInt.fromString("0xFFFFFFFF");

		assertEquals(0xFFFFFFFFL, n.longValue());
		assertEquals("0xFFFFFFFF", n.toHexString());
		assertEquals("4294967295", n.toString());
		assertTrue(Arrays.equals(new byte[] { (byte) 0xFF, (byte) 0xFF,
				(byte) 0xFF, (byte) 0xFF }, n.getBytes()));
	}

	public void test4() {
		UnsignedInt n = UnsignedInt.fromString("4294967295");

		assertEquals(0xFFFFFFFFL, n.longValue());
		assertEquals("0xFFFFFFFF", n.toHexString());
		assertEquals("4294967295", n.toString());
		assertTrue(Arrays.equals(new byte[] { (byte) 0xFF, (byte) 0xFF,
				(byte) 0xFF, (byte) 0xFF }, n.getBytes()));
	}

	public void test5() {
		UnsignedInt n = UnsignedInt.fromBytes(new byte[] { (byte) 0xFF,
				(byte) 0xFF, (byte) 0xFF, (byte) 0xFF });

		assertEquals(0xFFFFFFFFL, n.longValue());
		assertEquals("0xFFFFFFFF", n.toHexString());
		assertEquals("4294967295", n.toString());
		assertTrue(Arrays.equals(new byte[] { (byte) 0xFF, (byte) 0xFF,
				(byte) 0xFF, (byte) 0xFF }, n.getBytes()));
	}

	public void test6() {
		UnsignedInt n1 = new UnsignedInt(0);
		UnsignedInt n2 = new UnsignedInt(0xFFFF);
		UnsignedInt n3 = new UnsignedInt(0xFFFFFFFFL);

		assertTrue(n1.compareTo(n2) < 0);
		assertTrue(n2.compareTo(n3) < 0);
		assertTrue(n1.compareTo(n3) < 0);
		assertTrue(n2.compareTo(n2) == 0);
		assertTrue(n1.compareTo(new UnsignedInt(0)) == 0);
	}
}
