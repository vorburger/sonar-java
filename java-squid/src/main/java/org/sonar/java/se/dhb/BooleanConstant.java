package org.sonar.java.se.dhb;

public class BooleanConstant extends SymbolicValue {

  public static final SymbolicValue TRUE = new BooleanConstant(true);
  public static final SymbolicValue FALSE = new BooleanConstant(false);

  private final boolean value;

  private BooleanConstant(boolean value) {
    this.value = value;
  }

  @Override
  public SymbolicValue nonEqualValue() {
    return value ? FALSE : TRUE;
  }

  @Override
  public boolean implies(SymbolicValue aValue) {
    if (aValue instanceof BooleanConstant) {
      return value == ((BooleanConstant) aValue).value;
    }
    return false;
  }

  @Override
  public boolean maybeNull() {
    return false;
  }

  @Override
  public String toString() {
    return value ? "true" : "false";
  }
}
