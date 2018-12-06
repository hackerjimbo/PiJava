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
 * License along with this library; if not, see
 * <http://www.gnu.org/licenses/>.
 */

package Jimbo.Graphics;

import java.io.IOException;

/**
 * Do a demo on an arbitrary ColourMatrix.
 * 
 * @author Jim Darby
 */
public class ColourMatrixDemo implements Runnable
{
    public ColourMatrixDemo (ColourMatrix m)
    {
        this.m = m;
    }
    
    @Override
    public void run ()
    {
        try
        {
            run (m);
        }
        
        catch (InterruptedException | IOException e)
        {
            System.out.println ("ColourMatrixDemo thread got an exception: " + e);
        }
    }
    
    /**
     * Run the demo given a ColourMatrix. This is a static, non-threaded
     * version but is used by the threaded non-static version.
     * 
     * @param m The ColourMatrix to use.
     * 
     * @throws InterruptedException In case of thread badness.
     * @throws java.io.IOException In case of trouble.
     */
    public static void run (ColourMatrix m) throws InterruptedException, IOException
    {
        final Point limits = m.getMax ();
        final int max_x = limits.getX ();
        final int max_y = limits.getY ();
        
        // Basic scan
        
        for (int y = 0; y <= max_y; ++y)
            for (int x = 0; x <= max_x; ++x)
            {
                final Point p = new Point (x, y);
                
                for (int phase = 0; phase < 3; ++phase)
                {
                    m.setPixel (p,
                            (phase == 0) ? 0x80 : 0x00,
                            (phase == 1) ? 0x80 : 0x00,
                            (phase == 2) ? 0x80 : 0x00);
                    m.show ();
                    Thread.sleep (100);
                    m.setPixel (p, 0x00, 0x00, 0x00);
                    m.show ();
                    Thread.sleep (25);
                }
            }
        
        // "I made it rainbow!" TM
        
        int phase = 0;
        
        final double max_distance = Math.sqrt (max_x * max_x + max_y * max_y);
        
        while (true)
        {
            for (int y = 0; y <= limits.getY (); ++y)
                for (int x = 0; x <= limits.getX (); ++x)
                {
                    final double distance = Math.sqrt (x*x + y*y);
                    final double fraction = distance / max_distance;
                    
                    double value = 360 * (1 - fraction) + phase;
                    
                    if (value > 360)
                        value -= 360;
                    
                    final Colour c = new Colour (value);
                    
                    m.setPixel (new Point (x, y), c);
                }
            
            m.show ();
            
            phase += 1;
            
            if (phase == 360)
                phase = 0;
            
            Thread.sleep (10);
        }
    }
    
    /** The matrix we'll be working on. */
    private final ColourMatrix m;
}
