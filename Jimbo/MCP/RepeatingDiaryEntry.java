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

/**
 * Keep track of what we need to do and when. In this case we allow the entry
 * to be repeating.
 * 
 * @author Jim Darby
 */
public class RepeatingDiaryEntry extends DiaryEntry
{
    /**
     * Create a RepeatingDiaryEntry with an Executor environment.Its first run 
     * is at the next multiple of the increment.
     * 
     * @param task The task to run.
     * @param increment The time increment (or 0);
     * @param unit The time unit (or null).
     */
    public RepeatingDiaryEntry (Task task, long increment, ChronoField unit)
    {
        // Schedule the first run
        super (Controller.next (increment, unit), task);
        
        // Validater repetition parameters
        if (increment <= 0 || unit == null)
            throw new IllegalArgumentException (RepeatingDiaryEntry.class.getName () + ": invalid increment or unit");
        
        this.increment = increment;
        this.unit = unit;
    }
    
    /**
     * Perform a task.Wrap it in any provided executor.
     * 
     * @param now The time it's performed at
     * 
     * @return A new DiaryEntry if the task is repeating.
     */
    @Override
    DiaryEntry perform (Instant now)
    {   
        super.perform (now);
        
        if (task.closed ())
            closed = true;
        
        return closed ? null : new RepeatingDiaryEntry (task, increment, unit);
    }
    
    /** Repeat increment. */
    private final long increment;
    /** Repeat unit. */
    private final ChronoField unit;
}
