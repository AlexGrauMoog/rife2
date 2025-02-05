/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.authentication.credentialsmanagers.exceptions;

import rife.authentication.exceptions.CredentialsManagerException;

import java.io.Serial;

public class DuplicateUserIdException extends CredentialsManagerException {
    @Serial private static final long serialVersionUID = 1939667170190873742L;

    private final long userId_;

    public DuplicateUserIdException(long userId) {
        super("The user id '" + userId + "' is already present.");

        userId_ = userId;
    }

    public long getUserId() {
        return userId_;
    }
}
