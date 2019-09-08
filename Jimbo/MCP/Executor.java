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
 * This interface describes a task execution environment. It is implemented to
 * provide any context for this task.
 * 
 * @author Jim Darby.
 */

public interface Executor
{
    /**
     * Perform the task.
     * 
     * @param task The task to perform
     * @param now The time it's being executed.
     * @param wanted When it was supposed to be called
     */
    abstract public void perform (Task task, Instant now, Instant wanted);
}
