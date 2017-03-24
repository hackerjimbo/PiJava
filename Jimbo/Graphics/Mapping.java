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
 * This defines the generic mapping class hierarchy. It takes an input Point
 * and produces an output point.
 * 
 * @author Jim Darby
 */
public abstract class Mapping
{
    /**
     * Define a mapping where both input and output sizes are the same.
     * 
     * @param inOutMax A point giving the maximum values for X and Y in both
     * input and output
     */
    public Mapping (Point inOutMax)
    {
	inMax = inOutMax;
	outMax = inOutMax;
	before = null;
    }
    
    /**
     * Define a mapping where input and output sizes are different.
     * 
     * @param inMax The maximum input size.
     * @param outMax The maximum output size.
     */
    public Mapping (Point inMax, Point outMax)
    {
	this.inMax = inMax;
	this.outMax = outMax;
	before = null;
    }

    /**
     * Define a mapping where the input size is determined by a previous
     * transformation and the output size is given. The previous transformation
     * is remembered and performed whenever a mapping is done.
     * 
     * @param before The previous transformation.
     * @param outMax The output size.
     */
    public Mapping (Mapping before, Point outMax)
    {
	this.inMax = before.getOutMax ();
	this.outMax = outMax;
	this.before = before;
    }

    /**
     * Validate that the input parameter is within the size expected,
     * 
     * @param p The input point.
     */
    public void validateIn (Point p)
    {
	if (p.inside (inMax))
	    return;

	throw new IllegalArgumentException ("Input co-ordinate " + p + " outside " + inMax);
    }

    /**
     * Validate that the output parameter is within the size expected,
     * 
     * @param p The output point.
     */
    public void validateOut (Point p)
    {
	if (p.inside (outMax))
	    return;

	throw new IllegalArgumentException ("Output co-ordinate " + p + " outside " + outMax);
    }

    /**
     * Return the input limit.
     * 
     * @return The input limit.
     */
    public Point getInMax ()
    {
	return inMax;
    }

    /**
     * Return the original input limit. If we have a previous mapping then ask
     * that otherwise return ours.
     * 
     * @return The original input limit.
     */
    public Point getOriginalMax ()
    {
	return (before == null) ? inMax : before.getOriginalMax ();
    }

    /**
     * Return the output limit.
     * 
     * @return The output limit.
     */
    public Point getOutMax ()
    {
	return outMax;
    }

    /**
     * Abstract method to perform a mapping.
     * 
     * @param p The point to map.
     * @return The mapped value.
     */
    abstract public Point map (Point p);
    
    /**
     * Return a printable version of the mapping.
     * 
     * @return A printable String. 
     */
    @Override
    abstract public String toString ();

    /** The input maximum. */
    protected final Point inMax;
    /** The output maximum. */
    protected final Point outMax;
    /** The previous mapping. */
    protected Mapping before;
}
