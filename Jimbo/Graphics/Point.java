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
 * This class represents a point on the x,y plane. It's held as a purely
 * integer point where both x and y are greater then or equal to zero.
 * 
 * @author Jim Darby
 */
public class Point
{
    /**
     * Create a point with the given x and y coordinates. Both of these
     * must be greater than or equal to zero as they represent a point on a
     * display.
     * 
     * @param x The x coordinate.
     * @param y The y coordinate.
     */
    public Point (int x, int y)
    {
	if (x < 0 || y < 0)
	    throw new IllegalArgumentException ("Negative co-ordinate in (" + x + ',' + y + ')');
	
	this.x = x;
	this.y = y;
    }
    
    /**
     * Get the x coordinate.
     * 
     * @return The x coordinate.
     */
    public int getX ()
    {
	return x;
    }
    
    /**
     * Get the y coordinate.
     * 
     * @return The y coordinate.
     */
    public int getY ()
    {
	return y;
    }
    
    /**
     * Return the item as a string contain both the x and y coordinates.
     * 
     * @return A string representation.
     */
    @Override
    public String toString ()
    {
	return "(" + x + ',' + y + ')';
    }

    /**
     * Is a given point "inside" this point. This is better defined as the
     * points coordinates both being less than or equal to our coordinates.
     * 
     * @param p The point's coordinates.
     * @return If the parameter's x and y coordinates are both less than or
     * equal to ours.
     */
    public boolean inside (Point p)
    {
	return x <= p.x && y <= p.y;
    }
    
    /** Our x coordinate. */
    private final int x;
    /** Our y coordinate. */
    private final int y;
}
