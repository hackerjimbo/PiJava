/*
 * Copyright (C) 2016 Jim Darby.
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

package Jimbo.Boards.com.pimoroni;

import java.io.IOException;

import Jimbo.Devices.SN3218;
import Jimbo.Devices.Pi2C;

/**
 * This class allows control of the Piglow from Pimoroni.
 * 
 * The device has three legs each of six LEDs. The LEDs are coloured (from
 * inside to outside) white, blue, green, yellow, orange and red.
 * 
 * @author Jim Darby.
 */
public class Piglow
{
    /**
     * Construct a Piglow controller.
     * 
     * @throws IOException In case of difficulty.
     * @throws InterruptedException In case of difficulty.
     */
    public Piglow () throws IOException, InterruptedException
    {
        pg = new SN3218 (Pi2C.useBus ());
    }
    
    /**
     * Set a specific LED to a specific value. The mapping from LED number to
     * which one it is is a little curious so it's often better to use the more
     * nuanced routines.
     * 
     * @param led The LED to set.
     * @param value The value to set it to.
     * @throws IOException In case of error.
     */
    public void set (int led, int value) throws IOException
    {
        pg.set (led, value);
    }
    
    /**
     * Set all the red LEDs to a specific value.
     * 
     * @param value The value to set.
     * @throws IOException In case of error.
     */
    public void setReds (int value) throws IOException
    {
        set (REDS, value);
    }
    
    /**
     * Set all the orange LEDs to a specific value.
     * 
     * @param value The value to set.
     * @throws IOException In case of error.
     */
    public void setOranges (int value) throws IOException
    {
        set (ORANGES, value);
    }
 
    /**
     * Set all the yellow LEDs to a specific value.
     * 
     * @param value The value to set.
     * @throws IOException In case of error.
     */
    public void setYellows (int value) throws IOException
    {
        set (YELLOWS, value);
    }
 
    /**
     * Set all the green LEDs to a specific value.
     * 
     * @param value The value to set.
     * @throws IOException In case of error.
     */
    public void setGreens (int value) throws IOException
    {
        set (GREENS, value);
    }
 
    /**
     * Set all the blue LEDs to a specific value.
     * 
     * @param value The value to set.
     * @throws IOException In case of error.
     */
    public void setBlues (int value) throws IOException
    {
        set (BLUES, value);
    }
 
    /**
     * Set all the white LEDs to a specific value.
     * 
     * @param value The value to set.
     * @throws IOException In case of error.
     */
    public void setWhites (int value) throws IOException
    {
        set (WHITES, value);
    }

    /**
     * Set all the LEDs on leg 0 to a specific value.
     * 
     * @param value The value to set.
     * @throws IOException In case of error.
     */
    public void setLeg0 (int value) throws IOException
    {
        set (LEG0, value);
    }
 
    /**
     * Set all the LEDs on leg 1 to a specific value.
     * 
     * @param value The value to set.
     * @throws IOException In case of error.
     */
    public void setLeg1 (int value) throws IOException
    {
        set (LEG1, value);
    }
    
    /**
     * Set all the LEDs on leg 2 to a specific value.
     * 
     * @param value The value to set.
     * @throws IOException In case of error.
     */
    public void setLeg2 (int value) throws IOException
    {
        set (LEG2, value);
    }
    
    /**
     * Set all the LEDs on a given leg to a specific value.
     * 
     * @param leg The leg to set.
     * @param value The value to set.
     * @throws IOException In case of error.
     */
    public void setLeg (int leg, int value) throws IOException
    {
        if (leg < 0 || leg >= LEGS.length)
            throw new IOException ("Invalid leg given");
        
        set (LEGS[leg], value);
    }

    /**
     * Set a leg to a series of values.
     * 
     * @param leg The leg to use.
     * @param values The values to use.
     * @throws IOException In case of trouble.
     */
    public void setLeg (int leg, int values[]) throws IOException
    {
        if (leg < 0 || leg >= LEGS.length)
            throw new IOException ("Invalid leg given");
        
        if (LEGS[leg].length != values.length)
            throw new IOException ("Invalid leg data given");
        
        for (int i = 0; i < values.length; ++i)
            set (LEGS[leg][i], values[i]);
    }
        
