/*
 * Copyright (C) 2017 Jim Darby.
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
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;

import Jimbo.Devices.Pi2C;
import Jimbo.Devices.HT16K33;
import Jimbo.Devices.BMP280;
import Jimbo.Devices.APA102;
import Jimbo.Graphics.Colour;
import Jimbo.Graphics.ColourMatrix;
import Jimbo.Graphics.ColourMatrixDemo;
import Jimbo.Graphics.FlipX;
import Jimbo.Graphics.Mapping;
import Jimbo.Graphics.Point;

import java.io.IOException;
import java.time.LocalTime;

/**
 * Driver class for the Rainbow HAT.
 * 
 * @author Jim Darby
 */
public class RainbowHAT
{           
    public static void main (String args[]) throws IOException, InterruptedException
    {
        boolean rainbow = true;
        
        for (int i = 0; i < args.length; ++i)
        {  
            if (args[i].equals ("--norainbow"))
                rainbow = false;
            else
                System.out.println ("Option " + args[i] + " ignored.");
        }
        
        final RainbowHAT r = new RainbowHAT ();
        
        r.show ("Hola");
        r.setPoint (3, true);
        r.update ();
        
        Thread.sleep (1000);
        
        final GpioPinDigitalOutput red   = r.getRed ();
        final GpioPinDigitalOutput green = r.getGreen ();
        final GpioPinDigitalOutput blue  = r.getBlue ();
        final GpioPinDigitalInput  a     = r.getA ();
        final GpioPinDigitalInput  b     = r.getB ();
        final GpioPinDigitalInput  c     = r.getC ();
        
        boolean a_down = false;
        boolean b_down = false;
        boolean c_down = false;
        
        int state = 1;
        
        red.  setState (true);
        green.setState (false);
        blue. setState (false);
        
        if (rainbow)
            new Thread (new ColourMatrixDemo (r.getLEDs ())).start ();
        
        while (true)
        {
            final int old_state = state;
            
            if (a.isLow ())
            {
                if (!a_down)
                {
                    System.out.println ("A down");
                    a_down = true;
                    state = 1;
                }
            }
            else
            {
                if (a_down)
                {
                    System.out.println ("A up");
                    a_down = false;
                }
            }
            
            if (b.isLow ())
            {
                if (!b_down)
                {
                    System.out.println ("B down");
                    b_down = true;
                    state = 2;
                }
            }
            else
            {
                if (b_down)
                {
                    System.out.println ("B up");
                    b_down = false;
                }
            }
            
            if (c.isLow ())
            {
                if (!c_down)
                {
                    System.out.println ("C down");
                    c_down = true;
                    state = 3;
                }
            }
            else
            {
                if (c_down)
                {
                    System.out.println ("C up");
                    c_down = false;
                }
            }
            
            if (state != old_state)
            {
                System.out.println ("State is now " + state);
                
                red.  setState (state == 1);
                green.setState (state == 2);
                blue. setState (state == 3);
            }
            
            r.setPoint (0, false);
            r.setPoint (1, false);
            r.setPoint (2, false);
            r.setPoint (3, false);
                    
            switch (state)
            {
                case 1:
                {
                    final LocalTime now = LocalTime.now ();
            
                    r.setDigit (0, now.getHour () / 10);
                    r.setDigit (1, now.getHour () % 10);
                    r.setDigit (2, now.getMinute () / 10);
                    r.setDigit (3, now.getMinute () % 10);

                    r.setPoint (1, now.getNano () > 500000000);
                }
                
                break;
                
                case 2:
                    
                {
                    final double temp = r.getBMP280 ().read ().getTemperature ();
                    
                    if (temp < -99 || temp >= 1000)
                    {
                        r.show ("Err");
                    }
                    else
                    {
                        r.show (String.format ("%4d", (int) (temp * 10)));
                        r.setPoint (2, true);
                    }
                }
                
                break;
                
                case 3:
                    
                {
                    final double pressure = r.getBMP280 ().read ().getPressure () / 100;
                    
                    if (pressure < 0 || pressure >= 10000)
                    {
                        r.show ("Err");
                    }
                    else
                    {
                        r.show (String.format ("%4d", (int) pressure));
                    }
                }
                
                break;
                
                default:
                    System.out.println ("In a strange state: " + state);
                    state = 1;
                    break;
            }
            
            r.update ();
            
            Thread.sleep (20);
        }
    }

    /**
     * Construct a RainbowHAT controller object.
     * 
     * @throws IOException In case of error.
     * @throws InterruptedException In case of error.
     */
    public RainbowHAT () throws IOException, InterruptedException
    {
        final I2CBus bus = Pi2C.useBus ();
        
        display = new HT16K33 (bus, 0x70);
        bmp280  = new BMP280  (bus, 0x77);
        gpio    = GpioFactory.getInstance ();
    }
    
