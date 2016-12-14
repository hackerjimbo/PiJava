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

import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;

/**
 * A class to talk to the MAX7219 display multiplexor.
 * 
 * @author Jim Darby
 */
public class MAX7219
{
    /**
     * Constructor for a MAX7219 object.
     * 
     * @param channel The SPI channel it's on.
     * @param chained How many of them are chained.
     * 
     * @throws IllegalArgumentException For an invalid argument.
     * @throws IOException In case of error.
     */
    public MAX7219 (SpiChannel channel, int chained) throws IllegalArgumentException, IOException
    {
        if (chained < 1)
            throw new IllegalArgumentException ("MAX7219 needs at least one device");
        
        this.chained = chained;
        
        // We set 10 MHz (device limit) but in practice it'll be a power of 2 so 8MHz.
        dev = SpiFactory.getInstance (channel, 10000000, SpiDevice.DEFAULT_SPI_MODE);
        
        // Allocate the buffer
        buffer = new byte[chained * BYTES_PER_DEV];
        
        for (int i = 0; i < buffer.length; ++i)
            buffer[i] = 0;
        
        tx_buffer = new byte[2 * chained];
    }
    
    /**
     * Put a single value into the byte for a specific digit. This version sends
     * it to all the displays.
     * 
     * @param digit The digit to update. 0 to 7.
     * @param value The byte value put in.
     * 
     * @throws IOException In case of error.
     */
    public void writeDigit (byte digit, byte value) throws IOException
    {
        validateDigit (digit);
        
        for (int i = 0; i < chained; ++i)
            buffer[i * BYTES_PER_DEV + digit] = value;
    }

    /**
     * Put a single value into the byte for a specific digit on a specific
     * device. The devices are numbered from 0 (the first in the chain) to
     * the chained parameter passed to the constructor minus 1.
     * 
     * @param device The device number.
     * @param digit The digit to update. 0 to 7.
     * @param value The byte value put in.
     * 
     * @throws IOException In case of error.
     */
    public void writeDigit (int device, byte digit, byte value) throws IOException
    {
        validateDevice (device);
        validateDigit (digit);
        
        buffer[device * BYTES_PER_DEV + digit] = value;
    }
    
    /**
     * Sets the decode mode for all devices.
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
        validateMode (mode);
        
        tx_update (REG_DECODE_MODE, mode);
    }
    
     /**
     * Sets the decode mode for a specific device..
     * 
     * @param device The device to be addressed. This starts at 0 and must be
     * less than the total number of devices.
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
    public void setDecodeMode (int device, byte mode) throws IOException
    {
        validateDevice (device);
        validateMode (mode);

        tx_update (REG_NO_OP, mode, device, REG_DECODE_MODE);
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
        validateIntensity (intensity);
        
        tx_update (REG_INTENSITY, (byte) intensity);
    }
    
    /**
     * Set the intensity of the device.
     * 
     * @param device The device to be addressed. This starts at 0 and must be
     * less than the total number of devices.
     * @param intensity The intensity level. It must be in the range 0 to 15.
     * 
     * @throws IOException In case of error.
     */
    public void setIntensity (int device, int intensity) throws IOException
    {
        validateDevice (device);
        validateIntensity (intensity);
        
        tx_update (REG_NO_OP, (byte) intensity, device, REG_INTENSITY);
    }
    
    /**
     * Set the scan limit on all devices. The scan limit is the number of
     * displays actually used. The value is between 0 and 7 for 1 to 8 displays.
     * 
     * @param limit The limit on the number of displays.
     * 
     * @throws IOException In case of error.
     */
    public void setScanLimit (int limit) throws IOException
    {
        validateDigit (limit);
        
        tx_update (REG_SCAN_LIMIT, (byte) limit);
    }
        
    /**
     * Set the scan limit on a specific device. The scan limit is the number of
     * displays actually used. The value is between 0 and 7 for 1 to 8 displays.
     * 
     * @param device The device this applies to.
     * @param limit The limit on the number of displays.
     * 
     * @throws IOException In case of error.
     */
    public void setScanLimit (int device, int limit) throws IOException
    {
        validateDevice (device);
        validateDigit (limit);
        
        tx_update (REG_NO_OP, (byte) limit, device, REG_SCAN_LIMIT);
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
        tx_update (REG_SHUTDOWN, active ? (byte) 0 : (byte) 1);
    }
    
