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
import com.pi4j.io.i2c.I2CFactory;

/**
 * This class controls a HT16K33 display controller.
 * 
 * @author Jim Darby
 */
public class HT16K33
{    
    public HT16K33 (I2CBus bus, int dev) throws IOException
    {
        // Get a device object to use for communication.
        device = bus.getDevice (dev);
        
        setOscillator (true);
        setDisplay (true, DISPLAY_STEADY);
        setBrightness (15);
        
        for (int i = 0; i < buffer.length; ++i)
            buffer[i] = 0x00;
        
        update ();
    }
    
    /**
     * Turn the main oscillator on or off. Having the oscillator off is
     * standby mode and on is the normal run mode.
     * 
     * @param on Turn the oscillator on or not.
     * 
     * @throws IOException In case of trouble.
     */
    public void setOscillator (boolean on) throws IOException
    {
        device.write ((byte) (SETUP_REG | (on ? SETUP_ON : 0)));
    }
    
    /**
     * Set the display parameters. There is an on/off boolean and the blink
     * rate which can be one of DISPLAY_STEADY (where it's on permanently),
     * DISPLAY_TWOHZ (flashing at 2Hz), DISPLAY_ONEHZ (flashing at 1Hz) or
     * DISPLAY_HALFHZ (flashing at 0.5Hz).
     * 
     * @param on Display on/off.
     * @param blink DISPLAY_STEADY, DISPLAY_TWOHZ, DISPLAY_ONEHZ or
     * DISPLAY_HALFHZ.
     * 
     * @throws IOException In case of trouble.
     */
    public void setDisplay (boolean on, int blink) throws IOException
    {
        if (blink < 0 || blink > 3)
            throw new IOException ("HT16K33 invalid blink value");
        
        device.write ((byte) (DISPLAY_REG | (blink << 1) | (on ? DISPLAY_ON : 0)));
    }
    
    /**
     * Set the display's brightness level.
     * 
     * @param level The brightness level: 0 to 15 mapping to 1/16 to 16/16 max.
     * 
     * @throws IOException In case of trouble.
     */
    public void setBrightness (int level) throws IOException
    {
        if (level < 0 || level > 15)
            throw new IOException ("HT16K33 invalid brightness value");
        
        device.write ((byte) (BRIGHTNESS_REG | level));
    }
    
    /**
     * Set a single bit in the display on or off. Bits are number from byte
     * zero upwards and inside a byte from LSB to MSB.
     * 
     * @param bit The bit to set (in the range 0 to 127).
     * @param on Set the bit on or off.
     * 
     * @throws IOException In case of trouble.
     */
    public void setBit (int bit, boolean on) throws IOException
    {
        if (bit < 0 || bit >= buffer.length * 8 - 1)
            throw new IOException ("HT16K33 invalid bit number");
        
        final int index = bit / 8;
        final byte value = (byte) (1 << (bit % 8));
        
        if (on)
            buffer[index] |= value;
        else
            buffer[index] &= ~value;
    }
    
    /**
     * Set a specific byte in the buffer.
     * 
     * @param which Which byte to set (0 to 15).
     * @param value The value to set it to.
     * 
     * @throws java.io.IOException In case of problems.
     */
    public void setByte (int which, byte value) throws IOException
    {
        if (which < 0 || which >= buffer.length)
            throw new IOException ("HT16K33 invalid byte number");
        
        buffer[which] = value;
    }
    /**
     * Set a specific word in the buffer. The words are stored in little
     * endian format.
     * 
     * @param which Which byte to set (0 to 15).
     * @param value The value to set it to.
     * 
     * @throws java.io.IOException In case of problems.
     */
    
    public void setWord (int which, short value) throws IOException
    {
        if (which < 0 || which >= buffer.length / 2)
            throw new IOException ("HT16K33 invalid byte number");
        
        buffer[which*2 + 1] = (byte) (value >> 8);
        buffer[which*2] = (byte) value;
    }
    
    /**
     * Update the display.
     * 
     * @throws IOException In case of trouble.
     */
    public void update () throws IOException
    {
        device.write (0, buffer);
    }

/**
 * Test program.
 * 
 * @param args (ignored)
 * 
 * @throws IOException In case of trouble.
 * @throws com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException In case of trouble.
 * @throws InterruptedException In case of trouble.
 */    
    public static void main (String args[]) throws IOException, I2CFactory.UnsupportedBusNumberException, InterruptedException
    {
        HT16K33 h = new HT16K33 (I2CFactory.getInstance (I2CBus.BUS_1), 0x70);
        
        for (int i = 0; i < 128; ++i)
        {
            if (i > 0)
                h.setBit (i - 1, false);
            
            h.setBit (i, true);
            h.update ();
            
            Thread.sleep (50);
        }
    }

    /** The I2C device. */
    private final I2CDevice device;
    /** The data buffer */
    private final byte[] buffer = new byte[16];
    
    /** Address of the setup register. */
    static private final int SETUP_REG = 0x20;
    /** Command to turn the device on. */
    static private final byte SETUP_ON = 0x01;
    /** Address of the display setup register. */
    static private final int DISPLAY_REG = 0x80;
    /** Value to turn the display on. */
    static private final byte DISPLAY_ON = 0x01;
    /** Value for no blinking. */
    static private final byte DISPLAY_STEADY = 0x00;
    /** Value for blinking at 2 Hz. */
    static private final byte DISPLAY_TWOHZ = 0x01;
    /** Value for blinking at 1Hz. */
    static private final byte DISPLAY_ONEHZ = 0x02;
    /** Value for blinking at 0.5 Hz. */
    static private final byte DISPLAY_HALFHZ = 0x03;
    /** Address of brightness register. */
    static private final int BRIGHTNESS_REG = 0xe0;
}
