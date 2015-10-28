package org.sonar.java.se.dhb;

public class NonNullSymbolicValue extends ObjectSymbolicValue {

  @Override
  public SymbolicValue nonEqualValue() {
    return new AnySymbolicValue();
  }

  @Override
  public boolean implies(SymbolicValue aValue) {
    return aValue instanceof NonNullSymbolicValue;
  }

  @Override
  public boolean maybeNull() {
    return false;
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();
    buffer.append("not null");
    buffer.append(" (");
    buffer.append(this.hashCode());
    buffer.append(')');
    return buffer.toString();
  }
}
