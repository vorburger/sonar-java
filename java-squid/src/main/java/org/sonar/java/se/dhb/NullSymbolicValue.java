package org.sonar.java.se.dhb;

public class NullSymbolicValue extends ObjectSymbolicValue {

  @Override
  public SymbolicValue nonEqualValue() {
    return new NonNullSymbolicValue();
  }

  @Override
  public boolean implies(SymbolicValue aValue) {
    return (aValue instanceof NullSymbolicValue) || (aValue instanceof AnySymbolicValue);
  }

  @Override
  public boolean maybeNull() {
    return true;
  }

  @Override
  public String toString() {
    return "null";
  }
}
