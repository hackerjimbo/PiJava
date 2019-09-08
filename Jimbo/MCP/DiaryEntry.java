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

/**
 * Keep track of what we need to do and when. Tasks have this as an entry in the
 * controller class.
 * 
 * @author Jim Darby
 */
public class DiaryEntry implements Comparable <DiaryEntry>, AutoCloseable
{   
    /**
     * Create a DiaryEntry with a time to run.
     * 
     * @param notB4 Don't run before this.
     * @param task The task to run.
     */
    public DiaryEntry (Instant notB4, Task task)
    {
        this.notB4 = notB4;
        this.task = task;
        closed = false;
    }
    
    /**
     * Create a DiaryEntry to run immediately.
     * 
     * @param task The task to run.
     */
    public DiaryEntry (Task task)
    {
        this (null, task);
    }
    
    /**
     * Perform a task.Wrap it in any provided executor.
     * 
     * @param now The time it's performed at
     * 
     * @return A new DiaryEntry if the task is repeating.
     */
    DiaryEntry perform (Instant now)
    {
        if (closed || task.closed ())
            return null;
        
        final Executor e = task.getExecutor ();
        
        // If there's an Executor then execute inside it, otherwise not.
        if (e != null)
            e.perform (task, now, notB4);
        else
            task.perform (now, notB4);
        
        return null;
    }
    
    /**
     * Provide time ordering of two Tasks.
     * 
     * @param t The task to compare to.
     * 
     * @return true if this task occurs before the other task.
     */
    @Override
    public int compareTo (DiaryEntry t)
    {
        if (notB4 == null)
            return (t.notB4 == null) ? 0 : -1;

        return (t.notB4 == null) ? 1 : notB4.compareTo (t.notB4);
    }

    /**
     * Does this entry occur before an Instant.
     * 
     * @param when The Instant to compare with.
     * 
     * @return true if this task is before that time.
     */
    public boolean before (Instant when)
    {
        return (notB4 == null) ? true : notB4.isBefore (when);
    }

    /**
     * Does this entry occur after an Instant.
     * 
     * @param when The Instant to compare with.
     * 
     * @return true if this task is after that time.
     */
    public boolean after (Instant when)
    {
        return (notB4 == null) ? false : notB4.isAfter (when);
    }

    /**
     * Return the scheduled time.
     * 
     * @return The time.
     */
    public Instant when ()
    {
        return notB4;
    }
    
    /**
     * Close down this entry. This calls the close entry in the Task if the Task
     * itself hasn't already closed itself.
     */
    @Override
    public void close ()
    {
       closed = true;
    }
    
    /** The scheduled Instant. */
    private final Instant notB4;
    /** The Task to run. */
    protected final Task task;
    /** Are we closed. */
    protected boolean closed;
}