package org.sonar.java.se.dhb;

import org.sonar.plugins.java.api.tree.Tree;

@SuppressWarnings("serial")
public class IncompatibleConstraintsException extends AbstractSymbolicException {

  public IncompatibleConstraintsException(Tree syntaxNode, String message) {
    super(syntaxNode, message);
  }
}
