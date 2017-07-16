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
 * License along with this library.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package Jimbo.Devices;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;


/**
 * A class to talk to the BMP280 digital pressure and temperature
 * sensor.
 * 
 * @author Jim Darby
 */
public class BMP280
{
    /**
     * This class is used to return the result from a reading of a
     * BMP280 device. It is (typically) constructed by the {@code BMP280}
     * class as a result of asking for a reading and returns the values read in
     * a convenient form.
     */
    public class Result
    {
         /**
          * This constructs the result value.
          * 
          * @param pressure The pressure (in Pa).
          * @param temperature The temperature (in degrees Celsius).
          */
        public Result (double pressure, double temperature)
        {
            this.pressure = pressure;
            this.temperature = temperature;
        }
        
        /**
         * This method returns the pressure reading in Pa. The value is
         * set in the constructor and cannot be modified.
         * 
         * @return The pressure in Pa. 
         */
        public double getPressure ()
        {
            return pressure;
        }
        
        /**
         * This methods returns the temperature in degrees Celsius times 10.
         * The value is set in the constructor and cannot be modified.
         * 
         * @return The temperature in degrees Celsius.
         */
        public double getTemperature ()
        {
            return temperature;
        }
        
        /** Where we hold the pressure (in Pa). */
        private final double pressure;
        /** Where we hold the temperature (in Celsius). */
        private final double temperature;
    }
    
    public static void main (String args[]) throws I2CFactory.UnsupportedBusNumberException, IOException, InterruptedException
    {
        BMP280 b = new BMP280 (I2CFactory.getInstance (I2CBus.BUS_1), 0x77);
        
        while (true)
        {
            Result r = b.read ();
            System.out.println ("Temperature " + r.getTemperature () + " pressure " +r.getPressure ());
            Thread.sleep (1000);
        }
    }
    
    /**
     * This is the constructor for the BMP280 class. It takes the bus the
     * device is on and the address on that bus (which can be varied by setting
     * various pins on the device.
     * 
     * @param bus The {@code I2CBus} the device is on.
     * @param dev The device address on that bus (it can be changed).
     * 
     * @throws IOException If something goes amiss talking to the device.
     */
    public BMP280 (I2CBus bus, int dev) throws IOException
    {
        // Get a device object to use for communication.
        device = bus.getDevice (dev);
        
        // Verify it really is a BMP280
        final int signature = device.read (SIGNATURE_REG);
        
        if (signature != SIGNATURE)
            throw new IOException ("BMP280: Invalid signature (" + signature + ')');
        
        // Load the device calibration data (all in one go!).
        final int got = device.read (CALIBRATION_REG, buffer, 0, BUFFER_SIZE);
        
        // Did we get it all?
        if (got != BUFFER_SIZE)
            throw new IOException ("BMP280: Failed to read calibration coefficients");
        
        // The values are all 16-bit but T1 and P1 are unsigned. As Java
        // doesn't have unsigned variables but bytes are signed we take great
        // care with the following shifts and masks and place the results in
        // 32-bit ints where they fit properly.
        T1 = ((buffer[ 1] & 0xff) << 8) | (buffer[ 0] & 0xff);
        T2 = ((buffer[ 3]       ) << 8) | (buffer[ 2] & 0xff);
        T3 = ((buffer[ 5]       ) << 8) | (buffer[ 4] & 0xff);
        P1 = ((buffer[ 7] & 0xff) << 8) | (buffer[ 6] & 0xff);
        P2 = ((buffer[ 9]       ) << 8) | (buffer[ 8] & 0xff);
        P3 = ((buffer[11]       ) << 8) | (buffer[10] & 0xff);
        P4 = ((buffer[13]       ) << 8) | (buffer[12] & 0xff);
        P5 = ((buffer[15]       ) << 8) | (buffer[14] & 0xff);
        P6 = ((buffer[17]       ) << 8) | (buffer[16] & 0xff);
        P7 = ((buffer[19]       ) << 8) | (buffer[18] & 0xff);
        P8 = ((buffer[21]       ) << 8) | (buffer[20] & 0xff);
        P9 = ((buffer[23]       ) << 8) | (buffer[22] & 0xff);
        
        if (DEBUGGING)
        {
            System.out.println ("T: " + T1 + ' ' + T2 + ' ' + T3);
            System.out.println ("P: " + P1 + ' ' + P2 + ' ' + P3 + ' ' + P4 + ' ' +
                    P5 + ' ' + P6 + ' ' + P7 + ' ' + P8 + ' ' + P9);
        }
        
        config (4, 4, false);
        control (5, 5, 3);
    }
    
