package org.sonar.java.se.dhb;

import org.sonar.plugins.java.api.tree.Tree;

@SuppressWarnings("serial")
public class SymbolicNullPointerException extends AbstractSymbolicException {

  public SymbolicNullPointerException(Tree target, String message) {
    super(target, message);
  }

}
