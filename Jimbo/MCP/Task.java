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
 * What a task needs to provide to be put in a controller queue.
 * 
 * @author Jim Darby
 */
public interface Task extends AutoCloseable
{
    /**
     * Return the Executor for this task. It may be null.
     * 
     * @return The Executor.
     */
    default public Executor getExecutor ()
    {
        return null;
    }
    
    /**
     * Perform the task.
     * 
     * @param now When it's called
     * @param wanted When it was supposed to be called
     */
    abstract public void perform (Instant now, Instant wanted);
    
    /**
     * Close this task, meaning it's marked as not to be performed.
     */
    @Override
    abstract public void close ();
    
    /**
     * Check if a task is closed.
     * 
     * @return If it is.
     */
    abstract boolean closed ();
}
