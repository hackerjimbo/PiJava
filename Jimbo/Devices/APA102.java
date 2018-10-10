/*
 * Copyright (C) 2016-2018 Jim Darby.
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

package Jimbo.Devices;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;

import Jimbo.Graphics.Colour;
import Jimbo.Graphics.ColourMatrix;
import Jimbo.Graphics.MatrixHelper;

import java.util.Arrays;

/**
 * This class drives a chain of APA102 "intelligent" LEDs.
 *
 * @author Jim Darby
 */
public class APA102 extends MatrixHelper <Colour> implements ColourMatrix
{
    /**
     * Construct an APA102 controller.
     * 
     * @param gpio The GpioController to use.
     * @param data_pin The data pin to use
     * @param clock_pin The clock pin to use.
     * @param n The number of LEDs in the chain. 
     */
    public APA102 (GpioController gpio, Pin data_pin, Pin clock_pin, int n)
    {
        super (n, 1);
        
        dat = gpio.provisionDigitalOutputPin (data_pin);
        clk = gpio.provisionDigitalOutputPin (clock_pin);
        data = new int[n];
        
        // Set all off to start with. Java actually defines that the array is
        // initialised with zeros but this is here just to emphasise that.
        Arrays.fill (data, 0);
        
        // And push that out to the devices.
        show ();
    }
    
    /**
     * Set a LED to a specific red, green and blue value. We also set the
     * brightness. This is the fastest and most complete way.
     * 
     * @param n The LED number, in the range 0 to the number of LEDs minus one.
     * @param r The red value (0 to 255).
     * @param g The green value (0 to 255).
     * @param b The blue value (0 to 255).
     * @param bright The brightness (0 to 31).
     */
    public void setPixel (int n, int r, int g, int b, byte bright)
    {
        if (n < 0 || n >= data.length ||
            r < 0 || r > 255 ||
            g < 0 || g > 255 ||
            b < 0 || b > 255 ||
            bright < 0 || bright > MAX_BRIGHT)
            throw new IllegalArgumentException ("Invalid parameter");
        
        data[n] = (bright << 24) | (r << 16) | (g << 8) | b;
    }
    
    /**
     * Set a LED to a specific red, green and blue value. The brightness comes
     * from the default we've set earlier.
     * 
     * @param n The LED number, in the range 0 to the number of LEDs minus one.
     * @param r The red value (0 to 255).
     * @param g The green value (0 to 255).
     * @param b The blue value (0 to 255).
     */
    public void setPixel (int n, int r, int g, int b)
    {
        setPixel (n, r, g, b, brightness);
    }
    
    /**
     * Set a LED to a specific red, green and blue value. The brightness comes
     * from the default we've set earlier.
     * 
     * @param x The x coordinate. 
     * @param y The y coordinate. Must be zero.
     * @param r The red value (0 to 255).
     * @param g The green value (0 to 255).
     * @param b The blue value (0 to 255).
     */
    @Override
    public void setPixel (int x, int y, int r, int g, int b)
    {
        if (y != 0)
            throw new IllegalArgumentException ();
        
        setPixel (x, r, g, b);
    }
    
    /**
     * Update the LED chain.
     */
    @Override
    public final void show ()
    {
        // Transmit preamble
        for (int i = 0; i < 4; ++i)
            write_byte ((byte) 0);
        
        // Send data
        for (int i = 0; i < data.length; ++i)
            write_led (data[i]);
        
        // And latch it
        latch ();
    }
    
    /**
     * Scale the brightness to avoid blindness.
     * 
     * @param brightness The brightness scale factor: 0 to 31.
     */
    public void brightness (byte brightness)
    {
        if (brightness < 0 || brightness > MAX_BRIGHT)
            throw new IllegalArgumentException ("Invalid brightness");
        
        this.brightness = brightness;
    }
    
    /**
     * Scale the brightness to avoid blindness. This version uses a double in
     * the range 0 to 1 (inclusive).
     * 
     * @param b The brightness scale factor: 0.0 to 1.0.
     */
    public void brightness (double b)
    {
        if (b < 0 || b > 1)
            throw new IllegalArgumentException ("Invalid brightness");
        
        brightness ((byte) (b * MAX_BRIGHT + 0.5));
    }
    
    /**
     * Write out a single byte. It goes out MSB first.
     * 
     * @param out The byte to write.
     */
    private void write_byte (byte out)
    {
        for (int i = 7; i >= 0; --i)
        {
            dat.setState ((out & (1 << i)) != 0);
            clk.setState (true);
            clk.setState (false);
        }
    }
    
    /**
     * Write out a single LEDs information.
     * 
     * @param data The data for that LED.
     */
    private void write_led (int data)
    {
        write_byte ((byte) (0xe0 | ((data >> 24) & 0x1f)));

        write_byte ((byte) (data));
        write_byte ((byte) (data >> 8));
        write_byte ((byte) (data >> 16));
    }
    
    /**
     * Latch the data into the devices. This has prompted much discussion as
     * data sheet seems to be a work of fiction. These values seem to work.
     * 
     * In case of any difficulties, blame Gadgetoid: it's all his fault!
     */
    private void latch ()
    {
        // Transmit zeros not ones!
        dat.setState (false);
        
        // And 36 of them!
        for (int i = 0; i < 36; ++i)
        {
            clk.setState (true);
            clk.setState (false);
        }
    }
    
    /** The pin we use for data. */
    private final GpioPinDigitalOutput dat;
    /** The pin we use for the clock. */
    private final GpioPinDigitalOutput clk;
    /** The data for each LED in the chain. */
    private final int[] data;
    /** Scale factor for brightness. Defaults to quarter power because Pimoroni. */
    private byte brightness = MAX_BRIGHT / 4;
    
    /** The maximum brightness possible. */
    public static final byte MAX_BRIGHT = 31;
}
