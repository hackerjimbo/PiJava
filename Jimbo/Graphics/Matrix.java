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
 * This interface is the top level of the Matrix hierarchy. It just has
 * a size on it.
 * 
 * @author Jim Darby
 * @param <T> The type of pixel it is
 */
public interface Matrix <T>
{
    /**
     * Return a point with the maximum values for X and Y in this
     * matrix.
     * 
     * @return The maximum size.
     */
    public Point getMax ();
        
    /**
     * 
     * @param p
     * @param value
     */
    void setPixel (Point p, T value);
    /**
     * Update the display.
     * 
     * @throws java.io.IOException In case of trouble.
     */
    public void show () throws IOException;
}
