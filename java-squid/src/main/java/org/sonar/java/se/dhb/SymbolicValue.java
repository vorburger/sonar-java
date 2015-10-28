package org.sonar.java.se.dhb;

public abstract class SymbolicValue {

  public static final SymbolicValue NULL = new NullSymbolicValue();

  public abstract SymbolicValue nonEqualValue();

  public abstract boolean implies(SymbolicValue aValue);

  public abstract boolean maybeNull();
}
