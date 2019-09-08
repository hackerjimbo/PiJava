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
import java.time.Duration;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalUnit;

import java.util.PriorityQueue;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Controller for task scheduling. This class allows a "process" to have
 * messages queued to it. It processes those messages in the same
 * thread avoiding potential issues around concurrency (at the expense of
 * performance on multi-core systems).
 * 
 * The task implements the Runnable interface so we can create a Thread to run
 * it in.
 * 
 * @author Jim Darby
 */
public class Controller implements Runnable
{
    /**
     * Basic constructor. By default nothing happens here.
     */
    public Controller ()
    {
        inq = new ArrayBlockingQueue <> (100);
        workq = new PriorityQueue <> ();
    }
    
    /**
     * Task main loop. This never exits and is typically run as its own thread.
     */
    @Override
    public void run ()
    {
        LOG.info ("Running");
        
        // How long we wait for something new to crop up. In milliseconds.
        long waitfor = 0;
        
        while (true)
        {
            // Empty the delay queue, just to be sure.
            emptyQ ();
           
            // Empty the booking queue.
            // This may wait for a while if there's nothing to do (yet).
            while (true)
            {                
                try
                {
                    final DiaryEntry entry = inq.poll (waitfor, TimeUnit.MILLISECONDS);
                    
                    // Nothing found. We need to see if we have anything to do.
                    if (entry == null)
                        break;

                    final Instant now = Instant.now ();
                    
                    // Does it need to be done right now?
                    if (entry.before (now))
                        perform (entry, now);    // Do it now
                    else
                        workq.add (entry);       // Queue it
                }

                catch (InterruptedException e)
                {
                    LOG.log (Level.WARNING, "Interruped waiting for message: {0}", e.getMessage ());
                }
                
                // Empty the rest of the inq without delay.
                waitfor = 0;
            }
            
            // Empty the inq again in case some more stuff has cropped up.
            // This occurs more than you might think because the delay above may
            // overrun by a few milliseconds and things may have cropped up.
            emptyQ ();
            
            // If the work queue is empty then wait for ever, otherwise the
            // wait is until the next event. Note that this is the MAXIMUM
            // amount of time we're going to wait.
            waitfor = workq.isEmpty () ? Long.MAX_VALUE :
                    (workq.peek (). when ().toEpochMilli () - Instant.now ().toEpochMilli ());
            
            // Sanity check
            if (waitfor < 0)
            {
                LOG.log (Level.WARNING, "Negative time to wait for: {0}", waitfor);
                waitfor = 0;
            }
        }
    }

    /**
     * Add a task to the input queue.
     * 
     * @param entry The DiaryEntry to add.
     * @return If the item was sucessfully queued.
     */
    public boolean put (DiaryEntry entry)
    {
        final boolean result = inq.offer (entry);
        
        if (!result)
            LOG.severe ("Input queue to Controller full!");
        
        return result;
    }
    
    /**
     * Empty as much of the local work queue as we can.
     */
    private void emptyQ ()
    {
        while (!workq.isEmpty ())
        {
            final Instant now = Instant.now ();
            
            if (workq.peek ().after (now))
                break;
            
            final DiaryEntry entry = workq.poll ();
            final Instant due = entry.when();
            
            if (due != null)
            {
                final Duration late = Duration.between (due, now);
                final long millis = late.toMillis ();
            
                if (millis > 10)
                    LOG.log (Level.WARNING, "Late by {0}ms, due {1} now {2}", new Object[]{millis, due, now});
            }
            
            perform (entry, now);
        }
    }
    
    /**
     * Perform a task. If it's a repeating task then add another entry to the
     * diary.
     * 
     * @param t The task to perform. 
     */
    private void perform (DiaryEntry entry, Instant now)
    {
        final DiaryEntry next = entry.perform (now);
        
        if (next != null)
            put (next);
    }

    /**
     * Return the next Instant on the given timescale. 
     * 
     * @param now The time we're based on
     * @param units The number of units to advance
     * @param unit The units used
     * 
     * @return The next Instant on the timeline
     */
    public static Instant next (Instant now, long units, ChronoField unit)
    {
        //LOG.info ("We have " + now);
        //LOG.info ("Round to " + units + " " + unit);
        
        TemporalUnit tu = unit.getBaseUnit();
        //LOG.info ("Unit " + tu);
        
        final Instant rounded = now.truncatedTo (tu);
        //LOG.info ("rounded " + rounded);
        
        final long got = now.getLong (unit);
        //LOG.info ("We have " + got);
        
        final long have = got / units;
        //LOG.info ("Have " + have);
        
        final long want = (have + 1) * units;
        //LOG.info ("Want " + want);
        
        final Instant next = rounded.plus (want - got, tu);
        //LOG.info ("Result " + next);
        
        return next;
    }
    
    /**
     * Return the next time period based on the current time.
     * 
     * @param units The number of units to advance
     * @param unit The units used
     * 
     * @return The next Instant on the timeline
     */
    public static Instant next (long units, ChronoField unit)
    {
        return next (Instant.now (), units, unit);
    }
    
    /** Where we log to. */
    private static final Logger LOG = Logger.getLogger (Controller.class.getName ());
    
    /**
     * The input queue. This can be added to from any thread.
     */
    private final ArrayBlockingQueue <DiaryEntry> inq;
    
    /**
     * The work queue. This can only be added to from this thread.
     */
    private final PriorityQueue <DiaryEntry> workq;
}
