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

package Jimbo.Devices;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * This class drive the IS31FL3730 display controller. It's primary goal is to
 * be generic so that its users can provide the appropriate parameters to make
 * it work with the highly board specific implementation.
 * 
 * @author Jim Darby
 */
public class IS31FL3730
{
    private static final Logger LOG = Logger.getLogger ("Jimbo.Devices.IS31FL3730");
    
    /**
     * Constructor. This uses the default device address on the given bus.
     * 
     * @param bus The bus the controller is on.
     * @throws IOException When it can't create the bus device.
     */
    public IS31FL3730 (I2CBus bus) throws IOException
    {
        this (bus, I2C_ADDR);
    }
    
    /**
     * Constructor. The uses a specified address on the given bus,
     * 
     * @param bus The bus the controller is on.
     * @param addr The address on the bus
     * @throws IOException When it can't create the bus device.
     */
    public IS31FL3730 (I2CBus bus, int addr) throws IOException
    {
        device = bus.getDevice (addr);
        
        // Clear retry timers
        for (int i = 0; i < MAX_TRIES; ++i)
            tries [i] = 0;
    }
    
    /**
     * Set the mode of the device. You really want to read the manual for this.
     * 
     * @param mode You can have (or not) MODE_SSD, MODE_AE and one of:
     * MODE_8X8, MODE_7X9, MODE_6X10 or MODE_9X11 and one of:
     * MODE_M1, MODE_M2 or MODE_M12.
     * @throws IOException If anything goes wrong.
     */
    public void setMode (int mode) throws IOException
    {
        // Did they set any naughty bits?
        if ((mode & ~(0x9f)) != 0)
            throw new IllegalArgumentException ("Bad mode value " + mode);
        
        retryWrite (REG_CONFIG, (byte) mode);
    }
    
    /**
     * Set a single byte in M1.
     * 
     * @param offset The offset (0 to 10).
     * @param value The value
     * @throws java.io.IOException On invalid parameters or error.
     */
    public void setM1 (byte offset, byte value) throws IOException
    {
        if (offset < 0 || offset > 10)
            throw new IllegalArgumentException ("Invalid offset " + offset);
        
        retryWrite (REG_M1_BASE + offset, value);
    }

    /**
     * Set a single byte in M2.
     * 
     * @param offset The offset (0 to 10).
     * @param value The value
     * @throws java.io.IOException On invalid parameters or error.
     */
    public void setM2 (byte offset, byte value) throws IOException
    {
        if (offset < 0 || offset > 10)
            throw new IllegalArgumentException ("Invalid offset " + offset);
        
        retryWrite (REG_M2_BASE + offset, value);
    }
    
    /**
     * Write the M1 matrix data and then (maybe) hit the update register.
     * This needs between 1 to 12 bytes of data but the 12th (if there)
     * isn't used for anything useful except triggering the device to update.
     * 
     * @param data A 1- to 12-item byte array.
     * @throws IOException In case of problems.
     */
    public void fastUpdateM1 (byte data[]) throws IOException
    {
        if (data.length < 1 || data.length > 12)
            throw new IllegalArgumentException ("Invalid fastUpdate data");
        
        retryWrite (REG_M1_BASE, data, 0, data.length);
    }
    
    /**
     * Write the entire M2 matrix data.
     * 
     * @param data A 1- to 11-item byte array.
     * @throws IOException In case of problems.
     */
    public void fastUpdateM2 (byte data[]) throws IOException
    {
        if (data.length < 1 || data.length > 11)
            throw new IllegalArgumentException ("Invalid fastUpdate data");
        
        retryWrite (REG_M2_BASE, data, 0, data.length);
    }

    /**
     * Set the lighting effect register. See the datasheet for the full details.
     * In summary, if you're using the audio input you set the gain here using
     * one of the LER_*DB values for the appropriate decibel gain. You should
     * always use one of the LER_*MA values to set the driver current for the 
     * LEDs in the matrix.
     * 
     * @param effect The value to use. Must be between 0 and 127 inclusive.
     * @throws IOException In case of error.
     */
    public void setLightingEffect (int effect) throws IOException
    {
        if (effect < 0 || effect > 127)
            throw new IllegalArgumentException ("Invalid effect " + effect);
        
        retryWrite (REG_LER, (byte) effect);
    }
    
    /**
     * Set the PWM parameter.
     * 
     * @param pwm The PWM value. Must be between 0 and 128 inclusive.
     * @throws IOException In case of error.
     */
    public void setPWM (int pwm) throws IOException
    {
        if (pwm < 0 || pwm > MAX_PWM)
            throw new IllegalArgumentException ("Bad PWM " + pwm);
        
        retryWrite (REG_PWM, (byte) pwm);
    }
    
    /**
     * Tell the device to update its display.
     * 
     * @throws IOException In case things go wrong.
     */
    public void update () throws IOException
    {
        retryWrite (REG_UCR, (byte) 0x00);
    }
    
    /**
     * Try a bus write until it works.
     * 
     * @param reg The register to write.
     * @param value The value to write
     * @throws IOException If it really can't be made to work!
     */
    private void retryWrite (int reg, byte value) throws IOException
    {
        buffer1[0] = value;
        
        retryWrite (reg, buffer1, 0, 1);
    }
    
