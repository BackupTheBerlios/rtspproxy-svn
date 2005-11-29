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
public final class UnsignedInt extends UnsignedNumber {
	static final long serialVersionUID = 1L;

	private long value;

	public UnsignedInt(byte c) {
		value = c;
	}

	public UnsignedInt(short c) {
		value = c;
	}

	public UnsignedInt(int c) {
		value = c;
	}

	public UnsignedInt(long c) {
		value = c & 0xFFFFFFFFL;
	}

	private UnsignedInt() {
		value = 0;
	}

	public static UnsignedInt fromBytes(byte[] c) {
		return fromBytes(c, 0);
	}

	public static UnsignedInt fromBytes(byte[] c, int idx) {
		UnsignedInt number = new UnsignedInt();
		if ((c.length - idx) < 4)
			throw new IllegalArgumentException(
					"An UnsignedInt number is composed of 4 bytes.");

		number.value = (c[0] << 24 | c[1] << 16 | c[2] << 8 | c[3]);
		return number;
	}

	public static UnsignedInt fromString(String c) {
		UnsignedInt number = new UnsignedInt();
		char[] begin = new char[2];
		c.getChars(0, 2, begin, 0);
		long v = 0;
		if (begin[0] == '0' && (begin[1] == 'x' || begin[1] == 'X'))
			v = Long.parseLong(c.substring(2), 16);
		else
			v = Long.parseLong(c);
		number.value = (int) v;
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

	@Override
	public int intValue() {
		// the int will have the sign bit cleared
		return (int) (value & 0x7FFFFFFFL);
	}

	@Override
	public long longValue() {
		return value & 0xFFFFFFFFL;
	}

	public byte[] getBytes() {
		byte[] c = new byte[4];
		c[0] = (byte) ((value >> 24) & 0xFF);
		c[1] = (byte) ((value >> 16) & 0xFF);
		c[2] = (byte) ((value >> 8) & 0xFF);
		c[3] = (byte) ((value >> 0) & 0xFF);
		return c;
	}

	public int compareTo(UnsignedNumber other) {
		long otherValue = other.longValue();
		if ((long) value > otherValue)
			return +1;
		else if ((long) value < otherValue)
			return -1;
		return 0;
	}

	public String toString() {
		return Long.toString((long) value & 0xFFFFFFFFL);
	}

}
