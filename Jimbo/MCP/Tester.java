/*
 * Copyright (C) 2019 Jim Darby.
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

package Jimbo.MCP;

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.logging.Logger;
import java.util.logging.Level;
/**
 * Testing class.
 * 
 * @author Jim darby.
 */
public class Tester
{
    public static void main (String args[])
    {
        // Set up simpler logging to stdout
        Jimbo.Logging.Logging.useStdout ();
        
        LOG.info ("Tester started");
            
        control = new Controller ();
        exec = new Exec ();
        
        Thread t = new Thread (control);
        t.start ();
        
        final Instant now = Instant.now ();
        
        control.put (new DiaryEntry (now.plusMillis (5000), new Work (5000, exec)));
        control.put (new DiaryEntry (new Work (0, exec)));
        control.put (new DiaryEntry (now.plusMillis (2000), new Work (2000, exec)));
        control.put (new DiaryEntry (now.plusMillis (3000), new Work (3000, exec)));
        control.put (new DiaryEntry (now.plusMillis (1000), new Work (1000, exec)));
        control.put (new DiaryEntry (now.plusMillis (4000), new Work (4000, exec)));
        
        Work w = new Work (10000, exec);
        DiaryEntry d = new DiaryEntry (now.plusMillis (10000), w);
        control.put (d);
        
        w.close ();
        
        w = new Work (11000, exec);
        d = new DiaryEntry (now.plusMillis (11000), w);
        control.put (d);
        d.close ();
        
        w = new Work (12000, exec);
        d = new DiaryEntry (now.plusMillis (12000), w);
        control.put (d);
        w.close ();
        d.close ();
        
        
        RepeatingDiaryEntry r = new RepeatingDiaryEntry (new Work (15), 15, ChronoField.INSTANT_SECONDS);
        control.put (r);
        
        w = new Work (16);
        r = new RepeatingDiaryEntry (w, 15, ChronoField.INSTANT_SECONDS);
        control.put (r);
        w.close ();
        
        w = new Work (17);
        r = new RepeatingDiaryEntry (w, 15, ChronoField.INSTANT_SECONDS);
        control.put (r);
        r.close ();
        
        w = new Work (18);
        r = new RepeatingDiaryEntry (w, 15, ChronoField.INSTANT_SECONDS);
        control.put (r);
        r.close ();
        w.close ();
                
        w = new Work (19);
        r = new RepeatingDiaryEntry (w, 15, ChronoField.INSTANT_SECONDS);
        control.put (r);
        w.close ();
        r.close ();   
    }
    
    private static Controller control;
    private static Exec exec;
    
    /** Where we log to. */
    private static final Logger LOG = Logger.getLogger (Tester.class.getName ());
    
    private static class Exec implements Executor
    {
        @Override
        public void perform (Task task, Instant now, Instant wanted)
        {
            task.perform (now, wanted);
        }
    }
    
    private static class Work implements Task
    {
        public Work (int value, Executor e)
        {
            this.value = value;
            this.closed = false;
            this.exec = e;

            LOG.log (Level.INFO, "Create task with value {0}", value);
        }
        
        public Work (int value)
        {
            this.value = value;
            this.closed = false;
            this .exec = null;
        }

        @Override
        public Executor getExecutor ()
        {
            return exec;
        }

        @Override
        public void perform (Instant now, Instant wanted)
        {
            LOG.log (Level.INFO, "Perform work {0} scheduled for {1} at {2}", new Object[] {value, wanted, now});
            
            final long mod = value % 100;
            
            if (mod == 1)
                close ();
            else if (mod > 1)
                value  = (value / 100) * 100 + (mod - 1);
        }

        @Override
        public void close ()
        {
            LOG.log (Level.INFO, "Closed task with value {0}", value);
            closed = true;
        }

        @Override
        public boolean closed ()
        {
            return closed;
        }

        long value;
        boolean closed;
        final Executor exec;
    }
}
