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

package Jimbo.Boards.me.kano;

import Jimbo.Graphics.MonoMatrix;
import Jimbo.Graphics.MonoMatrixDemo;
import Jimbo.Graphics.Point;
import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.DataBits;
import com.pi4j.io.serial.FlowControl;
import com.pi4j.io.serial.Parity;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialDataEventListener;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialPort;
import com.pi4j.io.serial.StopBits;
import java.io.IOException;

/**
 * This class controls the Kano LightShow board that's part of the powerup kit.
 * 
 * @author Jim Darby
 */
public class LightShow implements MonoMatrix
{
    /**
     * Constructor for the Kano LightShow that comes with the powerup
     * kit.
     * 
     * @throws IOException In case of error.
     * @throws InterruptedException In case of error.
     */
    public LightShow () throws IOException, InterruptedException
    {
        port = SerialFactory.createInstance ();
        
        // Add a daya sink for any input
        port.addListener (new DataSink ());
        
        // Now configure it. This is hardcoded for the LightShow.
        final SerialConfig config = new SerialConfig ();

        config.device (SerialPort.getDefaultPort()).
                baud (Baud._38400).
                dataBits( DataBits._8).
                parity (Parity.NONE).
                stopBits (StopBits._1).
                flowControl (FlowControl.NONE);
        
        port.open (config);
        
        data = new byte[WIDTH * HEIGHT];
        
        for (int i = 0; i < data.length; ++i)
            data[i] = 0;
        
        show ();
    }

   /**
     * Set a specific pixel on or off. This works in the most efficient
     * way.
     * 
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param pwm The PWM value.
     */
    @Override
    public void setPixel (int x, int y, int pwm)
    {
        if (x < 0 || x > MAX_X || y < 0 || y > MAX_Y || pwm < 0 || pwm > MAX_PWM)
            throw new IllegalArgumentException ("Invalid parameters for setPixel");
        
        data[x + WIDTH * (MAX_Y - y)] = (byte) (pwm >> 5);
    }
    
   /**
     * Set a pixel in the generic way.
     * 
     * @param p The pixel.
     * @param value The value.
     */
    @Override
    public void setPixel (Point p, Integer value)
    {
        setPixel (p.getX (), p.getY (), value);
    }
    
    /**
     * Update the displayed data. Call this after setting up what you want
     * displayed and it will transfer it to the device and hence actually
     * display it.
     * 
     * @throws IOException In case of error
     */
    @Override
    public final void show() throws IOException
    {
        port.write (START);
        port.write (data);
    }
    
    /**
     * Return the maximum X and Y values as a Point.
     * 
     * @return The maximum X and Y values as a Point 
     */
    @Override
    public Point getMax()
    {
        return MAX;
    }

    /**
     * A class that implements the SerialDataEventListener interface
     * and just ignores everything that happens. We need this otherwise
     * the data just piles up. 
     */
    private static class DataSink implements SerialDataEventListener
    {
        /**
         * Read and ignore any data.
         * 
         * @param e The data to ignore
         */
        @Override
        public void dataReceived(SerialDataEvent e)
        {
            // And ignore it!
        }
    }
    
    public static void main (String args[]) throws IOException, InterruptedException
    {
        LightShow l = new LightShow ();
        
        MonoMatrixDemo.run (l);
    }
    
    /** The display's width. */
    private static final int WIDTH = 9;
    /** The display's height. */
    private static final int HEIGHT = 14;
    /** The maximum X value. */
    public final static int MAX_X = WIDTH - 1;
    /** The maximum Y value. */
    public final static int MAX_Y = HEIGHT - 1;
    /** The maximum values as a Point. */
    private final static Point MAX = new Point (MAX_X, MAX_Y);

    /** Start marker. */
    private static final byte START = 0x55;
    /** Maximum PWM value. */
    private static final int MAX_PWM = 255;

    
    /** The serial port to use. */    
    private final Serial port;
    /** The data for the display. */
    private final byte[] data;
}
