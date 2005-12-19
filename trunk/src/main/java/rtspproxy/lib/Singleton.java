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

package rtspproxy.lib;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Simple base implementation of the Singleton pattern. A singleton is a class
 * that can have only one instance.
 * 
 * @author Matteo Merli
 */
public abstract class Singleton
{

	private static Set<Class> classSet = Collections.synchronizedSet( new HashSet<Class>() );

	/**
	 * Constructor. Takes care that only one instance at a time of this class is
	 * present.
	 */
	protected Singleton()
	{
		if ( classSet.contains( this.getClass() ) ) {
			throw new RuntimeException( "There can be only one instance of class "
					+ this.getClass().getName() );
		}

		classSet.add( this.getClass() );
	}

	public void finalize()
	{
		classSet.remove( this.getClass() );
	}

}
