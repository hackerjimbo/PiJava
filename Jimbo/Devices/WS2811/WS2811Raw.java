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

package Jimbo.Devices.WS2811;

/**
 * The most basic interface to the native libraries. We try and do as much as
 * possible in Java land.
 * 
 * @author Jim Darby
 */

public class WS2811Raw
{
    /**
     * Initialise the system. Only one user may use it at once.
     * @param type The type of the string. One of WS2811_STRIP_RGB,
     * WS2811_STRIP_RBG, WS2811_STRIP_GRB, WS2811_STRIP_GBR, WS2811_STRIP_BRG
     * or WS2811_STRIP_BGR.
     * 
     * @param length the number of units in the string
     * 
     * @return If it worked.
     */
    public static native boolean ws2811_init (int type, int length);
    
    /**
     * Set the brightness level. The value is a double in the range 0.0 to 1.0.
     * 
     * @param level The scaling in the range 0 to 255.
     * 
     * @return true if it worked.
     */
    public static native boolean ws2811_brightness (int level);
    
    /**
     * Send the data to the string.
     * 
     * @param data An array of ints, one per unit in the string. This must me
     * the same length as used for the ws2811_init method.
     * 
     * @return If it worked.
     */
    public static native boolean ws2811_update (int[] data);
    
    /**
     * Wait for the DMA to complete.
     * 
     * @return If it worked.
     */
    public static native boolean ws2811_wait ();
    
    /**
     * Shut the system down.
     * 
     * @return If it worked.
     */
    public static native boolean ws2811_close ();
    
    public static final int WS2811_STRIP_RGB = 0x100800;
    public static final int WS2811_STRIP_RBG = 0x100008;
    public static final int WS2811_STRIP_GRB = 0x081000;
    public static final int WS2811_STRIP_GBR = 0x080010;
    public static final int WS2811_STRIP_BRG = 0x001008;
    public static final int WS2811_STRIP_BGR = 0x000810;
}
