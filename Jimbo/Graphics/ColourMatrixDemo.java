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

/**
 * Do a demo on an arbitrary ColourMatrix.
 * 
 * @author Jim Darby
 */
public class ColourMatrixDemo
{
    /**
     * Run the demo given a ColourMatrix.
     * 
     * @param m The ColourMatrix to use.
     * 
     * @throws InterruptedException 
     */
    public static void run (ColourMatrix m) throws InterruptedException
    {
        final Point limits = m.getMax ();
        final int max_x = limits.getX ();
        final int max_y = limits.getY ();
        
        // Basic scan
        
        int phase = 0;
        
        for (int y = 0; y <= max_y; ++y)
            for (int x = 0; x <= max_x; ++x)
            {
                Point p = new Point (x, y);
                m.setPixel (p,
                        (phase == 0) ? 0x80 : 0x00,
                        (phase == 1) ? 0x80 : 0x00,
                        (phase == 2) ? 0x80 : 0x00);
                m.show ();
                Thread.sleep (250);
                m.setPixel (p, 0x00, 0x00, 0x00);
                m.show ();
                Thread.sleep (50);
                
                phase = (phase + 1) % 3;
            }
        
        // "I made it rainbow!" TM
        
        phase = 0;
        
        final double max_distance = Math.sqrt (max_x * max_x + max_y * max_y);
        
        while (true)
        {
            for (int y = 0; y <= limits.getY (); ++y)
                for (int x = 0; x <= limits.getX (); ++x)
                {
                    final double distance = Math.sqrt (x*x + y*y);
                    final double fraction = distance / max_distance;
                    
                    double value = 360 * fraction + phase;
                    
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
}
