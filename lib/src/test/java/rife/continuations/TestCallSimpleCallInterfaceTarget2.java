/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.continuations;

public class TestCallSimpleCallInterfaceTarget2 implements CloneableContinuable, ContinuableSupportAware {
    private ContinuableSupport support_;

    public void setContinuableSupport(ContinuableSupport support) {
        support_ = support;
    }

    public Object clone()
    throws CloneNotSupportedException {
        return super.clone();
    }

    public void execute() {
        var answer = "during call target 2\n";
        support_.answer(answer);
    }
}