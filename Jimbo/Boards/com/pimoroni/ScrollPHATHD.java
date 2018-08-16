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
 * License along with this library.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package Jimbo.Boards.com.pimoroni;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;

import Jimbo.Devices.IS31FL3731;

import Jimbo.Graphics.Point;
import Jimbo.Graphics.MonoMatrix;
import Jimbo.Graphics.MonoMatrixDemo;
import Jimbo.Graphics.MatrixHelper;

/**
 * This class controls a Pimoroni ScrollpHAT HD.
 * 
 * @author Jim Darby
 */
public class ScrollPHATHD extends MatrixHelper <Integer> implements MonoMatrix
{
   /**
     * Create a ScrollPHATHD object.
     * 
     * @throws IOException In case of issues.
     * @throws I2CFactory.UnsupportedBusNumberException In case it can't find
     * the correct I2C bus.
     * @throws java.lang.InterruptedException In case of issues.
     */
    public ScrollPHATHD () throws IOException, I2CFactory.UnsupportedBusNumberException, InterruptedException
    {
        super (17, 7);
        
        phat =  new IS31FL3731 (I2CFactory.getInstance (I2CBus.BUS_1), 0x74);
    }
    
   /**
     * Update the displayed data. Call this after setting up what you want
     * displayed and it will transfer it to the device and hence actually
     * display it.
     * 
     * @throws IOException In case of problems.
     */
    @Override
    public void show () throws IOException
    {
        phat.update ();
    }
    
    /**
     * Set a specific pixel on or off. This works in the most efficient
     * way.
     * 
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param pwm The PWM value.
     */
    @Override
    public void setPixel (int x, int y, int pwm)
    {
        if (x < 0 || x > MAX_X || y < 0 || y > MAX_Y)
            throw new IllegalArgumentException ("Invalid co-ordinates for set");
        
        if (flip_x)
            x = MAX_X - x;
        
        if (flip_y)
            y = MAX_Y - y;
        
        // Piratical wiring madness!
        
        if (x >= 8)
        {
            x = (x - 8) * 2;
            y = MAX_Y - y;
        }
        else
        {
            x = 15 - x * 2;
        }
        
        phat.setLed (0, x * 8 + y, pwm);
    }
    
    /**
     * Set a pixel in the generic way.
     * 
     * @param p The pixel.
     * @param value The value.
     */
    @Override
    public void setPixel (Point p, Integer value)
    {
        setPixel (p.getX (), p.getY (), value);
    }
    
    /**
     * Optionally flip the x, y or both arguments. Useful for rotating
     * and other general diddling of the display,
     * 
     * @param x Flip the x coordinates?
     * @param y Flip the y coordinates?
     */
    public void flip (boolean x, boolean y)
    {
        flip_x = x;
        flip_y = y;
    }

    public static void main (String args[]) throws IOException, I2CFactory.UnsupportedBusNumberException, InterruptedException
    {
        ScrollPHATHD p = new ScrollPHATHD ();
        
        MonoMatrixDemo.run (p);
    }
    
    /** The device itself. */
    private final IS31FL3731 phat;
    /** Flag to flip the x coordinate. */
    private boolean flip_x = false;
    /** Flag to flip the y coordinate. */
    private boolean flip_y = false;
}
