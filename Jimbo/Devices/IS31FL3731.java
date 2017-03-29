/*
 * Copyright (C) 2017 Jim Darby.
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

package Jimbo.Devices;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;

/**
 * This class controls the IS31FL3731 display controller. It's currently
 * very limited in what it offers but it isn't bad....
 * 
 * @author Jim Darby
 */
public class IS31FL3731
{
    /**
     * This uses the given device address on the given bus.
     * 
     * @param bus The I2C bus to use.
     * @param addr The address on the bus.
     * 
     * @throws IOException In case of trouble.
     * @throws InterruptedException In case of trouble.
     */
    public IS31FL3731 (I2CBus bus, int addr) throws IOException, InterruptedException
    {
        device = bus.getDevice (addr);
        
        for (int i = 0; i < NUM_FRAMES; ++i)
        {
            // Turn blinking off
            for (int j = BLINK_BASE; j < BLINK_END; ++j)
                FRAME[i][j] = 0;
            
            // Turn LEDs on
            for (int j = ENABLE_BASE; j < ENABLE_END; ++j)
                FRAME[i][j] = (byte) 0xff;
            
            // Set the brightness to zero
            for (int j = PWM_BASE; j < PWM_END; ++j)
                FRAME[i][j] = 0;

            // Upload it to the device
            sendFrame (i, FRAME[i]);
            
            // Set dirty markers
            DIRTY_LOW[i]  = FRAME_LEN;
            DIRTY_HIGH[i] = -1;
        }
        
        // Set "sensible" defaults
        FUNCTION_BUFFER[REG_CONFIG]   = 0x00;
        FUNCTION_BUFFER[REG_DISPLAY]  = 0x00;
        FUNCTION_BUFFER[REG_AP1]      = 0x00;
        FUNCTION_BUFFER[REG_AP2]      = 0x00;
        FUNCTION_BUFFER[REG_RESERVED] = 0x00; // Not used
        FUNCTION_BUFFER[REG_OPTION]   = 0x00;
        FUNCTION_BUFFER[REG_AUDIO]    = 0x00;
        FUNCTION_BUFFER[REG_FRAME]    = 0x00; // Read only
        FUNCTION_BUFFER[REG_BREATH1]  = 0x00;
        FUNCTION_BUFFER[REG_BREATH2]  = 0x00;
        FUNCTION_BUFFER[REG_SHUTDOWN] = 0x01; // Activate (only non-default!)
        FUNCTION_BUFFER[REG_AGC_CTL]  = 0x00;
        FUNCTION_BUFFER[REG_AGC_RATE] = 0x00;
        
        sendFunction ();
    }

    /**
     * Set the PWM value of a specific LED in a specific frame.
     * 
     * @param frame The frame the LED is in.
     * @param led The LED number in that frame.
     * @param pwm The PWM value.
     */
    public void setLed (int frame, int led, int pwm)
    {
        if (frame < 0 || frame >= NUM_FRAMES)
            throw new IllegalArgumentException ("Invalid frame " + frame);
        
        if (led < 0 || led >= NUM_LEDS)
            throw new IllegalArgumentException ("Invalid LED " + led);
        
        if (pwm < 0 || pwm > MAX_PWM)
            throw new IllegalArgumentException ("Invalid PWM " + pwm);
        
        final int index = PWM_BASE + led;
        
        FRAME[frame][index] = (byte) pwm;
        
        // Update the dirty values
        
        if (index > DIRTY_HIGH[frame])
            DIRTY_HIGH[frame] = index;
        
        if (index < DIRTY_LOW[frame])
            DIRTY_LOW[frame] = index;
    }
    
    /**
     * Update the display.
     * 
     * @throws IOException In case of trouble.
     */
    public void update () throws IOException
    {
        for (int i = 0; i < NUM_FRAMES; ++i)
        {
            if (DIRTY_HIGH[i] >= DIRTY_LOW[i])
            {
                setPage (i);
                
                device.write(DIRTY_LOW[i], FRAME[i], DIRTY_LOW[i], DIRTY_HIGH[i] - DIRTY_LOW[i] + 1);
                
                DIRTY_LOW[i]  = FRAME_LEN;
                DIRTY_HIGH[i] = -1;
            }
        }
    }
    
