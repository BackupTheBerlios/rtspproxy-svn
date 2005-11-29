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
public abstract class UnsignedNumber extends Number {
	/**
	 * Get a byte array representation of the number. The order must be MSB
	 * first (Big Endian).
	 * 
	 * @return the serialized number
	 */
	public abstract byte[] getBytes();

	public abstract String toString();
	
	public abstract int compareTo(UnsignedNumber other);

	public String toHexString() {
		StringBuilder sb = new StringBuilder();
		sb.append("0x");
		for (byte b : getBytes())
			sb.append(hexLetters[(byte) ((b >> 4) & 0x0F)]).append(
					hexLetters[b & 0x0F]);
		return sb.toString();
	}

	private static final char[] hexLetters = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
}
