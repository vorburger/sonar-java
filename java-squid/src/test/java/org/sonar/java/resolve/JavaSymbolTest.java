/*
 * SonarQube Java
 * Copyright (C) 2012 SonarSource
 * sonarqube@googlegroups.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.java.resolve;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class JavaSymbolTest {
  private static final JavaSymbol.PackageJavaSymbol P_PACKAGE_JAVA_SYMBOL = new JavaSymbol.PackageJavaSymbol(null, null);

  @Test
  public void kinds() {
    assertThat(JavaSymbol.TYP).isLessThan(JavaSymbol.ERRONEOUS);
    assertThat(JavaSymbol.VAR).isLessThan(JavaSymbol.ERRONEOUS);
    assertThat(JavaSymbol.MTH).isLessThan(JavaSymbol.ERRONEOUS);
    assertThat(JavaSymbol.ERRONEOUS).isLessThan(JavaSymbol.ABSENT);
  }

  @Test
  public void completion_should_use_completer() {
    JavaSymbol symbol = new JavaSymbol(0, 0, null, null);
    JavaSymbol.Completer completer = mock(JavaSymbol.Completer.class);
    symbol.completer = completer;
    symbol.complete();
    verify(completer).complete(symbol);
    assertThat(symbol.completer).isNull();
  }

  @Test
  public void test_PackageSymbol() {
    JavaSymbol owner = mock(JavaSymbol.class);
    JavaSymbol.PackageJavaSymbol packageSymbol = new JavaSymbol.PackageJavaSymbol("name", owner);

    assertThat(packageSymbol.kind).isEqualTo(JavaSymbol.PCK);
    assertTrue(packageSymbol.isPackageSymbol());
    assertThat(packageSymbol.flags()).isEqualTo(0);
    assertThat(packageSymbol.owner()).isSameAs(owner);

    assertThat(packageSymbol.packge()).isSameAs(packageSymbol);
    assertThat(packageSymbol.outermostClass()).isNull();
    assertThat(packageSymbol.enclosingClass()).isNull();
  }

  @Test
  public void test_TypeSymbol() {
    JavaSymbol.TypeJavaSymbol outermostClass = new JavaSymbol.TypeJavaSymbol(42, "name", P_PACKAGE_JAVA_SYMBOL);
    JavaSymbol.TypeJavaSymbol typeSymbol = new JavaSymbol.TypeJavaSymbol(42, "name", outermostClass);

    assertThat(typeSymbol.kind).isEqualTo(JavaSymbol.TYP);
    assertTrue(typeSymbol.isTypeSymbol());
    assertThat(typeSymbol.flags()).isEqualTo(42);
    assertThat(typeSymbol.owner()).isSameAs(outermostClass);

    assertThat(typeSymbol.packge()).isSameAs(P_PACKAGE_JAVA_SYMBOL);
    assertThat(typeSymbol.outermostClass()).isSameAs(outermostClass);
    assertThat(typeSymbol.enclosingClass()).isSameAs(typeSymbol);
  }

  @Test
  public void access_to_superclass_should_trigger_completion() {
    JavaSymbol.TypeJavaSymbol typeSymbol = spy(new JavaSymbol.TypeJavaSymbol(42, "name", P_PACKAGE_JAVA_SYMBOL));
    typeSymbol.getSuperclass();
    verify(typeSymbol).complete();
  }

  @Test
  public void access_to_interfaces_should_trigger_completion() {
    JavaSymbol.TypeJavaSymbol typeSymbol = spy(new JavaSymbol.TypeJavaSymbol(42, "name", P_PACKAGE_JAVA_SYMBOL));
    typeSymbol.getInterfaces();
    verify(typeSymbol).complete();
  }

  @Test
  public void access_to_members_should_trigger_completion() {
    JavaSymbol.TypeJavaSymbol typeSymbol = spy(new JavaSymbol.TypeJavaSymbol(42, "name", P_PACKAGE_JAVA_SYMBOL));
    typeSymbol.members();
    verify(typeSymbol).complete();
  }

  @Test
  public void test_MethodSymbol() {
    JavaSymbol.TypeJavaSymbol outermostClass = new JavaSymbol.TypeJavaSymbol(42, "name", P_PACKAGE_JAVA_SYMBOL);
    JavaSymbol.TypeJavaSymbol typeSymbol = new JavaSymbol.TypeJavaSymbol(42, "t", outermostClass);
    JavaSymbol.MethodJavaSymbol methodSymbol = new JavaSymbol.MethodJavaSymbol(42, "name", typeSymbol);

    assertThat(methodSymbol.kind).isEqualTo(JavaSymbol.MTH);
    assertTrue(methodSymbol.isMethodSymbol());
    assertThat(methodSymbol.flags()).isEqualTo(42);
    assertThat(methodSymbol.owner()).isSameAs(typeSymbol);

    assertThat(methodSymbol.packge()).isSameAs(P_PACKAGE_JAVA_SYMBOL);
    assertThat(methodSymbol.outermostClass()).isSameAs(outermostClass);
    assertThat(methodSymbol.enclosingClass()).isSameAs(typeSymbol);
  }

  @Test
  public void test_VariableSymbol() {
    JavaSymbol.TypeJavaSymbol outermostClass = new JavaSymbol.TypeJavaSymbol(42, "name", P_PACKAGE_JAVA_SYMBOL);
    JavaSymbol.TypeJavaSymbol typeSymbol = new JavaSymbol.TypeJavaSymbol(42, "t", outermostClass);
    JavaSymbol.MethodJavaSymbol methodSymbol = new JavaSymbol.MethodJavaSymbol(42, "name", typeSymbol);
    JavaSymbol.VariableJavaSymbol variableSymbol = new JavaSymbol.VariableJavaSymbol(42, "name", methodSymbol);

    assertThat(variableSymbol.kind).isEqualTo(JavaSymbol.VAR);
    assertTrue(variableSymbol.isVariableSymbol());
    assertThat(variableSymbol.flags()).isEqualTo(42);
    assertThat(variableSymbol.owner()).isSameAs(methodSymbol);

    assertThat(variableSymbol.packge()).isSameAs(P_PACKAGE_JAVA_SYMBOL);
    assertThat(variableSymbol.outermostClass()).isSameAs(outermostClass);
    assertThat(variableSymbol.enclosingClass()).isSameAs(typeSymbol);
  }

  @Test
  public void test_helper_methods() throws Exception {
    JavaSymbol.TypeJavaSymbol outermostClass = new JavaSymbol.TypeJavaSymbol(Flags.INTERFACE, "name", P_PACKAGE_JAVA_SYMBOL);
    JavaSymbol.TypeJavaSymbol typeSymbol = new JavaSymbol.TypeJavaSymbol(Flags.INTERFACE, "t", outermostClass);
    JavaSymbol.MethodJavaSymbol methodSymbol = new JavaSymbol.MethodJavaSymbol(Flags.STATIC | Flags.ABSTRACT, "name", typeSymbol);
    JavaSymbol.TypeJavaSymbol enumeration = new JavaSymbol.TypeJavaSymbol(Flags.ENUM, "enumeration", P_PACKAGE_JAVA_SYMBOL);
    FakeUnknownSymbol fakeUnknownSymbol = new FakeUnknownSymbol(Flags.PRIVATE, "other", typeSymbol);

    assertThat(methodSymbol.isEnum()).isFalse();
    assertThat(methodSymbol.isFinal()).isFalse();
    assertThat(methodSymbol.isAbstract()).isTrue();
    assertThat(methodSymbol.isStatic()).isTrue();
    assertThat(methodSymbol.isPackageVisibility()).isTrue();
    assertThat(methodSymbol.isVolatile()).isFalse();
    assertThat(methodSymbol.isProtected()).isFalse();

    assertThat(methodSymbol.hasSameVisibility(new JavaSymbol.TypeJavaSymbol(0, "other", typeSymbol))).isTrue();
    assertThat(methodSymbol.hasSameVisibility(new JavaSymbol.TypeJavaSymbol(Flags.PRIVATE, "other", typeSymbol))).isFalse();
    assertThat(methodSymbol.hasSameVisibility(fakeUnknownSymbol)).isFalse();
    assertThat(fakeUnknownSymbol.hasSameVisibility(methodSymbol)).isFalse();

    assertThat(enumeration.isEnum()).isTrue();
    assertThat(enumeration.isAbstract()).isFalse();
    assertThat(enumeration.isStatic()).isFalse();
    assertThat(P_PACKAGE_JAVA_SYMBOL.isPackageSymbol()).isTrue();
    assertThat(outermostClass.isPackageSymbol()).isFalse();
  }

  private static class FakeUnknownSymbol extends JavaSymbol.TypeJavaSymbol {

    public FakeUnknownSymbol(int flags, String name, JavaSymbol owner) {
      super(flags, name, owner);
    }

    @Override
    public boolean isUnknown() {
      return true;
    }

  }
}
