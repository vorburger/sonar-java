package org.sonar.java.se.dhb;

public class NumberConstant extends ConstantSymbolicValue {

  private final Double value;
  private Double upperLimit;
  private Double lowerLimit;

  public NumberConstant(String string) {
    value = Double.valueOf(string);
  }

  public double value() {
    return value;
  }

  @Override
  public boolean implies(SymbolicValue aValue) {
    if (aValue instanceof NumberConstant) {
      if (upperLimit != null) {
        Double otherUpperLimit = ((NumberConstant) aValue).upperLimit;
        if (otherUpperLimit != null && upperLimit.compareTo(otherUpperLimit) > 0) {
          return false;
        }
      }
      if (lowerLimit != null) {
        Double otherLowerLimit = ((NumberConstant) aValue).lowerLimit;
        if (otherLowerLimit != null && upperLimit.compareTo(otherLowerLimit) < 0) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();
    if (lowerLimit != null) {
      buffer.append('{');
      buffer.append(lowerLimit);
      buffer.append(" <} ");
    }
    if (value == null) {
      buffer.append('x');
    } else {
      buffer.append(value);
    }
    if (upperLimit != null) {
      buffer.append("{< ");
      buffer.append(upperLimit);
      buffer.append('}');
    }
    return buffer.toString();
  }
}
