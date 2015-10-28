package org.sonar.java.se.dhb;

public class DifferentSymbolicValue extends SymbolicValue {

  private final SymbolicValue value;

  public DifferentSymbolicValue(SymbolicValue value) {
    this.value = value;
  }

  @Override
  public SymbolicValue nonEqualValue() {
    return new DifferentSymbolicValue(this);
  }

  @Override
  public boolean implies(SymbolicValue aValue) {
    if (aValue instanceof DifferentSymbolicValue) {
      return this.equals(((DifferentSymbolicValue) aValue).value);
    }
    return false;
  }

  @Override
  public boolean maybeNull() {
    return false;
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder("not equal to ");
    buffer.append(value);
    return buffer.toString();
  }
}
