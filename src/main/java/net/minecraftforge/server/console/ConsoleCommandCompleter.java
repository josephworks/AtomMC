/*
 * Minecraft Forge
 * Copyright (c) 2016.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.minecraftforge.server.console;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.server.dedicated.DedicatedServer;
import org.bukkit.event.server.TabCompleteEvent;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

final class ConsoleCommandCompleter implements Completer {

    private static final Logger logger = LogManager.getLogger();
    private final DedicatedServer server;

    public ConsoleCommandCompleter(DedicatedServer server) {
        this.server = checkNotNull(server, "server");
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        String buffer = line.line();
        boolean prefix;
        if (buffer.isEmpty() || buffer.charAt(0) != '/') {
            buffer = '/' + buffer;
            prefix = false;
        } else {
            prefix = true;
        }

        final String input = buffer;
        final String unmodifiedBuffer = line.line();
        Future<List<String>> tabComplete = this.server.callFromMainThread(() -> {

            List<String> offers = new ArrayList<>(this.server.getTabCompletions(this.server, input, this.server.getPosition(), false));
            Optional.ofNullable(server.server.getCommandMap().tabComplete(server.server.getConsoleSender(), unmodifiedBuffer))
                    .ifPresent(offers::addAll);

            TabCompleteEvent tabEvent = new TabCompleteEvent(server.server.getConsoleSender(), unmodifiedBuffer, offers);
            server.server.getPluginManager().callEvent(tabEvent);

            return tabEvent.isCancelled() ? Collections.emptyList() : tabEvent.getCompletions();
        });

        try {
            for (String completion : tabComplete.get()) {
                if (!completion.isEmpty()) {
                    boolean hasPrefix = completion.charAt(0) != '/' || prefix;
                    Candidate candidate = new Candidate(hasPrefix ? completion : completion.substring(1));
                    candidates.add(candidate);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            logger.error("Failed to tab complete", e);
        }
    }

}
