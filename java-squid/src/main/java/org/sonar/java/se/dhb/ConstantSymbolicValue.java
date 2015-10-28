package org.sonar.java.se.dhb;

public abstract class ConstantSymbolicValue extends SymbolicValue {

  @Override
  public boolean maybeNull() {
    return false;
  }

  @Override
  public SymbolicValue nonEqualValue() {
    return new DifferentSymbolicValue(this);
  }
}
