class Foo {

  int unusedField;

  public void f(int unusedParameter) {
    int unusedLocalVariable; // Noncompliant {{Remove this unused "unusedLocalVariable" local variable.}}

    int usedLocalVariable = 42;
    System.out.println(usedLocalVariable);

    try {
    } catch (Exception e) {
    }

    try (Stream foo = new Stream()) { // Noncompliant {{Remove this unused "foo" local variable.}}
    }

    for (int a: new int[]{ 0, 1, 2 }) { // Noncompliant
    }

    for (int i = 0; condition(); i++) { // Noncompliant
    }
    
    for (int j = 0; j < 10; j++) {
    }
    
    try (Stream foo2 = new Stream()) {
      foo2.bar();
    }
    
    int notReadLocalVariable = 0; // Noncompliant
    notReadLocalVariable = 1;
    notReadLocalVariable += 1;
    notReadLocalVariable++;
    
    int readLocalVariable;
    notReadLocalVariable = readLocalVariable++;
    
    unknown++;
    this.unknown++;
    java.util.Stream<Object> s;
    s.map(v -> "");
  }

}