    /**
     * Display a string on the 4-character display.
     * 
     * @param s The string to display.
     * @throws IOException In case of error.
     */
    public void show (String s) throws IOException
    {
        for (int i = 0; i < Math.min(s.length (), 4); ++i)
        {
            final char ch = s.charAt (i);
            
            if (ch >= 0 && ch < CHARS.length)
                display.setWord (i, CHARS[ch]);
            else
                display.setWord (i, (short) 0);
        }

        for (int i = s.length (); i < 4; ++i)
            display.setWord (i, (short) 0);
    }
    
    /**
     * Set the decimal point on the display. 
     * 
     * @param pos The decimal point: 0 to 3.
     * @param on true for on, otherwise false.
     * @throws IOException In case of error.
     */
    public void setPoint (int pos, boolean on) throws IOException
    {
        if (pos < 0 || pos > 3)
            throw new IllegalArgumentException ("RainbowHAT invalid point position");
        
        // Some magic here....
        display.setBit (pos * 16 + 14, on);
    }
    
    /**
     * Set a specific character on the display to a specific numerical value.
     * 
     * @param pos The position: 0 to 3.
     * @param value The value: 0 to 9.
     * @throws IOException In case of error.
     */
    public void setDigit (int pos, int value) throws IOException
    {
        if (pos < 0 || pos > 3)
            throw new IllegalArgumentException ("RainbowHAT invalid point position");
        
        if (value < 0 || value > 9)
            throw new IllegalArgumentException ("RainbowHAT invalid numeric value");
        
        display.setWord (pos, CHARS['0' + value]);
    }
    
    /**
     * Return a digital output for the red LED (above the A button).
     * 
     * @return The output.
     */
    public GpioPinDigitalOutput getRed ()
    {
        if (red == null)
            red = provisionOut (RaspiPin.GPIO_22, "Red LED");
        
        return red;
    }
    
    /**
     * Return a digital output for the green LED (above the B button).
     * 
     * @return The output.
     */
    public GpioPinDigitalOutput getGreen ()
    {
        if (green == null)
            green = provisionOut (RaspiPin.GPIO_24, "Green LED");
        
        return green;
    }
    
    /**
     * Return a digital output for the blue LED (above the C button).
     * 
     * @return The output.
     */
    public GpioPinDigitalOutput getBlue ()
    {
        if (blue == null)
            blue = provisionOut (RaspiPin.GPIO_25, "Blue LED");
        
        return blue;
    }
    
    /**
     * Return a GpioPinDigitalInput for the B button.
     * 
     * @return The input.
     */
    public GpioPinDigitalInput getA ()
    {
        if (a == null)
            a = provisionIn (RaspiPin.GPIO_29, "A");
        
        return a;
    }
    
    /**
     * Return a GpioPinDigitalInput for the B button.
     * 
     * @return The input.
     */
    public GpioPinDigitalInput getB ()
    {
        if (b == null)
            b = provisionIn (RaspiPin.GPIO_28, "B");
        
        return b;
    }
    
    /**
     * Return a GpioPinDigitalInput for the C button.
     * 
     * @return The input.
     */
    public GpioPinDigitalInput getC ()
    {
        if (c == null)
            c = provisionIn (RaspiPin.GPIO_27, "C");
        
        return c;
    }
    
    /**
     * Return the BMP280 device.
     * 
     * @return The BMP280 device.
     */
    public BMP280 getBMP280 ()
    {
        return bmp280;
    }
    
    /**
     * Return the APA102 LEDs.
     * 
     * @return The LEDs
     */
    public LEDs getLEDs ()
    {
        if (leds == null)
            leds = new LEDs (gpio);
        
        return leds;
    }
    
    /**
     * Update the 4-character display.
     * 
     * @throws IOException In case of error.
     */
    public void update () throws IOException
    {
        display.update ();
    }
    
    public static class LEDs implements ColourMatrix
    {
        /**
         * Construct the LEDs. We need a GPIO controller and the other
         * parameters are defined by the board.
         * 
         * @param gpio The GPIO Controller.
         */
        private LEDs (GpioController gpio)
        {
            cs = gpio.provisionDigitalOutputPin (RaspiPin.GPIO_10, "LED Chip Select");
            cs.low ();
            apa102 = new APA102 (gpio, RaspiPin.GPIO_12, RaspiPin.GPIO_14, 7);
            cs.high ();
            
            map = new FlipX (WIDTH, HEIGHT);
        }

        /**
         * Return the maximum X and Y coordinates.
         * 
         * @return The values. 
         */
        @Override
        public Point getMax ()
        {
            return MAX;
        }

