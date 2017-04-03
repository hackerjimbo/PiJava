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
 * Do a demo on an arbitrary MonoMatrix.
 * 
 * @author Jim Darby
 */
public class MonoMatrixDemo
{
    /**
     * Run the demo given a ColourMatrix.
     * 
     * @param m The ColourMatrix to use.
     * 
     * @throws InterruptedException In case of thread badness.
     * @throws java.io.IOException In case of trouble.
     */
    public static void run (MonoMatrix m) throws InterruptedException, IOException
    {
        final Point limits = m.getMax ();
        final int max_x = limits.getX ();
        final int max_y = limits.getY ();
        
        while (true)
        {
            for (int y = 0; y <= max_y; ++y)
                for (int x = 0; x <= max_x; ++x)
                {
                    m.setPixel (x, y, 255);
                    m.show ();
                    Thread.sleep (50);
                    m.setPixel (x, y, 0);
                    m.show ();
                }
        }
    }
}