    /**
     * Set all the legs. This should be an array of type int[3][6].
     * 
     * @param values The array of information. It should be of type int[3][6].
     * @throws IOException In case of trouble.
     */
    public void setLegs (int values[][]) throws IOException
    {
        for (int i = 0; i < values.length; ++i)
            setLeg (i, values[i]);
    }
    
    /**
     * Set a number of LEDs to a specific value.
     * 
     * @param leds The LEDs to set.
     * @param value The value to set.
     * @throws IOException In case of error.
     */
    public void set (byte[] leds, int value) throws IOException
    {
        for (int i = 0; i < leds.length; ++i)
            set (leds[i], value);
    }
 
    /**
     * Update the Piglow from the set data.
     * 
     * @throws IOException in case of error.
     */
    public void update () throws IOException
    {
        pg.update ();
    }

    /** The device we're working with. */
    private final SN3218 pg;
    
    /** All the red LEDs. */
    private static final byte[] REDS    = { 6, 17,  0 };
    /** All the orange LEDs. */
    private static final byte[] ORANGES = { 7, 16,  1 };
    /** All the yellow LEDs. */
    private static final byte[] YELLOWS = { 8, 15,  2 };
    /** All the green LEDs. */
    private static final byte[] GREENS  = { 5, 13,  3 };
    /** All the blue LEDs. */
    private static final byte[] BLUES   = { 4, 11, 14 };
    /** All the white LEDs. */
    private static final byte[] WHITES  = { 9, 10, 12 };
    
    /** All the LEDs on leg 0. */
    private static final byte[] LEG0    = {  9,  4,  5,  8,  7,  6 };
    /** All the LEDs on leg 1. */
    private static final byte[] LEG1    = { 10, 11, 13, 15, 16, 17 };
    /** All the LEDs on leg 2. */
    private static final byte[] LEG2    = { 12, 14,  3,  2,  1,  0 };
    /** All the legs. */
    private static final byte[][] LEGS  = { LEG0, LEG1, LEG2 };
    /**All the rings. */
    private static final byte[][] RINGS = { WHITES, BLUES, GREENS, YELLOWS, ORANGES, REDS };
    
    
    public static void main (String args[]) throws IOException, InterruptedException
    {
        Piglow pg = new Piglow ();
        
        for (int i = 0; i < 18; ++i)
        {
            System.out.println (i);
            pg.set (i, 40);
            pg.update ();
            Thread.sleep (100);
            pg.set (i, 0);
            pg.update ();
            Thread.sleep (100);
        }
        
        pg.setReds (255);
        pg.update ();
        Thread.sleep (100);
        pg.setReds (0);
        
        pg.setOranges (255);
        pg.update ();
        Thread.sleep (100);
        pg.setOranges (0);
        
        pg.setYellows (255);
        pg.update ();
        Thread.sleep (100);
        pg.setYellows (0);
        
        pg.setGreens (255);
        pg.update ();
        Thread.sleep (100);
        pg.setGreens (0);
        
        pg.setBlues (255);
        pg.update ();
        Thread.sleep (100);
        pg.setBlues (0);
        
        pg.setWhites (255);
        pg.update ();
        Thread.sleep (100);
        pg.setWhites (0);

        pg.setLeg0 (255);
        pg.update ();
        Thread.sleep (100);
        pg.setLeg0 (0);
        
        pg.setLeg1 (255);
        pg.update ();
        Thread.sleep (100);
        pg.setLeg1 (0);
        
        pg.setLeg2 (255);
        pg.update ();
        Thread.sleep (100);
        pg.setLeg2 (0);
        pg.update ();
        
        while (true)
        {
            for (int step = 0; step < 256; ++step)
            {
                final double offset = (Math.PI * step) / 255;
                
                pg.setReds    (toLed (Math.sin (Math.PI * 0 / 6 + offset)));
                pg.setOranges (toLed (Math.sin (Math.PI * 1 / 6 + offset)));
                pg.setYellows (toLed (Math.sin (Math.PI * 2 / 6 + offset)));
                pg.setGreens  (toLed (Math.sin (Math.PI * 3 / 6 + offset)));
                pg.setBlues   (toLed (Math.sin (Math.PI * 4 / 6 + offset)));
                pg.setWhites  (toLed (Math.sin (Math.PI * 5 / 6 + offset)));
                pg.update     ();
                Thread.sleep (4);
            }
        }
    }
    
    private static int toLed (double value)
    {
        if (value < 0)
            value = -value;
        
        return (int) (255 * value);
    }
}