        /**
         * Set a specific pixel to a specific value. The height is 1 and the
         * width is 7 meaning the X coordinate goes from 0 to 6 and the Y has
         * to be 0.
         * 
         * @param p The point to change.
         * @param value The value to change it to.
         */
        @Override
        public void setPixel (Point p, Colour value)
        {
            apa102.setPixel (map.map (p), value);
        }

        /**
         * Update the LEDs.
         * 
         * @throws IOException In case of error.
         */
        @Override
        public void show () throws IOException
        {
            cs.low ();
            apa102.show ();
            cs.high ();
        }
        
        /**
         * Scale the brightness to avoid blindness.
         * 
         * @param brightness The brightness scale factor: 0 to 31.
         */
        public void brightness (int brightness)
        {
            apa102.brightness(brightness);
        }
        
        /** The width of the board. */
        public final int WIDTH = 7;
        /** The height of the board. */
        public static final int HEIGHT = 1;
        /** The maximum X value. */
        public final int MAX_X = WIDTH - 1;
        /** The maximum Y value. */
        public static final int MAX_Y = HEIGHT - 1;
        /** The maximum values as a Point. */
        private final Point MAX = new Point (MAX_X, MAX_Y);
        
        /** Point to the underlying APA102 driver. */
        private final APA102 apa102;
        /** Select pin. */
        private final GpioPinDigitalOutput cs;
        /** Point mapping. */
        private final Mapping map;
    }
        
    /**
     * Provision an input pin. This will set the pin as an input and also set
     * pull up on it.
     * 
     * @param p The pin the create.
     * @param name The name of the pin.
     * @return The allocate pin object.
     */
    private GpioPinDigitalInput provisionIn (Pin p, String name)
    {
        return gpio.provisionDigitalInputPin (p, name, PinPullResistance.PULL_UP);
    }
    
    /**
     * Provision an input pin. This will set the pin as an output.
     * 
     * @param p The pin the create.
     * @param name The name of the pin.
     * @return The allocate pin object.
     */
    private GpioPinDigitalOutput provisionOut (Pin p, String name)
    {
        return gpio.provisionDigitalOutputPin (p, name);
    }
    
    /** The object that handles the 4-character display. */
    private final HT16K33 display;
    /** The object that handles the temperature/pressure sensor. */
    private final BMP280 bmp280;
    /** The GPIO Controller we're using. */
    private final GpioController gpio;
    
    /** The red LED. */
    private GpioPinDigitalOutput red   = null;
    /** The green LED. */
    private GpioPinDigitalOutput green = null;
    /** The blue LED. */
    private GpioPinDigitalOutput blue  = null;
    /** The A button input. */
    private GpioPinDigitalInput  a     = null;
    /** The B button input. */
    private GpioPinDigitalInput  b     = null;
    /** The C button input. */
    private GpioPinDigitalInput  c     = null;
    /** The LED handler object. */
    private LEDs                 leds  = null;
    
    /** Characters we can put on the 4-character display. Keep it clean! */
    private static final short[] CHARS =
    {
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 
        0x0000, 0x0006, 0x0220, 0x12ce, 0x12ed, 0x0c24, 0x235d, 0x0400, 
        0x2400, 0x0900, 0x3fc0, 0x12c0, 0x0800, 0x00c0, 0x0000, 0x0c00, 
        0x0c3f, 0x0006, 0x00db, 0x008f, 0x00e6, 0x2069, 0x00fd, 0x0007, 
        0x00ff, 0x00ef, 0x1200, 0x0a00, 0x2400, 0x00c8, 0x0900, 0x1083, 
        0x02bb, 0x00f7, 0x128f, 0x0039, 0x120f, 0x00f9, 0x0071, 0x00bd, 
        0x00f6, 0x1200, 0x001e, 0x2470, 0x0038, 0x0536, 0x2136, 0x003f, 
        0x00f3, 0x203f, 0x20f3, 0x00ed, 0x1201, 0x003e, 0x0c30, 0x2836, 
        0x2d00, 0x1500, 0x0c09, 0x0039, 0x2100, 0x000f, 0x0c03, 0x0008, 
        0x0100, 0x1058, 0x2078, 0x00d8, 0x088e, 0x0858, 0x0071, 0x048e, 
        0x1070, 0x1000, 0x000e, 0x3600, 0x0030, 0x10d4, 0x1050, 0x00dc, 
        0x0170, 0x0486, 0x0050, 0x2088, 0x0078, 0x001c, 0x2004, 0x2814, 
        0x28c0, 0x200c, 0x0848, 0x0949, 0x1200, 0x2489, 0x0520, 0x0000
    };
}
