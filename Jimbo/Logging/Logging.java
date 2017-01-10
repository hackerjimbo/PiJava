/*
 * Copyright (C) 2016 Jim Darby.
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

package Jimbo.Logging;

import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import java.text.MessageFormat;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;

/**
 * Some useful stuff to customise logging for us.
 * 
 * @author Jim Darby
 */
public class Logging
{
    public static void useStdout ()
    {
        // Find the root logger
        final Logger root_logger = Logger.getLogger ("");
        
        // Now kill off all the other handlers.
        
        Handler[] handlers = root_logger.getHandlers();
        
        for (Handler handler : handlers)
            root_logger.removeHandler (handler);
        
        // And insert ours.
        root_logger.addHandler (new Reporter ());
    }
    
    /**
     * Our own logging record handler class. Essentially just timestamps
     * the record and then sends it to stdout.
     */
    private static class Reporter extends Handler
    {
        /**
         * Actually process a log record.
         * 
         * @param r The log record to process.
         */
        @Override
        public void publish(LogRecord r)
        {
            // Extract raw message from the record
            String text = r.getMessage ();
        
            // The message can be null, if so ignore it.
            if (text == null)
                return;
            
            final Object[] parameters = r.getParameters();
                    
            // Do we need to format the message? Thanks to Brenton for finding this
            // algorithm. For full details see:
            // https://docs.oracle.com/javase/8/docs/api/java/util/logging/Formatter.html#formatMessage-java.util.logging.LogRecord-
            if (parameters != null && parameters.length != 0 && text.contains ("{0"))
                text = new MessageFormat (text).format (parameters);
            
            final int seconds = (int) ((r.getMillis () + 500) / 1000);
            final ZonedDateTime zoned = ZonedDateTime.of (LocalDateTime.ofEpochSecond (seconds, 0, ZoneOffset.UTC), ZoneOffset.UTC) ;
            final String when = zoned.format (DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            
            System.out.println ("[" + when + "] " + text);
            
            /*System.out.println ("Logging record:");
            System.out.println ("       Time: " + when);
            System.out.println ("      Level: " + r.getLevel());
            System.out.println ("       Name: " + r.getLoggerName ());
            System.out.println ("  Formatted: " + text);
            System.out.println ();*/
        }

        @Override
        public void flush()
        {
        }

        @Override
        public void close() throws SecurityException
        {
        }
    }
}
