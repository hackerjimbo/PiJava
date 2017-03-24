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
 * This class describes a colour.
 * 
 * @author Jim Darby
 */
public class Colour
{
    /**
     * Create from red, green and blue values. All in the range 0 to 255.
     * If this condition isn't met it will throw an IllegalArgumentException.
     * 
     * @param red The red component.
     * @param green The green component.
     * @param blue  The blue component.
     */
   public Colour (int red, int green, int blue)
   {
       if (red < 0 || red > 255 ||
               green < 0 || green > 255 ||
               blue < 0 || blue > 255)
            throw new IllegalArgumentException ("Invalid parameter to Colour");
       
       this.red   = red;
       this.green = green;
       this.blue  = blue;
   }
   
   /**
    * Create the colour from an angle (in degrees) in a colour wheel. 0
    * is full red, 120 is full green and 240 is full blue. It must be in the
    * range 0 to 360 inclusive. If this condition isn't met it will throw an
    * IllegalArgumentException.
    * 
    * @param angle The angle in the range 0 to 360 inclusive.
    */
   public Colour (double angle)
   {
       if (angle >= 0 && angle < 120)
       {
           red = (int) (255 * (120.0 - angle) / 120.0);
           green = 255 - red;
           blue = 0;
           
           return;
       }
       
       if (angle >= 120 && angle < 240)
       {
           red = 0;
           green = (int) (255 * (240.0 - angle) / 120.0);
           blue = 255 - green;
           
           return;
       }
       
       if (angle >= 240 && angle <= 360)
       {
           green = 0;
           blue = (int) (255 * (360.0 - angle) / 120.0);
           red = 255 - blue;
           
           return;
       }
       
       throw new IllegalArgumentException ("Invalid parameter to Colour");
   }
   
   /**
    * Return the red component.
    * 
    * @return The value.
    */
   public int getRed ()
   {
       return red;
   }
   
   /**
    * Return the green component.
    * 
    * @return The value.
    */
   public int getGreen ()
   {
       return green;
   }
   
   /**
    * Return the blue component.
    * 
    * @return The value.
    */
   public int getBlue ()
   {
       return blue;
   }
   
   /** Where we store the red value. */
   final private int red;
   /** Where we store the green value. */
   final private int green;
   /** Where we store the blue value. */
   final private int blue;
}
