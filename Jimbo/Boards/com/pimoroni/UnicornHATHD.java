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

import java.util.Arrays;
import java.io.IOException;

import Jimbo.Graphics.Point;
import Jimbo.Graphics.Colour;
import Jimbo.Graphics.ColourMatrix;
import Jimbo.Graphics.ColourMatrixDemo;

import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;

/**
 * This class controls the Pimoroni Unicorn HAT HD.
 * 
 * @author Jim Darby
 */
public class UnicornHATHD implements ColourMatrix 
{
    /**
     * Construct a UnicornHATHD object.
     * 
     * @throws IOException In case of error.
     */
    public UnicornHATHD () throws IOException
    {
        dev = SpiFactory.getInstance (SpiChannel.CS0, 9000000, SpiDevice.DEFAULT_SPI_MODE);
        Arrays.fill (data, (byte) 0);
        data[0] = 0x72;
        show ();
    }
    
    /**
     * Sets a pixel to a specific colour. This is implemented in the fastest
     * possible way.
     *
     * @param x The X coordinate (0 to 15).
     * @param y The Y coordinate (0 to 15).
     * @param r The red value: 0 to 255.
     * @param g The green value: 0 to 255.
     * @param b The blue value: 0 to 255.
     */
    @Override
    public void setPixel (int x, int y, int r, int g, int b)
    {
        if (x < 0 || x > MAX_X || y < 0 || y > MAX_Y)
            throw new IllegalArgumentException ("Invalid coordinates for setPixel");
        
        if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255)
            throw new IllegalArgumentException ("Invalid colour for setPixel");
        
        final int base = (x + WIDTH * (MAX_Y - y)) * 3 + 1;
        
        data[base    ] = (byte) r;
        data[base + 1] = (byte) g;
        data[base + 2] = (byte) b;
    }
    
    /**
      * Set a specific pixel to a specific Colour value. This is the generic
      * interface.
      * 
      * @param p The point to set.
      * @param value The colour to set it to.
      */
    @Override
    public void setPixel(Point p, Colour value)
    {
        setPixel (p.getX (), p.getY (),
                value.getRed (), value.getGreen (), value.getBlue ());
    }
    
    /**
      * Update the display.
      */
    @Override
    public final void show () throws IOException
    {
        dev.write (data, 0, data.length);
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
     * @throws java.io.IOException In case of trouble.
     */
    public static void main (String args[]) throws InterruptedException, IOException
    {
        final UnicornHATHD u = new UnicornHATHD ();
        
        ColourMatrixDemo.run (u);
    }
    
    /** The width of the board. */
    public static final int WIDTH = 16;
    /** The height of the board. */
    public static final int HEIGHT = 16;
    /** The maximum X value. */
    public static final int MAX_X = WIDTH - 1;
    /** The maximum Y value. */
    public static final int MAX_Y = HEIGHT - 1;
    
    /** The maximum values as a Point. */
    private final static Point MAX = new Point (MAX_X, MAX_Y);
    
    /** Data for the hat. */
    private final byte data[] = new byte [WIDTH * HEIGHT * 3 + 1];
    /** The SPI device we're going to use. */
    private final SpiDevice dev;
}
