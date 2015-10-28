package org.sonar.java.se.dhb;

import org.sonar.plugins.java.api.tree.Tree;

@SuppressWarnings("serial")
public class ComplexityExceededException extends AbstractSymbolicException {

  protected ComplexityExceededException(Tree syntaxNode, String message) {
    super(syntaxNode, message);
  }

}
