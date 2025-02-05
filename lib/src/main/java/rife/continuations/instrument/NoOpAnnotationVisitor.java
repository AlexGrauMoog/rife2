/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations.instrument;

import rife.asm.AnnotationVisitor;

import static rife.asm.Opcodes.ASM9;

class NoOpAnnotationVisitor extends AnnotationVisitor {
    protected NoOpAnnotationVisitor() {
        super(ASM9);
    }

    public void visit(String name, Object value) {
    }

    public void visitEnum(String name, String desc, String value) {
    }

    public AnnotationVisitor visitAnnotation(String name, String desc) {
        return this;
    }

    public AnnotationVisitor visitArray(String name) {
        return this;
    }

    public void visitEnd() {
    }
}
