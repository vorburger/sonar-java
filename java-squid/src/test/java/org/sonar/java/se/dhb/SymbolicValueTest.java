package org.sonar.java.se.dhb;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class SymbolicValueTest {

  @Test
  public void testImplies() {
    SymbolicValue anyValue1 = new AnySymbolicValue();
    SymbolicValue anyValue2 = new AnySymbolicValue();
    SymbolicValue nullValue = SymbolicValue.NULL;
    SymbolicValue nonNullValue1 = new NonNullSymbolicValue();
    SymbolicValue nonNullValue2 = new NonNullSymbolicValue();
    SymbolicValue trueValue = BooleanConstant.TRUE;
    SymbolicValue falseValue = BooleanConstant.FALSE;
    StringConstant hello = new StringConstant("Hello!");
    StringConstant helloWorld = new StringConstant("Hello world!");

    assertThat(anyValue1.implies(anyValue1)).as("Any value implies itself").isTrue();
    assertThat(anyValue1.implies(anyValue2)).as("Any value does not imply another value").isFalse();
    assertThat(anyValue1.implies(nullValue)).as("Any value does not imply a null value").isFalse();
    assertThat(anyValue1.implies(nonNullValue1)).as("Any value does not imply a non null value").isFalse();
    assertThat(anyValue1.implies(trueValue)).as("Any value does not imply a true value").isFalse();
    assertThat(anyValue1.implies(falseValue)).as("Any value does not imply a false value").isFalse();
    assertThat(anyValue1.implies(hello)).as("Any value does not imply a string value").isFalse();

    assertThat(nullValue.implies(nullValue)).as("A null value implies itself").isTrue();
    assertThat(nullValue.implies(anyValue1)).as("A null value implies any value").isTrue();
    assertThat(nullValue.implies(nonNullValue1)).as("A null value does not imply a non null value").isFalse();
    assertThat(nullValue.implies(trueValue)).as("A null value does not imply a true value").isFalse();
    assertThat(nullValue.implies(falseValue)).as("A null value does not imply a false value").isFalse();
    assertThat(nullValue.implies(hello)).as("A null value does not imply a string value").isFalse();

    assertThat(nonNullValue1.implies(nonNullValue1)).as("A non null value implies itself").isTrue();
    assertThat(nonNullValue1.implies(nonNullValue2)).as("A non null value implies another").isTrue();
    assertThat(nonNullValue1.implies(nullValue)).as("A non-null value does not imply a null value").isFalse();
    assertThat(nonNullValue1.implies(anyValue1)).as("A non-null value does not imply any other").isFalse();
    assertThat(nonNullValue1.implies(hello)).as("A non-null value does not imply a string value").isFalse();

    assertThat(trueValue.implies(trueValue)).as("A true value implies itself").isTrue();
    assertThat(trueValue.implies(falseValue)).as("A true value does not imply a false value").isFalse();

    assertThat(falseValue.implies(falseValue)).as("A false value implies itself").isTrue();
    assertThat(falseValue.implies(trueValue)).as("A false value does not imply a true value").isFalse();

    assertThat(hello.implies(hello)).as("A string value implies itself").isTrue();
    assertThat(hello.implies(nonNullValue1)).as("A string value implies a non null value").isTrue();
    assertThat(hello.implies(helloWorld)).as("A string value does not imply another string value").isFalse();
    assertThat(hello.implies(nullValue)).as("A string value does not imply a null value").isFalse();
  }
}
