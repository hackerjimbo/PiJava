/*
 * Copyright (C) 2016, 2019 Jim Darby.
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
package Jimbo.Devices;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;

/**
 * The class interfaces to the Si-EN Technology SN3214 LED driver chip. This
 * device is used in many cool devices, not least in things from Pimoroni for
 * the Raspberry Pi.
 * 
 * @author Jim Darby
 */
public class SN3218
{
    /**
     * Construct a SN3218 driver. It figures out the bus from the device.
     * 
     * @throws IOException In case of problems.
     * @throws java.lang.InterruptedException On bus detection failure.
     */
    
    public SN3218 () throws IOException, InterruptedException
    {
        this (Pi2C.useBus ());
    }
    
    /**
     * Construct a SN3218 driver.
     * 
     * @param bus The bus the device is on.
     * 
     * @throws IOException In case of problems.
     */
    public SN3218 (I2CBus bus) throws IOException
    {
        // Set the data up. All enabled, all off
        
        for (int i = 0; i < LEDS; ++i)
            data[i] = 0;
        
        for (int i = 0; i < ENABLES; ++i)
            data[LEDS + i] = 0x3f;
        
        // Tell the device to go. Any value will do but this is in memory of
        // Douglas Adams.
        data[LEDS + ENABLES] = 42;
        
        // Allocate device
        device = bus.getDevice (0x54);
     
        // And set everything up
        device.write (0, WAKEUP, 0, WAKEUP.length);
    }
    
    /**
     * Set a single output to a value.
     * 
     * @param led The LED (in the range 0 to 17).
     * @param value The value (in the range 0 to 255).
     * 
     * @throws IllegalArgumentException On an invalid parameter.
     */
    public void set (int led, int value)
    {
        if (led < 0 || led >= LEDS)
            throw new IllegalArgumentException ("Invalid LED " + led);
        
        if (value < 0 || value > 255)
            throw new IllegalArgumentException ("Invalid level " + value);
        
        data[led] = (byte) value;
    }
    
    /**
     * Set an RGB LED group with three values. The way the device is wired up
     * determines the specific groupings.
     * 
     * @param led The LED in the range 0 - 5.
     * @param v1 The first value.
     * @param v2 The second value.
     * @param v3 The third value.
     * 
     * @throws IllegalArgumentException On an invalid parameter.
     */
    public void set (int led, int v1, int v2, int v3)
    {
        if (led < 0 || led >= LEDS / 3)
            throw new IllegalArgumentException ("Invalid RGB LED " + led);
        
        if (v1 < 0 || v1 > 255 || v2 < 0 || v2 > 255 || v3 < 0 || v3 > 255)
            throw new IllegalArgumentException ("Invalid colour value");
        
        data[led * 3    ] = (byte) v1;
        data[led * 3 + 1] = (byte) v2;
        data[led * 3 + 2] = (byte) v3;
    }
    
    /**
     * Update the display. This sends all the updated value to the LEDs.
     * 
     * @throws IOException In case of an I2C error.
     */
    public void update () throws IOException
    {
        device.write(1, data, 0, data.length);
    }
   
    /** The I2C device. */
    private final I2CDevice device;
    /** The data we hold for the device. Starts at offset ONE in the device! */
    private final byte[] data = new byte[DATA_SIZE];
    
    /** The number of LEDs. */
    private final static int LEDS = 18;
    /** The number of enable bytes. */
    private final static int ENABLES = 3;
    /** The number of go bytes. */
    private final static int GOS = 1;
    /** The size of the data we hold: 18 values, 3 enables 1 go. */
    private final static int DATA_SIZE = LEDS + ENABLES + GOS;
    /** Wakeup data. */
    private final static byte[] WAKEUP = {
        0x01,                               // Wake up the device
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // Data part one
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // Data part two
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // Data part three
        0x3f, 0x3f, 0x3f,                   // Enable all outputs
        0x42                                // And go!
    };
}
