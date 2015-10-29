package org.sonar.java.se.dhb;

import org.sonar.java.cfg.CFG;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.MethodTree;

public class CompiledMethod {

  private final MethodTree syntaxTree;
  private final ConstraintsSet constraints;

  public CompiledMethod(ConstraintsSet classConstraints, MethodTree syntaxTree) {
    constraints = new ConstraintsSet(classConstraints);
    this.syntaxTree = syntaxTree;
  }

  public ConstraintsSet constraints() {
    return constraints;
  }

  public void execute(JavaFileScannerContext context, int maximumComplexity) {
    if (syntaxTree.block() != null) {
      SymbolicExecutor executor = new SymbolicExecutor(context, maximumComplexity, constraints());
      final CFG cfg = CFG.build(syntaxTree);
      executor.execute(cfg);
    }
  }
}