    /**
     * Try a bus write until it works.
     * 
     * @param reg The register to write.
     * @param value The values to write
     * @throws IOException If it really can't be made to work!
     */
    private void retryWrite (int reg, byte[] value, int base, int length) throws IOException
    {
        IOException error = null;
               
        for (int i = 0; i < MAX_TRIES; ++i)
        {
            try
            {
                device.write (reg, value, base, length);
                ++tries[i];
                
                if (i > 0)
                    printTries ();
                
                error = null;
                break;
            }
            
            catch (IOException e)
            {
                error = e;
            }
            
            try
            {
                Thread.sleep (1);
            }
            
            catch (InterruptedException e)
            {
                LOG.log (Level.WARNING, "Sleep interrupted: {0}", e.getLocalizedMessage ());
            }
        }
        
        if (error != null)
            throw error;
    }

    /**
     * Set the number of tries before logging a warning. Note this will
     * never log if there isn't a retry.
     * 
     * @param n The number of tries, >= 0.
     */
    public void setTriesWarning (int n)
    {
        if (n < 0)
            throw new IllegalArgumentException ("Bad number of tries " + n);
        
        warn_tries = n;
    }
    
    /**
     * Print out diagnostic information in case of failure.
     */
    private void printTries ()
    {
        int total = 0;
        int failed = 0;
        boolean summarise = false;
        
        for (int i = 0; i < MAX_TRIES; ++i)
        {
            total += tries[i];
        
            if (i > 0 && tries[i] != 0)
            {
                failed += tries[i] * i;
                
                if (i > warn_tries)
                {
                    LOG.log (Level.INFO, "Retries {0} = {1}", new Object[]{i, tries[i]});
                    summarise = true;
                }
            }
        }
        
        if (summarise)
            LOG.log (Level.INFO, "As as percentage: {0}", (((double)  failed / total)) * 100);
    }
    
    // Dealing with communication and communication problems
    
    /** Buffer for single byte writes. */
    private final byte[] buffer1 = new byte[1];
    /** Warn if this or more tried needed to communicate with device. */
    private int warn_tries = 4;
    /** The maximum number of tries to send something over the bus. */
    private static final int MAX_TRIES = 20;
    /** Create a histogram of the number of tries;. */
    private final int tries[] = new int[MAX_TRIES];
    
    /** The default I2C address of the device. */
    private static final byte I2C_ADDR = 0x60;
    
    /** Configuration register. */
    private static final byte REG_CONFIG = 0x00;
    /** Base of Matrix 1 data. */
    private static final byte REG_M1_BASE = 0x01;
    /** Base of Matrix 2 data. */
    private static final byte REG_M2_BASE = 0x0e;
    /** Update control register. */
    private static final byte REG_UCR = 0x0c;
    /** Lighting effects register. */
    private static final byte REG_LER = 0x0d;
    /** PWM register. */
    private static final byte REG_PWM = 0x19;
    /** Reset register */
    private static final byte REG_RESET = (byte) 0xff;
    
    /** Software shutdown mode */
    public static final byte MODE_SSD = (byte) 0x80;
    /** Set matrix 1 only */
    public static final byte MODE_M1 = 0x00;
    /** Set matrix 2 only */
    public static final byte MODE_M2 = 0x08;
    /** Set matrix 1 and 2 */
    public static final byte MODE_M12 = 0x18;
    /** Audio enable */
    public static final byte MODE_AE = 0x04;
    /** Set 8x8 */
    public static final byte MODE_8X8 = 0x00;
    /** Set 7*9 */
    public static final byte MODE_7X9 = 0x01;
    /** Set 6*10 */
    public static final byte MODE_6X10 = 0x02;
    /** Set 5*11 */
    public static final byte MODE_5X11 = 0x03;
    
    /** Set drive to 5mA. */
    public static final byte LER_05MA = 0x08;
    /** Set drive to 10mA. */
    public static final byte LER_10MA = 0x09;
    /** Set drive to 15mA. */
    public static final byte LER_15MA = 0x0a;
    /** Set drive to 20mA. */
    public static final byte LER_20MA = 0x0b;
    /** Set drive to 25mA. */
    public static final byte LER_25MA = 0x0c;
    /** Set drive to 30mA. */
    public static final byte LER_30MA = 0x0d;
    /** Set drive to 35mA. */
    public static final byte LER_35MA = 0x0e;
    /** Set drive to 40mA. */
    public static final byte LER_40MA = 0x00;
    /** Set drive to 45mA. */
    public static final byte LER_45MA = 0x01;
    /** Set drive to 50mA. */
    public static final byte LER_50MA = 0x02;
    /** Set drive to 55mA. */
    public static final byte LER_55MA = 0x03;
    /** Set drive to 60mA. */
    public static final byte LER_60MA = 0x04;
    /** Set drive to 65mA. */
    public static final byte LER_65MA = 0x05;
    /** Set drive to 70mA. */
    public static final byte LER_70MA = 0x06;
    /** Set drive to 75mA. */
    public static final byte LER_75MA = 0x07;
    
    /** Set audio gain to 0db. */
    public static final byte LER_0DB = 0x00;
    /** Set audio gain to +3db. */
    public static final byte LER_3DB = 0x10;
    /** Set audio gain to +6db. */
    public static final byte LER_6DB = 0x20;
    /** Set audio gain to +9db. */
    public static final byte LER_9DB = 0x30;
    /** Set audio gain to +12db. */
    public static final byte LER_12DB = 0x40;
    /** Set audio gain to +15db. */
    public static final byte LER_15DB = 0x50;
    /** Set audio gain to +18db. */
    public static final byte LER_18DB = 0x60;
    /** Set audio gain to -6db. */
    public static final byte LER_M6DB = 0x70;
    
    /** The maximum value the PWM can be. */
    public static final int MAX_PWM = 128;
    
    /** Point to the device we're using. */
    private final I2CDevice device;
}
