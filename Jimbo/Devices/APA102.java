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

package Jimbo.Devices;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.RaspiPin;

import java.io.IOException;

/**
 * This class drives a chain of APA102 "intelligent" LEDs.
 *
 * @author Jim Darby
 */
public class APA102
{
    /**
     * Construct an APA10-2 controller.
     * 
     * @param n The number of LEDs in the chain. 
     */
    public APA102 (int n)
    {
        leds = n;
        data = new int[n];
        
        // Set all off to start with.
        for (int i = 0; i < leds; ++i)
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
     * @throws java.io.IOException In the case of an invalid parameter.
     */
    public void set (int n, int r, int g, int b, int bright) throws IOException
    {
        if (n < 0 || n >= leds ||
            r < 0 || r > 255 ||
            g < 0 || g > 255 ||
            b < 0 || b > 255 ||
            bright < 0 || bright > 31)
            throw new IOException ("Invalid paramter");
        
        data[n] = (bright << 24) | (r << 16) | (g << 8) | b;
    }
    
    /**
     * Update the LED chain.
     */
    public void show ()
    {
        // Transmit preamble
        for (int i = 0; i < 4; ++i)
            write_byte ((byte) 0);
        
        // Send data
        for (int i = 0; i < leds; ++i)
            write_led (data[i]);
        
        // And latch it
        latch ();
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
        APA102 blinkt = new APA102 (8);
        
        while (true)
        for (int b = 0; b < 32; ++b)
        {
            blinkt.set ((b + 0) % 8, 255, 0, 0, b);
            blinkt.set ((b + 1) % 8, 0, 255, 0, b);
            blinkt.set ((b + 2) % 8, 0, 0, 255, b);
            blinkt.set ((b + 3) % 8, 255, 255, 0, b);
            blinkt.set ((b + 4) % 8, 255, 0, 255, b);
            blinkt.set ((b + 5) % 8, 0, 255, 255, b);
            blinkt.set ((b + 6) % 8, 255, 255, 255, b);

            blinkt.show ();
            
            Thread.sleep (100);
        }
    }
    
    final GpioController gpio = GpioFactory.getInstance();
    final GpioPinDigitalOutput dat = gpio.provisionDigitalOutputPin (RaspiPin.GPIO_04);
    final GpioPinDigitalOutput clk = gpio.provisionDigitalOutputPin (RaspiPin.GPIO_05);
    final int leds;
    final int[] data;
}
