/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam.contentstores.imagestoredrivers;

import rife.database.Datasource;

import java.sql.Types;

public class org_postgresql_Driver extends generic {
    public org_postgresql_Driver(Datasource datasource) {
        super(datasource);
    }

    protected int getNullSqlType() {
        return Types.LONGVARBINARY;
    }
}
