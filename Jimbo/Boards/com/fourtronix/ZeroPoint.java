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
//import com.pi4j.io.gpio.Pin;

//import java.io.IOException;
//import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class controls the 4tronix ZeroPoint.
 * 
 * @author Jim Darby.
 */

public class ZeroPoint extends ComponentBase implements Servo
{  
    public ZeroPoint ()
    {
        moveto (0);
    }
    
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
                
                if (phase >= sequence.length)
                    phase = 0;
            }
            else
            {
                phase -= 1;
                
                if (phase < 0)
                    phase = sequence.length - 1;
            }
            
            for (int j = 0; j < pins.length; ++j)
            {
                //System.out.print (pins[j].getName() + " to " + sequence[phase][j] + ' ');
                pins[j].setState (sequence[phase][j]);
            }
            
            //System.out.println ();
            
            try
            {
                // Once we've got going we can go faster. Let's have some magic
                // values!
                
                Thread.sleep (2);
            }
            
            catch (InterruptedException ex)
            {
                Logger.getLogger(ZeroPoint.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        location = pos;
    }
        
    @Override
    public void setPosition (float f)
    {
        moveto ((int) ((f + 100) / 200 * (MAX - MIN) + MIN));
    }

    @Override
    public float getPosition ()
    {
        return (float) (((double) (location - MIN) / (MAX - MIN) - 0.5) * 200);
    }

    @Override
    public ServoDriver getServoDriver ()
    {
        return null;
    }
    
    @Override
    public void clearProperties ()
    {
    }

    final boolean[][] sequence =
    {
        { true, false, false,  true},
        { true, false,  true, false},
        {false, true,   true, false},
        {false, true,  false,  true}
    };
    
    /*final boolean[][] sequence =
    {
        { true, false, false,  true},
        { true,  true, false, false},
        {false,  true,  true, false},
        {false, false,  true,  true}
    };*/
    
    private final GpioController gpio = GpioFactory.getInstance();
    
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

    public static final int MIN = 0;
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
 