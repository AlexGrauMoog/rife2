/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.dam;

import rife.cmf.MimeType;
import rife.validation.ConstrainedProperty;
import rife.validation.Validation;

public class ContentImageRepository extends Validation {
    private int id_ = -1;
    private String name_ = null;
    private byte[] image_ = null;

    protected void activateValidation() {
        addConstraint(new ConstrainedProperty("name")
            .maxLength(64)
            .notNull(true)
            .notEmpty(true));
        addConstraint(new ConstrainedProperty("image")
            .notNull(true)
            .mimeType(MimeType.IMAGE_PNG)
            .name("myimage.png")
            .repository("testrep"));
    }

    public void setId(int id) {
        id_ = id;
    }

    public int getId() {
        return id_;
    }

    public void setName(String name) {
        name_ = name;
    }

    public String getName() {
        return name_;
    }

    public ContentImageRepository name(String name) {
        name_ = name;
        return this;
    }

    public byte[] getImage() {
        return image_;
    }

    public void setImage(byte[] image) {
        image_ = image;
    }

    public ContentImageRepository image(byte[] image) {
        image_ = image;
        return this;
    }
}