    /**
     * Set the shutdown status of a specific device.
     * 
     * @param device The device this applies to.
     * @param active If true the device is shut down.
     * 
     * @throws IOException In case of problems.
     */
    public void shutdown (int device, boolean active) throws IOException
    {
        validateDevice (device);
        
        tx_update (REG_NO_OP, active ? (byte) 0 : (byte) 1, device, REG_SHUTDOWN);
    }
    
    /**
     * Set test mode for all displays. This sets all segments of all the
     * displays on.
     * 
     * @param active True to engage, false to turn off.
     * 
     * @throws IOException In case of problems.
     */
    public void displayTest (boolean active) throws IOException
    {
        tx_update (REG_DISPLAY_TEST, active ? (byte) 1 : (byte) 0);
    }
    
    /**
     * Set test mode for all displays. This sets all segments of the display on.
     * 
     * @param device The device this applies to.
     * @param active True to engage, false to turn off.
     * 
     * @throws IOException In case of problems.
     */
    public void displayTest (int device, boolean active) throws IOException
    {
        validateDevice (device);
        
        tx_update (REG_NO_OP, active ? (byte) 1 : (byte) 0, device, REG_DISPLAY_TEST);
    }
    
    /**
     * Send all the display information to the device. This is done in one go to
     * avoid excessive use of the bus on update of each digit.
     * 
     * @throws IOException In case of problems.
     */
    public void update () throws IOException
    {
        for (int digit = 0; digit < BYTES_PER_DEV; ++digit)
        {
            for (int device = 0; device < chained; ++device)
            {
                final int base = (chained - device - 1) * 2;
                tx_buffer[base] = (byte) (REG_DIGIT0 + digit);
                tx_buffer[base + 1] = buffer[device * BYTES_PER_DEV + digit];
            }
            
            dev.write (tx_buffer, 0, tx_buffer.length);
            
            /*System.out.print ("Buffer");
            
            for (int i = 0; i < tx_buffer.length; ++i)
                System.out.print (" " + tx_buffer[i]);
            
            System.out.println ();*/
        }
    }
    
    /**
     * Check if the device is valid.
     * 
     * @param device The device ID. Must be in the range 0 ≤ device < chained.
     */
    private void validateDevice (int device)
    {
        if (device < 0 || device >= chained)
            throw new IllegalArgumentException ("Invalid device");
    }
    
    /**
     * Check if the digit is valid.
     * 
     * @param digit The digit on the device. Must be in the range 0 ≤ digit < 8.
     */
    private void validateDigit (int digit)
    {
        if (digit < 0 || digit >= BYTES_PER_DEV)
            throw new IllegalArgumentException ("Invalid digit");
    }
    
    /**
     * Validate the device mode. It must be one of DECODE_NONE, DECODE_B_FOR_0,
     * DECODE_B_FOR_0_3 or DECODE_B_FOR_0_7.
     * 
     * @param mode The device mode.
     */
    private void validateMode (int mode)
    {
        switch (mode)
        {
            case DECODE_NONE:
            case DECODE_B_FOR_0:
            case DECODE_B_FOR_0_3:
            case DECODE_B_FOR_0_7:
                break;
                
            default:
                throw new IllegalArgumentException ("Invalid decode mode");
        }
    }
    
    /**
     * Validate the device intensity. It should be in the range 0 ≤ intensity 
     * < 16.
     * 
     * @param intensity The intensity.
     */
    private void validateIntensity (int intensity)
    {
        if (intensity < 0 || intensity > 0x0f)
            throw new IllegalArgumentException ("Invalid intensity");
    }
    
    /**
     * Transmit an update to all controllers. The command and parameter pair is
     * sent to each device in the chain.
     * 
     * @param command The command to send.
     * @param param The command's parameter.
     * 
     * @throws IOException In case of error.
     */
    private void tx_update (byte command, byte param) throws IOException
    {
        tx_update (command, param, -1, REG_NO_OP);
    }
    
