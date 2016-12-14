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
package Jimbo.Boards.com.thepihut;

import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;

import Jimbo.Devices.MAX7219;
import java.io.IOException;

import java.time.LocalTime;
import java.time.LocalDate;


/**
 * Driver class for The Pi Hut's ZeroSeg (designed by Average Man vs
 * Pi).
 * 
 * @author Jim Darby
 */
public class ZeroSeg
{
    public ZeroSeg () throws IllegalArgumentException, IOException
    {
        dev = new MAX7219 (SpiChannel.CS0, 1);
        
        // Set up and blank display
        
        shutdown (false);
        displayTest (false);
        setDecodeMode (MAX7219.DECODE_NONE);
        setIntensity (8);
        setScanLimit (7);
        update ();
        
        final GpioController gpio = GpioFactory.getInstance ();
        
        left = gpio.provisionDigitalInputPin (RaspiPin.GPIO_00, "Left");
        right = gpio.provisionDigitalInputPin (RaspiPin.GPIO_25, "Right");
    }
    
    /**
     * Put a single value into the byte for a specific digit. In this
     * case we renumber them 0 on the left to 7 on the right. 
     * 
     * @param digit The digit to update. 0 to 7.
     * @param value The byte value put in.
     * 
     * @throws IOException In case of error.
     */
    public void writeDigit (int digit, int value) throws IOException
    {
        final byte[] map = {7, 6, 5, 4, 3, 2, 1, 0};
        
        if (digit < 0 || digit >= map.length)
            throw new IllegalArgumentException ("Invalid digit");
        
        if (value < 0 || value > 0xff)
            throw new IllegalArgumentException ("Invalid value");
        
        dev.writeDigit (map[digit], (byte) value);
    }
    
    /**
     * Sets the decode mode.
     * 
     * @param mode The mode. One of DECODE_NONE, DECODE_B_FOR_0,
     * DECODE_B_FOR_0_3 or DECODE_B_FOR_0_7. This either puts no decoding in
     * (for DECODE_NONE) or maps the bottom four bits for digit 0
     * (DECODE_B_FOR_0), digits 1 to 3 (DECODE_B_FOR_0_3) or all the digits
     * (DECODE_B_FOR_0_7). The decode turns the bottom 4 bits into 0 to 9,
     * minus, E, H, L, P and blank (in that order).
     * 
     * Does anyone else think someone was trapped in the factory and put the
     * HELP in there as a “message in a bottle”?
     * 
     * @throws IOException In case of error.
     */
    public void setDecodeMode (byte mode) throws IOException
    {
        dev.setDecodeMode (mode);
    }
    
     /**
     * Set the intensity of the device.
     * 
     * @param intensity The intensity level. It must be in the range 0 to 15.
     * 
     * @throws IOException In case of error.
     */
    public void setIntensity (int intensity) throws IOException
    {
        dev.setIntensity (intensity);
    }
    
    /**
     * Set the scan limit. The scan limit is the number of displays actually
     * used. The value is between 0 and 7 for 1 to 8 displays.
     * 
     * @param limit The limit on the number of displays.
     * 
     * @throws IOException In case of error.
     */
    public void setScanLimit (int limit) throws IOException
    {
        dev.setScanLimit (limit);
    }
    
    /**
     * Set the shutdown status of all devices.
     * 
     * @param active If true the device is shut down.
     * 
     * @throws IOException In case of problems.
     */
    public void shutdown (boolean active) throws IOException
    {
        dev.shutdown (active);
    }
    
    /**
     * Set test mode. This sets all segments of all the displays on.
     * 
     * @param active True to engage, false to turn off.
     * 
     * @throws IOException In case of problems.
     */
    public void displayTest (boolean active) throws IOException
    {
        dev.displayTest (active);
    }
    
    /**
     * Send all the display information to the device. This is done in one
     * go to avoid excessive use of the bus on update of each digit.
     * 
     * @throws IOException In case of problems.
     */
    public void update () throws IOException
    {
        dev.update ();
    }
    
    /**
     * Get the left hand button.
     * 
     * @return The Pin handler.
     */
    public GpioPinDigitalInput getLeftPin ()
    {
        return left;
    }
    
    /**
     * Get the right hand button.
     * 
     * @return The Pin handler.
     */
    public GpioPinDigitalInput getRightPin ()
    {
        return right;
    }
    
    public static void main (String args[]) throws IOException, InterruptedException
    {
        final ZeroSeg z = new ZeroSeg ();
        final GpioPinDigitalInput left = z.getLeftPin ();
        final GpioPinDigitalInput right = z.getRightPin ();
        boolean time = true;
        boolean old_left = true;
        int intensity = 8;
        boolean old_right = true;
        boolean intensity_up = true;
        
        while (true)
        {
            boolean changed = false;
            
            // If left goes from high to low switch mode
            if (left.isHigh ())
            {
                old_left = true;
            }
            else
            {
                // High to low
                if (old_left)
                {
                    time = !time;
                    old_left = false;
                    changed = true;
                }
            }
            
            if (time)
            {
                final LocalTime now = LocalTime.now ();
                
                if (changed)
                    z.setDecodeMode (MAX7219.DECODE_NONE);
                
                z.writeDigit (0, DIGIT_MAP[now.getHour () / 10]);
                z.writeDigit (1, DIGIT_MAP[now.getHour () % 10]);
                z.writeDigit (2, 0);
                z.writeDigit (3, DIGIT_MAP[now.getMinute () / 10]);
                z.writeDigit (4, DIGIT_MAP[now.getMinute () % 10]);
                z.writeDigit (5, 0);
                z.writeDigit (6, DIGIT_MAP[now.getSecond () / 10]);
                z.writeDigit (7, DIGIT_MAP[now.getSecond () % 10]);
                
                z.update ();
            }
            else
            {
                final LocalDate now = LocalDate.now ();
                
                if (changed)
                    z.setDecodeMode (MAX7219.DECODE_B_FOR_0_7);
                
                z.writeDigit (0, now.getYear () / 1000);
                z.writeDigit (1, (now.getYear () / 100) % 10);
                z.writeDigit (2, (now.getYear () / 10) % 10);
                z.writeDigit (3, now.getYear () % 10);
                z.writeDigit (4, now.getMonthValue () / 10);
                z.writeDigit (5, now.getMonthValue () % 10);
                z.writeDigit (6, now.getDayOfMonth () / 10);
                z.writeDigit (7, now.getDayOfMonth () % 10);
                
                z.update ();
            }
            
             // If right goes from high to low update intensity
            if (right.isHigh ())
            {
                old_right = true;
            }
            else
            {
                // High to low
                if (old_right)
                {
                   old_right = false;
                   
                   if (intensity_up)
                   {
                       if (intensity == 15)
                       {
                           intensity = 14;
                           intensity_up = false;
                       }
                       else
                       {
                           intensity += 1;
                       }
                   }
                   else
                   {
                       if (intensity == 0)
                       {
                           intensity = 1;
                           intensity_up = true;
                       }
                       else
                       {
                           intensity -= 1;
                       }
                   }
                   
                   z.setIntensity (intensity);
                }
            }
            
            Thread.sleep (10);
        }
    }
        
    public static final byte DIGIT_MAP[] =
    {
        0x7e, 0x30, 0x6d, 0x79, 0x33,
        0x5b, 0x5f, 0x70, 0x7f, 0x7b
    };
    
    private final MAX7219 dev;
    private final GpioPinDigitalInput left;
    private final GpioPinDigitalInput right;
}
