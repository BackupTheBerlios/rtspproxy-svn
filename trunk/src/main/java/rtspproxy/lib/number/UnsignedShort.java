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

/**
 * @author Matteo Merli
 * 
 */
public final class UnsignedShort extends UnsignedNumber {
	static final long serialVersionUID = 1L;

	private int value;

	public UnsignedShort(byte c) {
		value = c;
	}

	public UnsignedShort(short c) {
		value = c;
	}

	public UnsignedShort(int c) {
		value = c & 0xFFFF;
	}

	public UnsignedShort(long c) {
		value = (int) (c & 0xFFFFL);
	}

	private UnsignedShort() {
		value = 0;
	}

	public static UnsignedShort fromBytes(byte[] c) {
		return fromBytes(c, 0);
	}

	public static UnsignedShort fromBytes(byte[] c, int idx) {
		UnsignedShort number = new UnsignedShort();
		if ((c.length - idx) < 2)
			throw new IllegalArgumentException(
					"An UnsignedShort number is composed of 2 bytes.");

		number.value = (c[0] << 8 | c[1]) & 0xFFFF;
		return number;
	}

	public static UnsignedShort fromString(String c) {
		UnsignedShort number = new UnsignedShort();
		char[] begin = new char[2];
		c.getChars(0, 2, begin, 0);
		long v = 0;
		if (begin[0] == '0' && (begin[1] == 'x' || begin[1] == 'X'))
			v = Integer.parseInt(c.substring(2), 16);
		else
			v = Integer.parseInt(c);
		number.value = (int) (v & 0xFFFF);
		return number;
	}

	@Override
	public double doubleValue() {
		return (double) value;
	}

	@Override
	public float floatValue() {
		return (float) value;
	}

	public short shortValue() {
		return (short) (value & 0xFFFF);
	}

	@Override
	public int intValue() {
		return value & 0xFFFF;
	}

	@Override
	public long longValue() {
		return value & 0xFFFFL;
	}

	public byte[] getBytes() {
		byte[] c = new byte[2];
		c[0] = (byte) ((value >> 8) & 0xFF);
		c[1] = (byte) ((value >> 0) & 0xFF);
		return c;
	}

	public int compareTo(UnsignedNumber other) {
		int otherValue = other.intValue();
		if (value > otherValue)
			return +1;
		else if (value < otherValue)
			return -1;
		return 0;
	}

	public String toString() {
		return Integer.toString(value);
	}

}
