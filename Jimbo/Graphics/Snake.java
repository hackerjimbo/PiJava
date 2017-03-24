/*
 * Copyright (C) 2017 Jim Darby.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, see
 * <http://www.gnu.org/licenses/>.
 */

package Jimbo.Graphics;

/**
 * The class performs a "snake" mapping of a point. This is very useful when
 * the wiring of pixels is a little "unusual". It flips the X value but only on
 * alternate lines. This allows a layout such as:
 * 
 * 15 14 13 12
 * 08 09 10 11
 * 07 06 05 04
 * 00 01 02 03
 * 
 * @author Jim Darby.
 */
public class Snake extends Mapping
{
    /**
     * Create a mapping given the width and height of the input. Note that the
     * valid X coordinates are from 0 to width - 1 and the valid Y coordinates
     * are from 0 to height - 1.
     * 
     * @param width The input width.
     * @param height The input height.
     */
    public Snake (int width, int height)
    {
	super (new Point (width - 1, height - 1));
    }

    /**
     * Create a mapping given a previous mapping. The width and height are
     * inherited from the previous item.
     * 
     * @param before The previous mapping.
     */
    public Snake (Mapping before)
    {
	super (before, before.getOutMax ());
    }
    
    /**
     * Perform a mapping. The flips odd numbered X lines.
     * 
     * @param p The input point.
     * @return The mapped result.
     */
    @Override
    public Point map (Point p)
    {
	if (before != null)
	    p = before.map (p);
	
	validateIn (p);

	final Point result = ((p.getY () & 1) != 0) ? new Point (getInMax ().getX () - p.getX (), p.getY ()) : p;

	validateOut (result);
	
	return result;
    }

    /**
     * Return a printable form of the mapping.
     * 
     * @return The String representation.
     */
    @Override
    public String toString ()
    {
	String result = "Snake from " + getInMax () + " to " + getOutMax ();

	if (before != null)
	    result = before.toString () + ' ' + result;
	
	return result;
    }
}

