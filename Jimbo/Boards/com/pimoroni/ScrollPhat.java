/*
 * Copyright (C) 2016-2017 Jim Darby.
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

import Jimbo.Graphics.Point;
import Jimbo.Graphics.BitMatrix;
import Jimbo.Graphics.BitMatrixDemo;

import Jimbo.Devices.IS31FL3730;

/**
 * This class controls a Pimoroni ScrollpHAT.
 * 
 * @author Jim Darby
 */
public class ScrollPhat implements BitMatrix
{    
    /**
     * Create a ScroolPhat object.
     * 
     * @throws IOException In case of issues.
     * @throws I2CFactory.UnsupportedBusNumberException In case it can't find
     * bus.
     */
    public ScrollPhat () throws IOException, I2CFactory.UnsupportedBusNumberException
    {
        phat =  new IS31FL3730 (I2CFactory.getInstance (I2CBus.BUS_1));
        
        // Set five by 11.
        phat.setMode (IS31FL3730.MODE_5X11 | IS31FL3730.MODE_M1);
        
        // Use 40mA drive
        phat.setLightingEffect (IS31FL3730.LER_40MA);
        
        // It's a Pimoroni device, default LEDs to quarter power to avoid
        // user blindness.
        phat.setPWM (IS31FL3730.MAX_PWM / 4);
        
        // Clear it
        for (int i = 0; i < data.length; ++i)
            data[i] = 0;
 
        show ();
    }
    
    /**
     * Update the displayed data. Call this after setting up what you want
     * displayed and it will transfer it to the device and hence actually
     * display it.
     * 
     * @throws IOException In case of problems.
     */
    @Override
    public final void show () throws IOException
    {
        phat.fastUpdateM1 (data);
    }
    
    /**
     * Set a specific pixel on or off.
     * 
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param on True for on, otherwise false.
     */
    @Override
    public void setPixel (int x, int y, boolean on)
    {
        if (x < 0 || x > MAX_X || y < 0 || y > MAX_Y)
            throw new IllegalArgumentException ("Invalid co-ordinates for set");
        
        if (flip_x)
            x = MAX_X - x;
        
        // We flip around Y because the board is wired "upside down".
        if (!flip_y)
            y = MAX_Y - y;
        
        if (on)
            data[x] |= (1 << y);
        else
            data[x] &= ~(1 << y);
    }
    
    
    /**
     * Set a pixel in the generic way.
     * 
     * @param p The pixel.
     * @param value The value.
     */
    @Override
    public void setPixel(Point p, Boolean value) 
    {
        setPixel (p, value.booleanValue ());
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
    
    /**
     * Set the number of tries before logging a warning. Note this will
     * never log if there isn't a retry.
     * 
     * @param n The number of tries, must be non-negative.
     * 
     * @throws IOException On a bad paramters.
     */
    public void setTriesWarning (int n) throws IOException
    {
        phat.setTriesWarning (n);
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
    
    /** The device width. */
    public static final int WIDTH = 11;
    /** The device height. */
    public static final int HEIGHT = 5;
    
    /**
     * The maximum x value. X coordinates must be in the range 0 to this
     * INCLUSIVE.
     */
    private static final int MAX_X = WIDTH - 1;
    /**
     * The maximum y value. Y coordinates must be in the range 0 to this
     * INCLUSIVE.
     */
    private static final int MAX_Y = HEIGHT - 1;
    
    /** The maximum values as a Point. */
    private final static Point MAX = new Point (MAX_X, MAX_Y);
    
    /** Internal pointer to the low-level device. */
    private final IS31FL3730 phat;
    /** Data we hold for the display. */
    private final byte data[] = new byte[12];
    /** Do we flip the x coordinate? */
    private boolean flip_x = false;
    /** Do we flip the y coordinate? */
    private boolean flip_y = false;

    /**
     * Test routine.
     * 
     * @param args the command line arguments
     * @throws java.io.IOException In case of problem.
     * @throws java.lang.InterruptedException In case of problem.
     * @throws com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException In case of problem.
     */
    public static void main(String[] args) throws IOException, InterruptedException, I2CFactory.UnsupportedBusNumberException
    {
        ScrollPhat s = new ScrollPhat ();
        
        s.setTriesWarning (0);
        
        BitMatrixDemo.run (s);
    }
}