    /**
     * Send a specific frame to the device.
     * 
     * @param frame The frame to send.
     * @param data The data to send.
     * 
     * @throws IOException In case of trouble.
     */
    private void sendFrame (int frame, byte[] data) throws IOException
    {
        if (frame < 0 || frame >= NUM_FRAMES)
            throw new IllegalArgumentException ("Bad frame number " + frame);
        
        setPage (frame);
        device.write (0, data);
    }
    
    /**
     * Send the function data to the device.
     * 
     * @throws IOException In case of trouble.
     */
    private void sendFunction () throws IOException
    {
        setPage (FUNCTION_PAGE);
        device.write (0, FUNCTION_BUFFER);
    }
    
    /**
     * Set the page we're currently writing to.
     * 
     * @param to The page we want to use.
     * 
     * @throws IOException In case of trouble.
     */
    private void setPage (int to) throws IOException
    {
        if ((to < 0 || to > 7) && to != FUNCTION_PAGE)
            throw new IllegalArgumentException ("Bad page number " + to);
        
        if (to != current_page)
        {
            device.write (REG_CMD, (byte) to);
            current_page = to;
        }
    }
    
    public static void main (String args[]) throws IOException, InterruptedException
    {
        IS31FL3731 d = new IS31FL3731 (Pi2C.useBus (), 0x74);        
                
        for (int i = 0; i < 144; ++i)
        {
            d.setLed (0, i, 0xff);
            d.update ();
            Thread.sleep (100);
            d.setLed (0, i, 0x00);
            d.update ();
            Thread.sleep (100);
        }
    }
        
    /** Number of LEDs per frame. */
    public final static int NUM_LEDS = 144;
    /** Number of frames. */
    public final static int NUM_FRAMES = 8;
    /** Maximum PWM value. */
    public final static int MAX_PWM = 0xff;
    
    /** Point to the device we're using. */
    private final I2CDevice device;
    /** Current page we're on. */
    private int current_page = -1;
    /** Two byte buffer. */
    //private final byte BUFFER2[] = new byte[2];
    
    /** Frame output buffer. */
    //private final byte FRAME_BUFFER[] = new byte[1 + FRAME_LEN]; 
    /** Function buffer. */
    private final byte FUNCTION_BUFFER[] = new byte[FUNCTION_LEN];
    /** Frame storage. */
    private final byte FRAME[][] = new byte[NUM_FRAMES][FRAME_LEN];
    /** Dirty low marker. */
    private final int DIRTY_LOW[] = new int[NUM_FRAMES];
    /** Dirty high marker. */
    private final int DIRTY_HIGH[] = new int[NUM_FRAMES];
            
    /** The command register. */
    private final static byte REG_CMD     = (byte) 0xfd;
    /** The function register page. */
    private final static int FUNCTION_PAGE = 0x0b;
    
    /** The configuration register. */
    private final static int REG_CONFIG   = 0x00;
    /** The display register. */
    private final static int REG_DISPLAY  = 0x01;
    private final static int REG_AP1      = 0x02;
    private final static int REG_AP2      = 0x03;
    /** The reserved register. */
    private final static int REG_RESERVED = 0x04;
    /** The option register. */
    private final static int REG_OPTION   = 0x05;
    /** The audio register. */
    private final static int REG_AUDIO    = 0x06;
    /** The Frame register. */
    private final static int REG_FRAME    = 0x07;
    /** The breath register (part 1). */
    private final static int REG_BREATH1  = 0x08;
    /** The breath register (part 2). */
    private final static int REG_BREATH2  = 0x09;
    /** The shutdown register. */
    private final static int REG_SHUTDOWN = 0x0a;
    /** The AGC control register. */
    private final static int REG_AGC_CTL  = 0x0b;
    /** The AGC rate register. */
    private final static int REG_AGC_RATE = 0x0c;

    /** Base of enable bits. */
    private final static int ENABLE_BASE = 0x00;
    /** End of enable bits (+ 1!). */
    private final static int ENABLE_END = ENABLE_BASE + NUM_LEDS / 8;
    /** Base of blink bits. */
    private final static int BLINK_BASE = ENABLE_END;
    /** End of blink bits (+ 1!). */
    private final static int BLINK_END = BLINK_BASE + NUM_LEDS / 8;
    /** Base of PWM data. */
    private final static int PWM_BASE = BLINK_END;
    /** End of PWM data (+ 1!). */
    private final static int PWM_END = PWM_BASE + NUM_LEDS;
    
    /** Length of frame data. */
    private final static int FRAME_LEN = PWM_END;
    /** Length of function data. */
    private final static int FUNCTION_LEN = 0x0d;
}
