/*
 * Copyright (C) 2016-2018 Jim Darby.
 *
 * This software is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, If not, see
 * <http://www.gnu.org/licenses/>.
 */

package Jimbo.Boards.com.pimoroni;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;

import Jimbo.Devices.APA102;

import Jimbo.Graphics.ColourMatrixDemo;

import java.io.IOException;

/**
 * This class drives a Pimoroni Blinkt LED string.
 *
 * @author Jim Darby
 */
public class Blinkt extends APA102
{
    public Blinkt ()
    {
        super(GpioFactory.getInstance(), RaspiPin.GPIO_04, RaspiPin.GPIO_05, 8);
    }
    
    public static void main (String args[]) throws InterruptedException, IOException
    {
        final Blinkt b = new Blinkt ();
        
        ColourMatrixDemo.run (b);
    }
}
