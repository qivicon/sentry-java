package net.kencochrane.raven.connection;

import net.kencochrane.raven.event.Event;

import java.io.IOException;

/**
 * Connection that drops events.
 *
 * Only use it when no DSN is set.
 */
public class NoopConnection extends AbstractConnection {
    public NoopConnection() {
        super(null, null);
    }

    @Override
    protected void doSend(Event event) throws ConnectionException {
    }

    @Override
    public void close() throws IOException {
    }
}