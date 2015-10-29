package org.sonar.java.se.dhb;

import org.sonar.plugins.java.api.JavaFileScannerContext;

import java.util.HashMap;
import java.util.Map;

public class CompiledClass {

  private final ConstraintsSet constraints = new ConstraintsSet();
  private final Map<String, CompiledMethod> methods = new HashMap<>();

  public ConstraintsSet constraints() {
    return constraints;
  }

  public CompiledMethod getMethod(Object name) {
    return methods.get(name);
  }

  public CompiledMethod putMethod(String name, CompiledMethod method) {
    return methods.put(name, method);
  }

  public void executeAll(JavaFileScannerContext context, int maximumComplexity) {
    for (CompiledMethod method : methods.values()) {
      method.execute(context, maximumComplexity);
    }
  }

  public void execute(String methodName, JavaFileScannerContext context, int maximumComplexity) {
    methods.get(methodName).execute(context, maximumComplexity);
  }
}