    /**
     * Test constructor for the BMP180. It runs through the standard test
     * sequence for the temperature compensation to verify it.
     */
    private BMP280 ()
    {
        device = null;
        
        T1 = 27504;
        T2 = 26435;
        T3 = -1000;
        
        if (compensateTemperatureInt (519888) != 2508)
            System.out.println ("Failed integer temperature validation!");
        
        if (!within (compensateTemperatureFloat (519888), 25.08, 0.01))
            System.out.println ("Failed floating point temperature validation");
        
        P1 = 36477;
        P2 = -10685;
        P3 = 3024;
        P4 = 2855;
        P5 = 140;
        P6 = -7;
        P7 = 15500;
        P8 = -14600;
        P9 = 6000;
        
        if (compensatePressureInt (415148) != 100653)
            System.out.println ("Failed integer pressure validation!");
        
        if (!within (compensatePressureFloat (415148), 100653.27, 0.01))
            System.out.println ("Failed floating point pressure validation!");
    }
    
    /**
     * Set up the control register. For full details you'll need to read the
     * data sheet.
     * 
     * @param temperature_oversampling 0 means skip reading temperature. 1 to
     * 5 means oversample 2^(n-1) times. 6 and above mean oversample 16 times.
     * Only values in the range 0 to 7 are acceptable.
     * @param pressure_oversampling 0 means skip reading temperature. 1 to
     * 5 means oversample 2^(n-1) times. 6 and above mean oversample 16 times.
     * Only values in the range 0 to 7 are acceptable.
     * @param power_mode 0 is sleep mode, 1 and 2 are forced mode and 3 is
     * normal mode.
     * 
     * @throws IOException In case of error.
     */
    public final void control (int temperature_oversampling,
            int pressure_oversampling,
            int power_mode) throws IOException
    {
        if (temperature_oversampling < 0 || temperature_oversampling > 7 ||
                pressure_oversampling < 0 || pressure_oversampling > 7 ||
                power_mode < 0 || power_mode > 3)
            throw new IOException ("BMP280: Invalid control");
        
        final byte value = (byte) ((temperature_oversampling << 5) |
                (pressure_oversampling << 2) | power_mode);
        
        device.write (CONTROL_REG, value);
    }
    
    /**
     * Set the configuration register. For full details you'll need to read
     * the data sheet.
     * 
     * @param inactive Set the inactive duration.
     * @param filter Set the IIR filter parameter.
     * @param spi Switch to SPI mode.
     * 
     * @throws IOException In case of error.
     */
    public final void config (int inactive, int filter, boolean spi) throws IOException
    {
        if (inactive < 0 || inactive > 7 ||
                filter < 0 || filter > 7)
            throw new IOException ("BMP280: Invalid config");
        
        final byte value = (byte) ((inactive << 5) | (filter << 2) | (spi ? 1 : 0));
        
        device.write (CONFIG_REG, value);
    }
    
