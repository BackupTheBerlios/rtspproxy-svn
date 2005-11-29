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
public final class UnsignedByte extends UnsignedNumber {
	static final long serialVersionUID = 1L;

	private short value;

	public UnsignedByte(byte c) {
		value = c;
	}

	public UnsignedByte(short c) {
		value = (short) (c & 0xFF);
	}

	public UnsignedByte(int c) {
		value = (short) (c & 0xFF);
	}

	public UnsignedByte(long c) {
		value = (short) (c & 0xFFL);
	}

	private UnsignedByte() {
		value = 0;
	}

	public static UnsignedByte fromBytes(byte[] c) {
		return fromBytes(c, 0);
	}

	public static UnsignedByte fromBytes(byte[] c, int idx) {
		UnsignedByte number = new UnsignedByte();
		if ((c.length - idx) < 1)
			throw new IllegalArgumentException(
					"An UnsignedByte number is composed of 1 byte.");

		number.value = (short) (c[0] & 0xFF);
		return number;
	}

	public static UnsignedByte fromString(String c) {
		UnsignedByte number = new UnsignedByte();
		char[] begin = new char[2];
		c.getChars(0, 2, begin, 0);
		long v = 0;
		if (begin[0] == '0' && (begin[1] == 'x' || begin[1] == 'X'))
			v = Short.parseShort(c.substring(2), 16);
		else
			v = Short.parseShort(c);
		number.value = (short) (v & 0xFF);
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
		return (short) (value & 0xFF);
	}

	@Override
	public int intValue() {
		return value & 0xFF;
	}

	@Override
	public long longValue() {
		return value & 0xFFL;
	}

	public byte[] getBytes() {
		byte[] c = { (byte) (value & 0xFF) };
		return c;
	}

	public int compareTo(UnsignedNumber other) {
		short otherValue = other.shortValue();
		if (value > otherValue)
			return +1;
		else if (value < otherValue)
			return -1;
		return 0;
	}

	public String toString() {
		return Short.toString(value);
	}

}
