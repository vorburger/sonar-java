package org.sonar.java.se.dhb;

import org.sonar.java.model.JavaTree;
import org.sonar.plugins.java.api.JavaCheck;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.Tree;

@SuppressWarnings("serial")
public abstract class AbstractSymbolicException extends Exception {

  private final int line;
  private final String message;

  protected AbstractSymbolicException(Tree syntaxNode, String message) {
    line = ((JavaTree) syntaxNode).getLine();
    this.message = message;
  }

  public void report(JavaFileScannerContext context, JavaCheck javaCheck) {
    context.addIssue(line, javaCheck, message);
  }
}
