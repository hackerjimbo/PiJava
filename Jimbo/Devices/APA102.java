/*
 * Copyright (C) 2016-2017 Jim Darby.
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
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

import Jimbo.Graphics.Colour;
import Jimbo.Graphics.ColourMatrix;
import Jimbo.Graphics.ColourMatrixDemo;
import Jimbo.Graphics.Point;

import java.io.IOException;

/**
 * This class drives a chain of APA102 "intelligent" LEDs.
 *
 * @author Jim Darby
 */
public class APA102 implements ColourMatrix
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
        WIDTH = n;
        MAX_X = WIDTH - 1;
        MAX = new Point (MAX_X, MAX_Y);
        dat = gpio.provisionDigitalOutputPin (data_pin);
        clk = gpio.provisionDigitalOutputPin (clock_pin);
        data = new int[n];
        
        // Set all off to start with.
        for (int i = 0; i < WIDTH; ++i)
            data[i] = 0;
        
        // And push that out to the devices.
        show ();
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
        if (n < 0 || n >= WIDTH ||
            r < 0 || r > 255 ||
            g < 0 || g > 255 ||
            b < 0 || b > 255 ||
            bright < 0 || bright > 31)
            throw new IllegalArgumentException ("Invalid parameter");
        
        data[n] = (bright << 24) | (r << 16) | (g << 8) | b;
    }
    
    /**
     * Return a point with the maximum values for X and Y in this
     * matrix.
     * 
     * @return A Point object with the maximum values for X and Y.
     */
    @Override
    public Point getMax()
    {
        return MAX;
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
        
        set (p.getX (), value.getRed (), value.getGreen (), value.getBlue (), brightness);
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
        for (int i = 0; i < WIDTH; ++i)
            write_led (data[i]);
        
        // And latch it
        latch ();
    }
    
    /**
     * Scale the brightness to avoid blindness.
     * 
     * @param brightness The brightness scale factor: 0 to 31.
     */
    
    public final void brightness (int brightness)
    {
        if (brightness < 0 || brightness > 31)
            throw new IllegalArgumentException ("Invalid brightness");
        
        this.brightness = brightness;
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
    
    public static void main (String args[]) throws InterruptedException, IOException
    {
        APA102 blinkt = new APA102 (GpioFactory.getInstance(), RaspiPin.GPIO_04, RaspiPin.GPIO_05, 8);
        
        for (int x = 0; x < 8; ++x)
        {
            blinkt.set (x, 255, 0, 0, 31);
            blinkt.show ();
            Thread.sleep (500);
            blinkt.set (x, 0, 0, 0, 31);
            blinkt.show ();
        }
        
        ColourMatrixDemo.run (blinkt);
    }
    
    /** The pin we use for data. */
    private final GpioPinDigitalOutput dat;
    /** The pin we use for the clock. */
    private final GpioPinDigitalOutput clk;
    /** The data for each LED in the chain. */
    private final int[] data;
    /** Scale factor for brightness. Defaults to 8 (of 31) because Pimoroni. */
    private int brightness = 8;
    
    /** The width of the board. */
    public final int WIDTH;
    /** The height of the board. */
    public static final int HEIGHT = 1;
    /** The maximum X value. */
    public final int MAX_X;
    /** The maximum Y value. */
    public static final int MAX_Y = HEIGHT - 1;
    
    /** The maximum values as a Point. */
    private final Point MAX;
}
