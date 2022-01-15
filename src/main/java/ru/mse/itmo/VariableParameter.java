package ru.mse.itmo;

import ru.mse.itmo.enums.VariableParameterEnum;

public class VariableParameter {
    public final VariableParameterEnum kind;
    public final int leftBound;
    public final int rightBound;
    public final int step;

    public VariableParameter(VariableParameterEnum kind, int leftBound, int rightBound, int step) {
        this.kind = kind;
        this.leftBound = leftBound;
        this.rightBound = rightBound;
        this.step = step;
    }
}
