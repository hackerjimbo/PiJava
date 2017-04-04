/*
 * Copyright (C) 2016-2017 Jim Darby.
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

package Jimbo.Devices.WS2811;

import Jimbo.Graphics.Mapping;
import Jimbo.Graphics.Point;
import Jimbo.Graphics.Colour;
import Jimbo.Graphics.ColourMatrix;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Provide a sensible interface to the WS2811 library. IT tries to
 * provide access to the essentials of the underlying C library. 
 * 
 * @author Jim Darby
 */
public class WS2811 implements ColourMatrix
{
    /**
     * Create an interface to the WS2811 hardware.
     * 
     * @param width The width of the display.
     * @param height The height of the display.
     * @param map A mapping that takes (0,0) as the lower left and converts it
     * into whatever the display actually uses.
     * @param type The type of the display. One of WS2811Raw.WS2811_STRIP_RGB,
     * SWS2811Raw.W2811_STRIP_RBG, WS2811Raw.WS2811_STRIP_GRB,
     * WS2811Raw.WS2811_STRIP_GBR, WS2811Raw.WS2811_STRIP_BRG,
     * WS2811Raw.WS2811_STRIP_BGR.
     * @param brightness A scaling factor for the brightness: [0.0,1.0].
     */
    public WS2811 (int width, int height,
		   Mapping map,
		   int type,
		   double brightness)
    {
        if (width <= 0 || height <= 0 ||
                (type != WS2811Raw.WS2811_STRIP_RGB) &&
                (type != WS2811Raw.WS2811_STRIP_RBG) &&
                (type != WS2811Raw.WS2811_STRIP_GRB) &&
                (type != WS2811Raw.WS2811_STRIP_GBR) &&
                (type != WS2811Raw.WS2811_STRIP_BRG) &&
                (type != WS2811Raw.WS2811_STRIP_BGR) ||
                brightness < 0 || brightness > 1)
            throw new IllegalArgumentException ("Invalid parameter to WS2811");

	i_width = width;
	i_height = height;
	max = new Point (i_width - 1, i_height - 1);
	
	final Point out = map.getOutMax ();
	
        final int o_width = out.getX () + 1;
        final int o_height = out.getY () + 1;
		
        leds = o_width * o_height;
	this.map = new int[leds];
        data = new int[leds];

	for (int y = 0; y < i_height; ++y)
	    for (int x = 0; x < i_width; ++x)
            {
		final Point p = map.map (new Point (x, y));
		final int value = p.getX () + o_width * p.getY ();
		
		this.map[x + i_width * y] = value;
	    }
	
        for (int i = 0; i < leds; ++i)
            data[i] = 0;
        
        loadNative ();
        
        if (!WS2811Raw.ws2811_init (type, leds))
            throw new IllegalArgumentException ("Unable to start WS2811");
        
        WS2811Raw.ws2811_brightness ((int) (brightness * 255));
        WS2811Raw.ws2811_update (data);
    }
    
    /**
     * Set a specific pixel to a specific RGB value. This works in the
     * most efficient way.
     * 
     * @param p The point to set.
     * @param r Red value: [0,255].
     * @param g Green value: [0,255].
     * @param b Blue value: [0,255].
     */
    @Override
    public void setPixel (Point p, int r, int g, int b)
    {       
	if (!p.inside (max) ||
                r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255)
            throw new IllegalArgumentException ("Invalid parameter to WS2811.setPixel");

	final int x = p.getX ();
	final int y = p.getY ();
	
	// System.out.println ("pos " + x + ',' + y + " -> " + map[x + i_width * y]);
	 
        data[map[x + i_width * y]] = (r << 16) | (g << 8) | b;
    }
    
    /**
     * Set  a specific pixel to a specific colour. This is the generic
     * interface.
     * 
     * @param p The pixel to set.
     * @param c The colour to set.
     */
    @Override
    public void setPixel (Point p, Colour c)
    {
        setPixel (p, c.getRed (), c.getGreen (), c.getBlue ());
    }
    
    /**
     * Send the data to the string.
     */
    @Override
    public void show ()
    {
        WS2811Raw.ws2811_update (data);
    }
    
    /**
     * Return a point with the maximum values for X and Y in this
     * matrix.
     * 
     * @return The maximum size.
     */
    @Override
    public Point getMax ()
    {
        return max;
    }
    
    /**
     * Shut everything down.
     */
    void close ()
    {
        WS2811Raw.ws2811_close ();
    }
    
    /**
     * Support routine to load the native library. Very strongly inspired
     * by code from the pi4j library itself.
     */
    private static void loadNative ()
    {
        if (nativeLoaded)
            return;
        
        try
        {
            final String path = "/Jimbo/Devices/WS2811/libjavaws2811.so";    
            
            Path inputPath = Paths.get (path);

            if (!inputPath.isAbsolute ())
                throw new IllegalArgumentException ("The path has to be absolute, but found: " + inputPath);

            final String fileNameFull = inputPath.getFileName ().toString ();
            final int dotIndex = fileNameFull.indexOf ('.');

            if (dotIndex < 0 || dotIndex >= fileNameFull.length () - 1)
                throw new IllegalArgumentException ("The path has to end with a file name and extension, but found: " + fileNameFull);

            final String fileName = fileNameFull.substring (0, dotIndex);
            final String extension = fileNameFull.substring (dotIndex);

            final Path target = Files.createTempFile (fileName, extension);
            final File targetFile = target.toFile ();

            targetFile.deleteOnExit ();

            // System.out.println ("Tempfile at " + target);

            try (InputStream source = WS2811.class.getResourceAsStream (inputPath.toString ()))
            {
                if (source == null)
                    throw new FileNotFoundException ("File " + inputPath + " was not found in classpath.");

                Files.copy (source, target, StandardCopyOption.REPLACE_EXISTING);
            }

            // Finally, load the library
            System.load (target.toAbsolutePath ().toString ());
	}
        
        catch (Exception | UnsatisfiedLinkError e)
        {
            System.out.println ("Failed to load native library: " + e);
	}

        nativeLoaded = true;
    }

    /** The total input width. */
    final private int i_width;
    /** The total input height. */
    final private int i_height;
    /** A point containing the maximum values for X and Y. */
    final private Point max;
    
    /** The total number of LEDs (WS2811s to be precise) we have. */
    final private int leds;

    /** The map from input (X,Y) to data (X,Y). */
    final private int[] map;
    /** The data of what is on the string (or will be when show is called. */
    final private int[] data;
    
    /** Have we loaded the native library. */
    private static boolean nativeLoaded = false;
}
