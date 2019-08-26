package io.sentry.context;

/**
 * {@link ContextManager} that maintains unique {@link Context} per sentry client
 */
public class DefaultContextManager implements ContextManager {
    
    private final Context context;
    
    public DefaultContextManager() {
        this.context = new Context();
    }

    /**
     * Returns the unique {@link Context} instance
     *
     * @return the associated {@link Context} instance.
     */
    public Context getContext() {
        return context;
    }

    @Override
    public void clear() {
        context.clear();
    }
}
