/*
 * Copyright (C) 2017 Jim Darby.
 *
 * This software is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, If not, see
 * <http://www.gnu.org/licenses/>.
 */

package Jimbo.Boards.com.pimoroni;

import Jimbo.Devices.WS2811.WS2811;
import Jimbo.Devices.WS2811.WS2811Raw;

import Jimbo.Graphics.FlipY;
import Jimbo.Graphics.Point;
import Jimbo.Graphics.Colour;
import Jimbo.Graphics.ColourMatrixDemo;

/**
 * This class controls the Pimoroni Unicorn pHAT.
 * 
 * @author Jim Darby
 */
public class UnicornPhat implements Jimbo.Graphics.ColourMatrix
{
    /**
     * Construct a new UnicornPhat object. We can have only one though this
     * isn't checked.
     */
    public UnicornPhat ()
    {
        h = new WS2811 (WIDTH, HEIGHT, new FlipY (WIDTH, HEIGHT), WS2811Raw.WS2811_STRIP_GRB, 0.25);
    }
    
    /**
     * Sets a pixel to a specific colour.
     * 
     * @param p The address of the Pixel.
     * @param r The red value: 0 to 255.
     * @param g The green value: 0 to 255.
     * @param b The blue value: 0 to 255.
     */
    @Override
    public void setPixel (Point p, int r, int g, int b)
    {
        h.setPixel (p, r, g, b);
    }
    
    /**
     * Set a specific pixel to a specific Colour value.
     * 
     * @param p The point to set.
     * @param c The colour to set it to.
     */
    @Override
    public void setPixel (Point p, Colour c)
    {
        h.setPixel (p, c);
    }
    
    /**
     * Update the display.
     */
    @Override
    public void show ()
    {
        h.show ();
    }
    
    /**
     * Return a point with the maximum values for X and Y in this
     * matrix.
     * 
     * @return The maximum size.
     */
    @Override
    public Point getMax ()
    {
        return MAX;
    }
    
    /**
     * Run a simple test demo on the board.
     * 
     * @param args The command line arguments. They're ignored.
     * 
     * @throws InterruptedException If Thread.sleep gets interrupted.
     */
    public static void main (String args[]) throws InterruptedException
    {
        final UnicornPhat u = new UnicornPhat ();
        
        ColourMatrixDemo.run (u);
    }
    
    /** The width of the board. */
    public static final int WIDTH = 8;
    /** The height of the board. */
    public static final int HEIGHT = 4;
    
    /** Internal pointer to the hat. */
    private final WS2811 h;
    /** Internal Point contain the maximum X and Y values. */
    private static final Point MAX = new Point (WIDTH - 1, HEIGHT - 1);
}
