package org.sonar.java.se.dhb;

public class CharacterConstant extends ConstantSymbolicValue {

  private final char value;

  public CharacterConstant(String string) {
    value = "'\\0'".equals(string) ? 0 : string.charAt(1);
  }

  @Override
  public boolean implies(SymbolicValue aValue) {
    if (aValue instanceof CharacterConstant) {
      return value == ((CharacterConstant) aValue).value;
    }
    return false;
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();
    buffer.append('\'');
    buffer.append(value);
    buffer.append('\'');
    return buffer.toString();
  }
}
