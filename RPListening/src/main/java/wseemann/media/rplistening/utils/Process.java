/*
 * RPListening: An Open Source desktop client for Roku private listening.
 *
 * Copyright (C) 2021 William Seemann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package wseemann.media.rplistening.utils;

import java.io.IOException;

public class Process {

    private final java.lang.Process process;

    public Process(java.lang.Process process) {
        this.process = process;
    }

    public void destroy() {
        if (process != null) {
            try {
                process.destroy();
                process.getInputStream().close();
                process.getOutputStream().close();
                process.getErrorStream().close();
                Runtime.getRuntime().exec("pkill ffplay");
            } catch (IOException ex) {
            }
        }
    }
}
