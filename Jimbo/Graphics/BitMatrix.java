/*
 * Copyright (C) 2017, 2018 Jim Darby.
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
 * This interface describes a matrix of bit values. All implementations must
 * provide an implementation of the abstract method. This interface also
 * provides default methods for mapping the generic methods into the abstract
 * fast one.
 * 
 * @author Jim Darby
 */
public interface BitMatrix extends Matrix <Boolean>
{
    /**
     * Sets a pixel to a specific colour. This me be implemented to provide the
     * fastest implementation.
     * 
     * @param x The X coordinate of the pixel.
     * @param y The Y coordinate of the pixel.
     * @param on If the pixel is on.
     */
    abstract public void setPixel (int x, int y, boolean on);
    
    /**
     * Sets a pixel to a specific value. This maps the canonical interface to
     * the optimised specific one.
     * 
     * @param p The point the set/clear.
     * @param on If the pixel is on.
     */
    @Override
    default public void setPixel (Point p, Boolean on)
    {
	setPixel (p.getX (), p.getY (), on);
    }
    
    /**
     * Clear (blank) a pixel at a specific point.
     * 
     * @param p The pixel to clear.
     */
    @Override
    default void clearPixel (Point p)
    {
        setPixel (p, BLACK);
    }
    
    /** The value used to clear a pixel. */
    static final boolean BLACK = false;
}
