/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers.databasedrivers;

import rife.authentication.credentialsmanagers.DatabaseUsers;
import rife.authentication.credentialsmanagers.RoleUserAttributes;
import rife.authentication.credentialsmanagers.exceptions.DuplicateLoginException;
import rife.authentication.credentialsmanagers.exceptions.DuplicateUserIdException;
import rife.authentication.exceptions.CredentialsManagerException;
import rife.database.Datasource;

public class org_hsqldb_jdbcDriver extends generic {
    public org_hsqldb_jdbcDriver(Datasource datasource) {
        super(datasource);
    }

    public DatabaseUsers addUser(String login, RoleUserAttributes attributes)
    throws CredentialsManagerException {
        try {
            _addUser(addUserWithId_, getFreeUserId_, getRoleId_, addRoleLink_, login, attributes);
        } catch (CredentialsManagerException e) {
            if (null != e.getCause() &&
                null != e.getCause().getCause()) {
                String message = e.getCause().getCause().getMessage().toUpperCase();
                if (message.contains(createTableUser_.getUniqueConstraints().get(0).getName())) {
                    throw new DuplicateLoginException(login);
                }
                if (message.contains("UNIQUE")) {
                    throw new DuplicateUserIdException(attributes.getUserId());
                }
            }

            throw e;
        }

        return this;
    }
}
