/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.ioc.HierarchicalProperties;

public class TestServerRunner implements AutoCloseable {
    final Server server_ = new Server().port(8181);

    public TestServerRunner(Site site) {
        server_.start(site);
    }

    public HierarchicalProperties properties() {
        return server_.properties();
    }

    public void close() {
        server_.stop();
    }
}
