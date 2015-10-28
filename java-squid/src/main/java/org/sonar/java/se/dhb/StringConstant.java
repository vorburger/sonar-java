package org.sonar.java.se.dhb;

public class StringConstant extends ConstantSymbolicValue {

  private final String string;

  public StringConstant(String string) {
    this.string = string;
  }

  @Override
  public boolean implies(SymbolicValue aValue) {
    if (aValue instanceof StringConstant) {
      return string.equals(((StringConstant) aValue).string);
    }
    return aValue instanceof NonNullSymbolicValue;
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();
    buffer.append('"');
    buffer.append(string);
    buffer.append('"');
    return buffer.toString();
  }
}