    public Result read () throws IOException
    {
        if (device.read (RESULTS_REG, buffer, 0, DATA_SIZE) != DATA_SIZE)
            throw new IOException ("BMP280: Short data read");

        final int pressure_adc = ((buffer[0] & 0xff) << 12) +
                ((buffer[1] & 0xff) << 4) + ((buffer[2] & 0xff) >> 4);
        final int temperature_adc = ((buffer[3] & 0xff) << 12) +
                ((buffer[4] & 0xff) << 4) + ((buffer[5] & 0xff) >> 4);
        
        // They need to be calculated in the order as the temperature
        // calculation sets a varaible to asist with pressure calculation.
        // Thanks Bosch!
        
        final double temperature = compensateTemperatureFloat (temperature_adc);
        final double pressure = compensatePressureFloat (pressure_adc);
        
        if (DEBUGGING)
        {
            System.out.println ("Raw: pressure " + pressure_adc + " temperature " + temperature_adc);
            System.out.println ("Cooked: temperature " + temperature + " pressure " + pressure);
        }
        
        return new Result (pressure, temperature);
    }
    
    /**
     * Calculate the compensated temperature using the integer version
     * of the algorithm.
     * 
     * @param adc The ADC value
     * 
     * @return The temperature in hundreths of a degree Celsius.  
     */
    private int compensateTemperatureInt (int adc)
    {
        // This horror is from the data sheet. Don't blame me!
        
        final int var1 = ((((adc >> 3) - (T1 << 1))) * (T2)) >> 11;
        final int var2 = (((((adc >> 4) - (T1)) * ((adc >> 4) - (T1))) >> 12) * (T3)) >> 14;
        t_fine_int = var1 + var2;
        final int T = (t_fine_int * 5 + 128) >> 8;
        
        if (DEBUGGING)
            System.out.println ("var1 " + var1 + " var2 " + var2 + " t_fine " + t_fine_int + " T " + T);

        return T;
    }
    
    /**
     * Calculate the compensated temperature using the floating point
     * version of the algorithm.
     * 
     * @param adc The ADC value
     * 
     * @return The temperature in degrees Celsius.  
     */
    private double compensateTemperatureFloat (int adc)
    {
        // This horror is from the data sheet. Don't blame me!
      
        final double val1 = (adc / 16384.0 - T1 / 1024.0) * T2;
        final double val2 = ((adc / 131072.0) - T1 / 8192.0) * (adc / 131072.0 - T1 / 8192.0) * T3;
        
        t_fine_double = val1 + val2;
        
        final double T = t_fine_double / 5120;
        
        if (DEBUGGING)
            System.out.println ("val1 " + val1 + " val2 " + val2 + " t_fine " + t_fine_double + " T " + T);
        
        return T;
    }
    
    /**
     * Calculate the compensated pressure using integer arithmetic.
     * 
     * @param adc The ADC result.
     * 
     * @return The pressure in Pascals.
     */
    private int compensatePressureInt (int adc)
    {
        // This horror is from the data sheet. Don't blame me!
        
        long var1, var2, p;
        var1 = ((long) t_fine_int) - 128000;
        
        if (DEBUGGING)
            System.out.println ("var1 " + var1);
        
        var2 = var1 * var1 * (long) P6;
        
        if (DEBUGGING)
            System.out.println ("var2 " + var2);
        
        var2 = var2 + ((var1*(long) P5)<<17);
        
        if (DEBUGGING)
            System.out.println ("var2 " + var2);
        
        var2 = var2 + (((long) P4)<<35);
                
        if (DEBUGGING)
            System.out.println ("var2 " + var2);
     
        var1 = ((var1 * var1 * (long) P3)>>8) + ((var1 * (long) P2)<<12);
        
        if (DEBUGGING)
            System.out.println ("var1 " + var1);
        
        var1 = (((((long) 1)<<47)+var1))*((long) P1)>>33;
        
        if (DEBUGGING)
            System.out.println ("var1 " + var1);

        if (var1 == 0)
        {
            return 0; // avoid exception caused by division by zero
        }
        
        p = 1048576-adc;
        
        if (DEBUGGING)
            System.out.println ("p " + p);
        
        p = (((p<<31)-var2)*3125)/var1;
        
        if (DEBUGGING)
            System.out.println ("p " + p);
        
        var1 = (((long) P9) * (p>>13) * (p>>13)) >> 25;
        
        if (DEBUGGING)
            System.out.println ("var1 " + var1);
        
        var2 = (((long) P8) * p) >> 19;
        
        if (DEBUGGING)
            System.out.println ("var2 " + var2);
        
        p = ((p + var1 + var2) >> 8) + (((long) P7)<<4);
        
        if (DEBUGGING)
            System.out.println ("p " + p);
        
        return (int) ((p + 128) / 256);
    }
    
