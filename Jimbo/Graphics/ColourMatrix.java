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
 * This interface describes a matrix of RGB Pixels. Note that at least one of
 * setPixel (Point p, int r, int g, int b) and
 * setPixel (int x, int y, int r, int g, int b) must be defined to avoid
 * infinite recursion from the default implementations.
 * 
 * @author Jim Darby
 */
public interface ColourMatrix extends Matrix
{
    /**
     * Sets a pixel to a specific colour.
     * 
     * @param p The address of the Pixel.
     * @param r The red value: 0 to 255.
     * @param g The green value: 0 to 255.
     * @param b The blue value: 0 to 255.
     */
    default public void setPixel (Point p, int r, int g, int b)
    {
        setPixel (p.getX (), p.getY (), r, g, b);
    }
    
     /**
     * Sets a pixel to a specific colour.
     * 
     * @param x The X coordinate of the pixel.
     * @param y The Y coordinate of the pixel.
     * @param r The red value: 0 to 255.
     * @param g The green value: 0 to 255.
     * @param b The blue value: 0 to 255.
     */
    default public void setPixel (int x, int y, int r, int g, int b)
    {
        setPixel (new Point (x, y), r, g, b);
    }

    /**
     * Set a specific pixel to a specific Colour value.
     * 
     * @param p The point to set.
     * @param c The colour to set it to.
     */
    default public void setPixel (Point p, Colour c)
    {
        setPixel (p, c.getRed (), c.getGreen (), c.getBlue ());
    }
    
    /**
     * Sets a pixel to a specific colour.
     * 
     * @param x The X coordinate of the pixel.
     * @param y The Y coordinate of the pixel.
     * @param c The colour to set it to.
     */
    default public void setPixel (int x, int y, Colour c)
    {
        setPixel (x, y, c.getRed (), c.getGreen (), c.getBlue ());
    }
}
