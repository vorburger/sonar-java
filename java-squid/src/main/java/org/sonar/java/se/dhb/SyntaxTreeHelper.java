package org.sonar.java.se.dhb;

import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.CompilationUnitTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Tree;

import java.util.HashMap;
import java.util.Map;

public class SyntaxTreeHelper {

  private final Map<String, MethodTree> methods = new HashMap<>();
  private final Map<String, ConstraintsSet> methodConstraints = new HashMap<>();
  private final ConstraintsSet classConstraints = new ConstraintsSet();

  public SyntaxTreeHelper(CompilationUnitTree compilationTree) {
    for (Tree tree : ((ClassTree) compilationTree.types().get(0)).members()) {
      if (tree.is(Tree.Kind.METHOD)) {
        final MethodTree methodTree = (MethodTree) tree;
        methods.put(methodTree.simpleName().name(), methodTree);
        ConstraintsSet constraints = new ConstraintsSet(classConstraints);

        // } else if (tree.is(Tree.Kind.VARIABLE)) {
        // final VariableTree variableTree = (VariableTree) tree;
        // final String name = variableTree.simpleName().name();
        // for (AnnotationTree annotation : variableTree.modifiers().annotations()) {
        // classConstraints.setConstraints(variableTree, value);
        // ;
        // }
      }
    }
  }

  public MethodTree getMethod(String name) {
    return methods.get(name);
  }

  public ConstraintsSet getMethodConstraints(Object key) {
    return methodConstraints.get(key);
  }
}
