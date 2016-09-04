/*
 * Copyright (C) 2016 Jim Darby.
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

import com.pi4j.io.i2c.I2CBus;

import Jimbo.Devices.Pi2C;
import Jimbo.Devices.IS31FL3730;
import java.io.IOException;

/**
 * This class interfaces to the Pimoroni Micro Dot pHAT. It consists of three
 * IS31FL3730 controller chips wired to six displays in a delightfully unusual
 * manner. I blame the grog!
 * 
 * @author Jim Darby
 */
public class MicroDotPHAT
{
    /**
     * Constructor - builds an object to handle the device. Only one may be used
     * at once.
     * 
     * @throws IOException In case of error
     * @throws InterruptedException In case of error
     */
    public MicroDotPHAT () throws IOException, InterruptedException
    {
        final I2CBus bus = Pi2C.useBus ();
    
        for (int i = 0; i < driver.length; ++i)
        {
            driver[i] = new IS31FL3730 (bus, ADDRS[i]);
            driver[i].setMode (IS31FL3730.MODE_M12 | IS31FL3730.MODE_8X8);
            driver[i].setLightingEffect (IS31FL3730.LER_35MA);
            driver[i].setPWM (128);
            
            for (int j = 0; j < 8; ++j)
            {
                m1[i][j] = 0;
                m2[i][j] = 0;
            }
            
            driver[i].fastUpdateM1 (m1[i]);
            driver[i].fastUpdateM2 (m2[i]);
            driver[i].update ();
        }
    }
    
    /**
     * Set a single bit on the display. The origin is at the bottom left, as it
     * should be.
     * 
     * @param x The x coordinate, range 0 to 29.
     * @param y The y coordinate, range 0 to 6.
     * @param on Make the pixel light up?
     * @throws IOException In case of issues.
     */
    public void set (int x, int y, boolean on) throws IOException
    {
        // Validate parameters
        if ( x< 0 || x >= WIDTH || y < 0 || y >= HEIGHT)
            throw new IOException ("Invalid coordidinates");
        
        // Now figure out the display and the coordinates on it.
        final int index = x / 10;
        final boolean left = (x % 10) < 5;
        final int offset = x % 5;
        final int y_flip = HEIGHT - 1 - y;
        
        // Now map it into the device
        if (left)
        {
            if (on)
                m2[index][offset] |= (byte) (1 << y_flip);
            else
                m2[index][offset] &= (byte) ~(1 << y_flip);
        }
        else
        {
            if (on)
                m1[index][y_flip] |= (byte) (1 << offset);
            else
                m1[index][y_flip] &= (byte) ~(1 << offset);
        }
    }
    
    /**
     * Set a decimal point on the display.
     * 
     * @param n Which one. 0 is leftmost, 5 is rightmost.
     * @param on Turn it on or off? True for on, false for off.
     * @throws IOException In case of error.
     */
    public void setPoint (int n, boolean on) throws IOException
    {
        if (n < 0 || n > 5)
            throw new IOException ("Invalid decimal point");
        
        // Now figure out the display and the coordinates on it.
        final int index = n / 2;
        final boolean left = (n % 2) == 0;

        // Now map it into the device
        if (left)
        {
            if (on)
                m2[index][7] |= (byte) (0x40);
            else
                m2[index][7] &= (byte) ~(0x40);
        }
        else
        {
            if (on)
                m1[index][6] |= (byte) 0x80;
            else
                m1[index][6] &= (byte) ~(0x80);
        }
    }
    
    /**
     * Update the physical device.
     * @throws IOException In case of problems.
     */
    public void update () throws IOException
    {       
        for (int i = 0; i < driver.length; ++i)
        {
            driver[i].fastUpdateM1 (m1[i]);
            driver[i].fastUpdateM2 (m2[i]);
            driver[i].update ();
        }
    }
   
    /**
     * Set the PWM of the board.
     * @param pwm Bright nice in range 0 to 128 (yes, 129 levels).
     * @throws IOException In case of trouble.
     */
    public void setPWM (int pwm) throws IOException
    {
        // Rather naughtily we let the lower level routines do the range checking.
        for (int i = 0; i < driver.length; ++i)
            driver[i].setPWM (pwm);
    }
    
    /**
     * Basic test routine.
     * @param args Command line arguments, ignored.
     * @throws IOException In case of error.
     * @throws InterruptedException In case of error.
     */
    public static void main (String[] args) throws IOException, InterruptedException
    {
        final MicroDotPHAT m = new MicroDotPHAT ();
        boolean setting = true;
        int pwm = 128;
    
        while (true)
        {
            for (int i = 0; i < 6; ++i)
            {
                m.setPoint (i, setting);
                m.update ();
                Thread.sleep (200);
            }
            
            for (int y = 0; y < HEIGHT; ++y)
                for (int x = 0; x < WIDTH; ++x)
                {
                    m.setPWM (pwm);

                    pwm -= 1;

                    if (pwm < 0)
                        pwm = 128;
            
                    m.set (x, y, setting);
                    m.update ();
                    
                    Thread.sleep (5);
                }
            
            setting = !setting;
        }
    }
    
    /** The width of the device. */
    public static final int WIDTH = 30;
    /** The height of the device. */
    public static final int HEIGHT = 7;
    
    /** The addresses, left to right, of the chips */
    private static final int[] ADDRS = { 0x63, 0x62, 0x61 };
    /** Our drivers for each chip. */
    private final IS31FL3730 driver[] = new IS31FL3730[ADDRS.length];
    
    /** Local cache of M1 data. */
    private final byte[][] m1 = new byte[driver.length][8];
    /** Local cache of M2 data. */
    private final byte[][] m2 = new byte[driver.length][8];
}
