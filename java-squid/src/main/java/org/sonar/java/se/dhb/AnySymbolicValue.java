package org.sonar.java.se.dhb;

public class AnySymbolicValue extends ObjectSymbolicValue {

  @Override
  public SymbolicValue nonEqualValue() {
    return new AnySymbolicValue();
  }

  @Override
  public boolean implies(SymbolicValue aValue) {
    return this == aValue;
  }

  @Override
  public boolean maybeNull() {
    return true;
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();
    buffer.append("any");
    buffer.append(" (");
    buffer.append(this.hashCode());
    buffer.append(')');
    return buffer.toString();
  }

}
