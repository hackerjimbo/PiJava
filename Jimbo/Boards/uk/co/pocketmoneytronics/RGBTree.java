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
 * License along with this library.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package Jimbo.Boards.uk.co.pocketmoneytronics;

import Jimbo.Devices.WS2811.WS2811;
import Jimbo.Devices.WS2811.WS2811Raw;

import Jimbo.Graphics.Point;
import Jimbo.Graphics.Identity;
import Jimbo.Graphics.Colour;

/**
 * The class controls the WS2811-based RGB Christmas tree from
 * PocketMoneyTronics.
 * 
 * @author Jim Darby
 */
public class RGBTree
{
    /**
     * Construct the tree.
     */
    public RGBTree ()
    {
        tree = new WS2811 (PIXELS, 1, new Identity (PIXELS, 1), WS2811Raw.WS2811_STRIP_RGB, 0.25);
    }
    
    /**
     * Set a specific pixel to a specific Colour value.
     * 
     * @param n The pixel number.
     * @param r The red value.
     * @param g The green value.
     * @param b The blue value.
     */
    public void setPixel (int n, int r, int g, int b)
    {
        tree.setPixel (new Point (n, 0), r, g, b);
    }
    
    /**
     * Set a specific pixel to a specific Colour value.
     * 
     * @param n The pixel number.
     * @param c The colour to set it to.
     */
    public void setPixel (int n, Colour c)
    {
        tree.setPixel (new Point (n, 0), c);
    }
    
    /**
     * Update the display.
     */
    public void show ()
    {
        tree.show ();
    }
    
    /**
     * Demo program.
     * 
     * @param args Command line parameters.
     * 
     * @throws java.lang.InterruptedException In case of interruption.
     */
    public static void main (String args[]) throws InterruptedException
    {
        RGBTree t = new RGBTree ();
        
        final Colour[] colours = new Colour[3];
        
        colours[0] = new Colour (255, 0, 0);
        colours[1] = new Colour (0, 255, 0);
        colours[2] = new Colour (0, 0, 255);
        
        for (int i = 0; i < colours.length; ++i)
        {
            for (int j = 0; j < PIXELS; ++j)
                t.setPixel (j, colours[i]);
            
            t.show ();
            
            Thread.sleep (500);
        }
    }
    
    /** Number of pixels. */
    public final static int PIXELS = 6;
    /** The tree. */
    private final WS2811 tree;
}
