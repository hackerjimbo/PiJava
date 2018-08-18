/*
 * Copyright (C) 2016, 2018 Jim Darby.
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

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;

import Jimbo.Devices.APA102;

import Jimbo.Graphics.Point;
import Jimbo.Graphics.Colour;
import Jimbo.Graphics.ColourMatrix;
import Jimbo.Graphics.ColourMatrixDemo;
import Jimbo.Graphics.MatrixHelper;

import java.io.IOException;

/**
 * This class drives a Pimoroni Blinkt LED string.
 *
 * @author Jim Darby
 */
public class Blinkt extends MatrixHelper <Colour> implements ColourMatrix
{
    public Blinkt ()
    {
        super (8, 1);
        
        a = new APA102 (GpioFactory.getInstance(), RaspiPin.GPIO_04, RaspiPin.GPIO_05, 8);
    }
    
   /**
     * Set a pixel the generic way.
     * 
     * @param p The pixel to set.
     * @param value The colour to set it to.
     */
    @Override
    public void setPixel(Point p, Colour value)
    {
        if (p.getY () != 0)
            throw new IllegalArgumentException ("Invalid Y coordinate");
        
        set (p.getX (), value.getRed (), value.getGreen (), value.getBlue (), 31);
    }
    
   /**
     * Set a LED to a specific red, green and blue value. We also set the
     * brightness.
     * 
     * @param n The LED number, in the range 0 to the number of LEDs minus one.
     * @param r The red value (0 to 255).
     * @param g The green value (0 to 255).
     * @param b The blue value (0 to 255).
     * @param bright The brightness (0 to 31).
     */
    public void set (int n, int r, int g, int b, int bright)
    {
        a.set (n, r, g, b, bright);
    }
    
    /**
     * Update the LED chain.
     */
    @Override
    public final void show ()
    {
        a.show ();
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
        final Blinkt b = new Blinkt ();
        
        ColourMatrixDemo.run (b);
    }
    
    /** Internal pointer to the hat. */
    private final APA102 a;
}
