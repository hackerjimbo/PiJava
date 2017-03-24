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
 * This interface describes a matrix of RGB Pixels.
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
    public void setPixel (Point p, int r, int g, int b);
    
    /**
     * Set a specific pixel to a specific Colour value.
     * 
     * @param p The point to set.
     * @param c The colour to set it to.
     */
    public void setPixel (Point p, Colour c);
    
    /**
     * Update the display.
     */
    public void show ();
}
