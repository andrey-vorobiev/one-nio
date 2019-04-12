/*
 * Copyright 2015 Odnoklassniki Ltd, Mail.Ru Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package one.nio.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.management.JMException;
import javax.management.ObjectName;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicLong;

public class ThreadDumper {
    private static final Log log = LogFactory.getLog(ThreadDumper.class);
    private static final AtomicLong dumpTime = new AtomicLong();

    public static void dump(OutputStream out) {
        Object threadDump;
        try {
            threadDump = ManagementFactory.getPlatformMBeanServer().invoke(
                    new ObjectName("com.sun.management:type=DiagnosticCommand"),
                    "threadPrint",
                    new Object[]{null},
                    new String[]{"[Ljava.lang.String;"}
            );
        } catch (JMException e) {
            log.warn("Failed to get threads dump: " + e);
            return;
        }

        PrintStream printStream = out == null ? System.out : new PrintStream(out);
        printStream.println(threadDump);
    }

    public static void dump(OutputStream out, long minDumpInterval) {
        long currentTime = System.currentTimeMillis();
        long lastDumpTime = dumpTime.get();
        if (currentTime - lastDumpTime >= minDumpInterval && dumpTime.compareAndSet(lastDumpTime, currentTime)) {
            dump(out);
        }
    }
}
