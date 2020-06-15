/*
 * Copyright (C) 2018 Jim Darby.
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

package Jimbo.Graphics;

/**
 * Super-class from the Matrix classes that takes out all the hard work
 * and does it in a single place once. Awesome!
 * 
 * @author Jim Darby
 * @param <T> What the Matrix is made of.
 */
public abstract class MatrixHelper <T> implements Matrix <T>
{
    /**
     * Constructor to set all the constants up.
     * 
     * @param width The Matrix's width.
     * @param height The Matrix's height.
     */
    public MatrixHelper (int width, int height)
    {
        WIDTH = width;
        HEIGHT = height;
        MAX_X = WIDTH - 1;
        MAX_Y = HEIGHT - 1;
        MAX = new Point (MAX_X, MAX_Y);
    }
    
    /**
     * Return the width of this matrix.
     * 
     * @return The width in pixels.
     */
    @Override
    public int getWidth ()
    {
        return WIDTH;
    }
    
    /**
     * Return the height of this matrix.
     * 
     * @return The height in Pixels.
     */
    @Override
    public int getHeight ()
    {
        return HEIGHT;
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
        return MAX;
    }
    
    /** The width of the board. */
    public final int WIDTH;
    /** The height of the board. */
    public final int HEIGHT;
    /** The maximum X value. */
    public final int MAX_X;
    /** The maximum Y value. */
    public final int MAX_Y;
    
    /** The maximum values as a Point. */
    private final Point MAX;
}
