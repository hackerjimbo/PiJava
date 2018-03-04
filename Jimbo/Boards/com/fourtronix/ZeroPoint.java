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
 * License along with this library.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package Jimbo.Boards.com.fourtronix;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.RaspiPin;

import com.pi4j.component.servo.Servo;
import com.pi4j.component.servo.ServoDriver;
import com.pi4j.component.ComponentBase;

/**
 * This class controls the 4tronix ZeroPoint.
 * 
 * @author Jim Darby.
 */

public class ZeroPoint extends ComponentBase implements Servo
{  
    /**
     * Construct a ZeroPoint object. There can be only one (TM) and we have
     * no way of checking one is actually wired up.
     */
    public ZeroPoint ()
    {
        moveto (0);
    }
    
    /**
     * Move to a specific step. This uses the original version of the values
     * with the range 0 to 600.
     * 
     * @param pos The position to move to.
     */
    public void moveto (int pos)
    {
        // Already there?
        if (pos == location)
            return;
        
        final boolean up = pos > location;
        final int steps = up ? (pos - location) : (location - pos);
        
        for (int i = 0; i < steps; ++i)
        {
            if (up)
            {
                phase += 1;
                
                if (phase >= SEQUENCE.length)
                    phase = 0;
            }
            else
            {
                phase -= 1;
                
                if (phase < 0)
                    phase = SEQUENCE.length - 1;
            }
            
            for (int j = 0; j < pins.length; ++j)
                pins[j].setState (SEQUENCE[phase][j]);
            
            try
            {
                // Lots of debate over the rate. 500 steps per second seems to
                // work all the time.
                
                Thread.sleep (2);
            }
            
            // That didn't got according to play.... But ignore it.
            catch (InterruptedException ex)
            {
            }
        }
        
        location = pos;
    }
        
    /**
     * Set the position using the Servo interface. It's not a servo but
     * seems to wat to act like one.
     * 
     * @param pos The position: -100 to +100.
     */
    @Override
    public void setPosition (float pos)
    {
        moveto ((int) ((pos + 100) / 200 * (MAX - MIN) + MIN));
    }

    /**
     * Return the current position in the range -100 to +100.
     * 
     * @return The position.
     */
    @Override
    public float getPosition ()
    {
        return (float) (((double) (location - MIN) / (MAX - MIN) - 0.5) * 200);
    }

    /**
     * Return the servo driver used. As we don't use on this returns null.
     * This may cause problems later.
     * 
     * @return The servo driver (null).
     */
    @Override
    public ServoDriver getServoDriver ()
    {
        return null;
    }

    /**
     * Implement the off option for the servo. This makes it all passive.
     */
    @Override
    public void off ()
    {
        for (int i = 0; i < pins.length; ++i)
            pins[i].setState (false);
    }
    
    /**
     * The SEQUENCE from Gareth. This doesn't appear to be the standard one.
     * There may be some odd wiring going on here. Or I just don't understand!
     */
    static private final boolean[][] SEQUENCE =
    {
        { true, false, false,  true},
        { true, false,  true, false},
        {false, true,   true, false},
        {false, true,  false,  true}
    };
    
    /** The GPIO controller we're going to use. */
    private final GpioController gpio = GpioFactory.getInstance();
    
    /** Our list of pins used. */
    private final GpioPinDigitalOutput pins[] =
    {
        gpio.provisionDigitalOutputPin (RaspiPin.GPIO_07, "A"),
        gpio.provisionDigitalOutputPin (RaspiPin.GPIO_00, "B"),
        gpio.provisionDigitalOutputPin (RaspiPin.GPIO_01, "C"),
        gpio.provisionDigitalOutputPin (RaspiPin.GPIO_02, "D")
    };
    
    /** Our current location. Start at 650 then we move to zero. */
    private int location = 650;
    /** Our current phase in the steps. */
    private int phase = 0;

    /** The minimum step value used. */
    public static final int MIN = 0;
    /** The maximum step value used. */
    public static final int MAX = 600;
    
    public static void main (String args[]) throws InterruptedException
    {
        ZeroPoint zp = new ZeroPoint ();
        
        System.out.println ("At " + zp.location + " aka " + zp.getPosition ());
        zp.moveto (MAX);
        System.out.println ("At " + zp.location + " aka " + zp.getPosition ());
        
        zp.moveto (MAX / 2);
        System.out.println ("At " + zp.location + " aka " + zp.getPosition ());
        Thread.sleep (500);
        
        zp.setPosition (-50);
        System.out.println ("At " + zp.location + " aka " + zp.getPosition ());
        Thread.sleep (1000);
                
        zp.setPosition (50);
        System.out.println ("At " + zp.location + " aka " + zp.getPosition ());
        Thread.sleep (1000);
        
        zp.setPosition (0);
        System.out.println ("At " + zp.location + " aka " + zp.getPosition ());
        Thread.sleep (1000);
    }
 }
 