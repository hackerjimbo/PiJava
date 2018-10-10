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
 * This interface describes a matrix of monochrome Pixels. All implementations
 * must provide an implementation of the abstract method. This interface also
 * provides default methods for mapping the generic methods into the abstract
 * fast one.
 * 
 * @author Jim Darby
 */
public interface MonoMatrix extends Matrix <Integer>
{
    /**
     * Sets a pixel to a specific value. This should be the fastest
     * implementation.
     * 
     * @param x The X coordinate of the pixel.
     * @param y The Y coordinate of the pixel.
     * @param value The value to set in the range 0 to 255.
     */
    abstract public void setPixel (int x, int y, int value);
    
    /**
     * Sets a pixel to a specific value. The allows a double as the value to
     * make it easier to think in the 0 to 1 range for luminosity.
     * 
     * @param x The X coordinate of the pixel.
     * @param y The Y coordinate of the pixel.
     * @param value The value to set in the range 0.0 to 1.0.
     */
    default public void setPixel (int x, int y, double value)
    {
        if (value < 0 || value > 1)
            throw new IllegalArgumentException ("Invalid pixel value " + value);
        
        setPixel (x, y, (int) (value * 255 + 0.5));
    }
    
    /**
     * Sets a pixel to a specific value. This unboxes the Integer value.
     * 
     * @param x The X coordinate of the pixel.
     * @param y The Y coordinate of the pixel.
     * @param value The value to set in the range 0 to 255.
     */
    default public void setPixel (int x, int y, Integer value)
    {
        setPixel (x, y, value.intValue ());
    }
    
    /**
     * Sets a pixel to a specific value. This unboxes the Double value.
     * 
     * @param x The X coordinate of the pixel.
     * @param y The Y coordinate of the pixel.
     * @param value The value to set in the range 0 to 255.
     */
    default public void setPixel (int x, int y, Double value)
    {
        setPixel (x, y, value.doubleValue ());
    }

    /**
     * Sets a pixel to a specific value. This expands the Point.
     * 
     * @param p The pixel to set.
     * @param value The value to set in the range 0 to 255.
     */
    default public void setPixel (Point p, int value)
    {
        setPixel (p.getX (), p.getY (), value);
    }
    
    /**
     * Sets a pixel to a specific value. This expands the Point.
     * 
     * @param p The pixel to set.
     * @param value The value to set in the range 0 to 255.
     */
    default public void setPixel (Point p, double value)
    {
        setPixel (p.getX (), p.getY (), value);
    }
    
    /**
     * Sets a pixel to a specific value. This expands the point and unboxes the
     * Integer value.
     * 
     * @param p The pixel to set.
     * @param value The value to set in the range 0 to 255.
     */
    default public void setPixel (Point p, Integer value)
    {
        setPixel (p.getX (), p.getY (), value.intValue ());
    }
    
    /**
     * Sets a pixel to a specific value. This expands the Point and unboxes the
     * Double.
     * 
     * @param p The pixel to set.
     * @param value The value to set in the range 0.0 to 1.0.
     */
    default public void setPixel (Point p, Double value)
    {
        setPixel (p.getX (), p.getY (), value.doubleValue ());
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
    static final int BLACK = 0;
}