    /**
     * Calculate the compensated pressure using floating point
     * arithmetic.
     * 
     * @param adc The ADC result.
     * 
     * @return The pressure in Pascals.
     */
    private double compensatePressureFloat (int adc)
    {
         // This horror is from the data sheet. Don't blame me!
      
        double var1, var2, p;
        
        var1 = t_fine_double / 2.0 - 64000;
        
        if (DEBUGGING)
            System.out.println ("var1 " + var1);
        
        var2 = var1 * var1 * P6 / 32768;
        
        if (DEBUGGING)
            System.out.println ("var2 " + var2);
        
        var2 = var2 + var1 * P5 * 2;
        
        if (DEBUGGING)
            System.out.println ("var2 " + var2);
        
        var2 = var2 / 4 + P4 * 65536.0;
                
        if (DEBUGGING)
            System.out.println ("var2 " + var2);
        
        var1 = ((var1 * var1 * P3) / 524288.0 + (var1 * P2 )) / 524288.0;
        
        if (DEBUGGING)
            System.out.println ("var1 " + var1);
        
        var1 = (1 + var1 / 32768) * P1;
        
        if (DEBUGGING)
            System.out.println ("var1 " + var1);

        if (var1 == 0)
        {
            return 0; // avoid exception caused by division by zero
        }
        
        p = 1048576 - adc;
        
        if (DEBUGGING)
            System.out.println ("p " + p);
        
        p = (p - var2 / 4096) * 6250 / var1;
        
        if (DEBUGGING)
            System.out.println ("p " + p);
        
        var1 = P9 * p  * p / 2147483648.0;
        
        if (DEBUGGING)
            System.out.println ("var1 " + var1);
        
        var2 = P8 * p / 32768;
        
        if (DEBUGGING)
            System.out.println ("var2 " + var2);
        
        p = p + (var1 + var2 + P7) / 16.0;
        
        if (DEBUGGING)
            System.out.println ("p " + p);
        
        return p;
    }
    
    private static boolean within (double a, double b, double limit)
    {
        return Math.abs (a - b) < limit;
    }
    
    /** Locate of signature register. */
    private static final int SIGNATURE_REG = 0xd0;
    /** Signature value. */
    private static final int SIGNATURE = 0x58;
    /** Location of calibration data. */
    private static final int CALIBRATION_REG = 0x88;
    /** Location of status register. */
    private static final int STATUS_REG = 0xf3;
    /** Location of control register. */
    private static final int CONTROL_REG = 0xf4;
    /** Location of configuration register. */
    private static final int CONFIG_REG = 0xf5;
    /** Largest read data sized used (in fact calibration data). */
    private static final int BUFFER_SIZE = 24;
    /** Base of data. */
    private static final int RESULTS_REG = 0xf7;
    /** Size of data. */
    private static final int DATA_SIZE = 6;
    /** Run in debugging mode. */
    private static final boolean DEBUGGING = false;
    
    /** The I2C device. */
    private final I2CDevice device;
    /** Buffer used for reading results. */
    private final byte[] buffer = new byte[BUFFER_SIZE];
    
    // Compensation parameters.
    private final int T1;
    private final int T2;
    private final int T3;
    private final int P1;
    private final int P2;
    private final int P3;
    private final int P4;
    private final int P5;
    private final int P6;
    private final int P7;
    private final int P8;
    private final int P9;
    
    int t_fine_int;
    double t_fine_double;
}