    /**
     * Transmit an update to all controllers. The command and parameter pair is
     * sent to each device in the chain *except* for except which gets the
     * command exception.
     * 
     * @param command The command to send.
     * @param param The command's parameter.
     * @param except The device that gets a *different* command.
     * @param exception What the command to the exception device is.
     * 
     * @throws IOException In case of error.
     */
    private void tx_update (byte command, byte param, int except, byte exception) throws IOException
    {
        for (int i = 0; i < chained; ++i)
        {
            final int base = (chained - i - 1) * 2;
            tx_buffer[base] = (i == except) ? exception : command;
            tx_buffer[base + 1] = param;
        }

        /*System.out.print ("Buffer");
            
        for (int i = 0; i < tx_buffer.length; ++i)
            System.out.print (" " + tx_buffer[i]);
            
        System.out.println ();*/
            
        dev.write (tx_buffer, 0, tx_buffer.length);
    }
    
    
    public static void main (String args[]) throws IOException, InterruptedException
    {
        MAX7219 m = new MAX7219 (SpiChannel.CS0, 2);
        
        m.shutdown (false);
        
        m.displayTest (true);
        Thread.sleep (1000);
        m.displayTest (0, false);
        
        m.setIntensity ((byte) 8);
        m.setScanLimit ((byte) 7);
        
        for (byte digit = 0; digit < 8; ++digit)
            m.writeDigit (0, digit, (byte) 0);
        
        m.setDecodeMode (DECODE_B_FOR_0_7);
        
         for (byte digit = 0; digit < 8; ++digit)
            for (byte value = 0; value < 0x10; ++value)
            {
                m.writeDigit (0, digit, value);
                m.update ();
                Thread.sleep (500);
                m.writeDigit (0, digit, (byte) 0);
            }
         
        m.setDecodeMode (DECODE_NONE);
        
        for (byte digit = 0; digit < 8; ++digit)
            for (byte bit = 0; bit < 8; ++bit)
            {
                m.writeDigit (0, digit, (byte) (1 << bit));
                m.update ();
                Thread.sleep (500);
                m.writeDigit (0, digit, (byte) 0);
            }
        
        m.update ();
    }

    /** Number of chained devices. */
    private final int chained;
    /** The SPI device we're going to use. */
    private final SpiDevice dev;
    /** The buffer that holds all the display information. */
    private final byte[] buffer;
    /** The buffer we use to transmit commands. */
    private final byte[] tx_buffer;
    
    /** No decode mode. The bit maps goes to the segments. */
    public static final byte DECODE_NONE       = 0x00;
    /** Just decode the first display. */
    public static final byte DECODE_B_FOR_0    = 0x01;
    /** Decode displays 0 to 3 and not 4 to 7. */
    public static final byte DECODE_B_FOR_0_3  = 0x0f;
    /** Decode all the displays. */
    public static final byte DECODE_B_FOR_0_7  = (byte) 0xff;
    
    /** The number of bytes per device. Also the number of digits per device. */
    private static final int  BYTES_PER_DEV    = 8;
    /** The NO-OP register. */
    private static final byte REG_NO_OP        = 0x0;
    /** The register for digit 0. */
    private static final byte REG_DIGIT0       = 0x1;
    /** The register for digit 1. */
    private static final byte REG_DIGIT1       = 0x2;
    /** The register for digit 2. */
    private static final byte REG_DIGIT2       = 0x3;
    /** The register for digit 3. */
    private static final byte REG_DIGIT3       = 0x4;
    /** The register for digit 4. */
    private static final byte REG_DIGIT4       = 0x5;
    /** The register for digit 5. */
    private static final byte REG_DIGIT5       = 0x6;
    /** The register for digit 6. */
    private static final byte REG_DIGIT6       = 0x7;
    /** The register for digit 7. */
    private static final byte REG_DIGIT7       = 0x8;
    /** The register that controls the decode mode. */
    private static final byte REG_DECODE_MODE  = 0x9;
    /** The register that controls the intensity. */
    private static final byte REG_INTENSITY    = 0xa;
    /** The register that controls the scan limit. */
    private static final byte REG_SCAN_LIMIT   = 0xb;
    /** The register that controls shutdown. */
    private static final byte REG_SHUTDOWN     = 0xc;
    /** The register that controls the display test. */
    private static final byte REG_DISPLAY_TEST = 0xf;
}
