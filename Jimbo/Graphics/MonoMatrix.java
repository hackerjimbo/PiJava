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
 * This interface describes a matrix of monochrome Pixels. One of
 * setPixel (Point p, int value) or setPixel (int x, int y, int value) must be
 * implemented to avoid infinite recursion.
 * 
 * @author Jim Darby
 */
public interface MonoMatrix extends Matrix <Integer>
{
    /**
     * Sets a pixel to a specific value.
     * 
     * @param p The address of the Pixel.
     * @param value The value to set in the range 0 to 255.
     */
    default public void setPixel (Point p, int value)
    {
        setPixel (p.getX (), p.getY (), value);
    }
    
    /**
     * Sets a pixel to a specific value.
     * 
     * @param x The X coordinate of the pixel.
     * @param y The Y coordinate of the pixel.
     * @param value The value to set in the range 0 to 255.
     */
    default public void setPixel (int x, int y, int value)
    {
        setPixel (new Point (x, y), value);
    }
    
    /**
     * Sets a pixel to a specific value.
     * 
     * @param x The X coordinate of the pixel.
     * @param y The Y coordinate of the pixel.
     * @param value The value to set in the range 0.0 to 1.0.
     */
    default public void setPixel (int x, int y, double value)
    {
        if (value < 0 || value > 1)
            throw new IllegalArgumentException ("Invalid pixel value " + value);
        
        setPixel (x, y, new Double (value * 255).intValue ());
    }
    
    /**
     * Sets a pixel to a specific value.
     * 
     * @param p The pixel to set.
     * @param value The value to set in the range 0.0 to 1.0.
     */
    default public void setPixel (Point p, double value)
    {
        if (value < 0 || value > 1)
            throw new IllegalArgumentException ("Invalid pixel value " + value);
        
        setPixel (p, new Double (value * 255).intValue ());
    }
}
