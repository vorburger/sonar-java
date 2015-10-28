package org.sonar.java.se.dhb;

import com.google.common.base.Charsets;
import com.sonar.sslr.api.typed.ActionParser;
import org.fest.assertions.Fail;
import org.junit.Test;
import org.sonar.java.ast.parser.JavaParser;
import org.sonar.java.cfg.CFG;
import org.sonar.java.model.JavaTree;
import org.sonar.plugins.java.api.JavaCheck;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.CompilationUnitTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Tree;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.fest.assertions.Assertions.assertThat;

public class SymbolicExecutorTest {

  public static final ActionParser<Tree> parser = JavaParser.createParser(Charsets.UTF_8);

  private static CFG buildCFG(final String methodCode) {
    final CompilationUnitTree cut = (CompilationUnitTree) parser.parse("class A { " + methodCode + " }");
    final MethodTree tree = ((MethodTree) ((ClassTree) cut.types().get(0)).members().get(0));
    return CFG.build(tree);
  }

  private static CompilationUnitExecutor buildCFGFromResource(String resourceName) {
    try (final InputStream stream = SymbolicExecutorTest.class.getResourceAsStream(resourceName);
      final InputStreamReader fileReader = new InputStreamReader(stream);
      final BufferedReader reader = new BufferedReader(fileReader);
      final StringWriter out = new StringWriter();
      final PrintWriter printer = new PrintWriter(out);) {
      String line = reader.readLine();
      while (line != null) {
        printer.println(line);
        line = reader.readLine();
      }
      printer.close();
      final CompilationUnitTree compiledClass = (CompilationUnitTree) parser.parse(out.toString());
      CompilationUnitExecutor generator = new CompilationUnitExecutor(compiledClass);
      compiledClass.accept(generator);
      return generator;
    } catch (IOException e) {
      Fail.fail("Unable to open resource " + resourceName);
    }
    return null;
  }

  private static class TestScanner implements JavaFileScannerContext {

    private final Map<Integer, String> messages = new HashMap<>();

    public boolean isEmpty() {
      return messages.isEmpty();
    }

    public int size() {
      return messages.size();
    }

    public String getMessage(int line) {
      return messages.get(Integer.valueOf(line));
    }

    @Override
    public CompilationUnitTree getTree() {
      return null;
    }

    @Override
    public void addIssue(Tree tree, JavaCheck check, String message) {
      addIssue(tree, check, message, null);
    }

    @Override
    public void addIssue(Tree tree, JavaCheck check, String message, Double cost) {
      addIssue(((JavaTree) tree).getLine(), check, message, cost);
    }

    @Override
    public void addIssueOnFile(JavaCheck check, String message) {
      addIssue(-1, check, message);
    }

    @Override
    public void addIssue(int line, JavaCheck check, String message) {
      addIssue(line, check, message, null);
    }

    @Override
    public void addIssue(int line, JavaCheck javaCheck, String message, Double cost) {
      messages.put(Integer.valueOf(line), message);
    }

    @Override
    public void addIssue(File file, JavaCheck check, int line, String message) {
      addIssue(line, check, message, null);
    }

    @Override
    public Object getSemanticModel() {
      return null;
    }

    @Override
    public String getFileKey() {
      return null;
    }

    @Override
    public File getFile() {
      return null;
    }

    @Override
    public int getComplexity(Tree tree) {
      return 0;
    }

    @Override
    public int getMethodComplexity(ClassTree enclosingClass, MethodTree methodTree) {
      return 0;
    }

    @Override
    public List<Tree> getComplexityNodes(Tree tree) {
      return null;
    }

    @Override
    public List<Tree> getMethodComplexityNodes(ClassTree enclosingClass, MethodTree methodTree) {
      return null;
    }

    @Override
    public void addNoSonarLines(Set<Integer> lines) {
    }

    @Override
    public void reportIssue(JavaCheck check, Tree tree, String message) {
      addIssue(tree, check, message, null);
    }

    @Override
    public void reportIssue(JavaCheck check, Tree tree, String message, List<Location> secondaryLocations, Integer cost) {
      addIssue(tree, check, message, null);
    }

    @Override
    public Integer getJavaVersion() {
      return null;
    }
  }

  @Test
  public void simpleAssignment() {
    final CFG cfg = buildCFG("void fun() { String a = \"Hello\"; String b = null; String d = a; char c = 'c'; b= a; b.toString();}");
    TestScanner report = new TestScanner();
    SymbolicExecutor executor = new SymbolicExecutor(report);
    executor.execute(cfg);
    assertThat(report.isEmpty()).as("No error should have been reported").isTrue();
  }

  @Test
  public void simpleNPE() {
    final CFG cfg = buildCFG("void fun() { String a = null; a.toString();}");
    TestScanner report = new TestScanner();
    SymbolicExecutor executor = new SymbolicExecutor(report);
    executor.execute(cfg);
    assertThat(report.size()).as("Number of errors").isEqualTo(1);
    assertThat(report.getMessage(1)).as("NPE expected at line 1").isEqualTo("NullPointerException might be thrown as 'a' is nullable here");
  }

  @Test
  public void indirectNPE() {
    final CFG cfg = buildCFG("void fun() { String a = null; String b = a; a=\"etc\"; b.toString();}");
    TestScanner report = new TestScanner();
    SymbolicExecutor executor = new SymbolicExecutor(report);
    executor.execute(cfg);
    assertThat(report.size()).as("Number of errors").isEqualTo(1);
    assertThat(report.getMessage(1)).as("NPE expected at line 1").isEqualTo("NullPointerException might be thrown as 'b' is nullable here");
  }

  @Test
  public void noNPE() {
    final CFG cfg = buildCFG("void fun() { String a = getString(); a.toString();}");
    TestScanner report = new TestScanner();
    SymbolicExecutor executor = new SymbolicExecutor(report);
    executor.execute(cfg);
    assertThat(report.isEmpty()).as("No error should have been reported").isTrue();
  }

  @Test
  public void conditionalNPE() {
    final CFG cfg = buildCFG("void fun(String a) { if (a==null) {a.toString();}}");
    TestScanner report = new TestScanner();
    SymbolicExecutor executor = new SymbolicExecutor(report);
    executor.execute(cfg);
    assertThat(report.size()).as("Number of errors").isEqualTo(1);
    assertThat(report.getMessage(1)).as("NPE expected at line 1").isEqualTo("NullPointerException might be thrown as 'a' is nullable here");
  }

  @Test
  public void conditionalNPEInverted() {
    final CFG cfg = buildCFG("void fun(String a) { if (null==a) {a.toString();}}");
    TestScanner report = new TestScanner();
    SymbolicExecutor executor = new SymbolicExecutor(report);
    executor.execute(cfg);
    assertThat(report.size()).as("Number of errors").isEqualTo(1);
    assertThat(report.getMessage(1)).as("NPE expected at line 1").isEqualTo("NullPointerException might be thrown as 'a' is nullable here");
  }

  @Test
  public void conditionalNoError() {
    final CFG cfg = buildCFG("void fun(String a) { if (a==null) {a = \"Hello\";} a.toString();}");
    TestScanner report = new TestScanner();
    SymbolicExecutor executor = new SymbolicExecutor(report);
    executor.execute(cfg);
    assertThat(report.isEmpty()).as("No error should have been reported").isTrue();
  }

  @Test
  public void unneededIf() {
    final CFG cfg = buildCFG("void fun() { String a = \"Hello\"; if (a==null) {a = \"Hello world!\";} a.toString();}");
    TestScanner report = new TestScanner();
    SymbolicExecutor executor = new SymbolicExecutor(report);
    executor.execute(cfg);
    assertThat(report.size()).as("Number of errors").isEqualTo(1);
    assertThat(report.getMessage(1)).as("Unneeded IF expected at line 1").isEqualTo("Change this condition so that it does not always evaluate to \"false\"");
  }

  @Test
  public void unneededSecondIf() {
    final CFG cfg = buildCFG("void fun(String a) { if (a==null) {String b = \"Hello world!\"; if (a==null) {a = \"unneeded!\";} a = \"Hello\";} a.toString();}");
    TestScanner report = new TestScanner();
    SymbolicExecutor executor = new SymbolicExecutor(report);
    executor.execute(cfg);
    assertThat(report.size()).as("Number of errors").isEqualTo(1);
    assertThat(report.getMessage(1)).as("Unneeded IF expected at line 1").isEqualTo("Change this condition so that it does not always evaluate to \"true\"");
  }

  @Test
  public void unneededSecondIf2() {
    final CFG cfg = buildCFG("void fun() { String a = getString(); if (a==null) {String b = \"Hello world!\"; if (a!=null) {b = \"unneeded!\";}} a.toString();}");
    TestScanner report = new TestScanner();
    SymbolicExecutor executor = new SymbolicExecutor(report);
    executor.execute(cfg);
    assertThat(report.size()).as("Number of errors").isEqualTo(1);
    assertThat(report.getMessage(1)).as("Unneeded IF expected at line 1").isEqualTo("Change this condition so that it does not always evaluate to \"false\"");
  }

  @Test
  public void cascadedAndReturn() {
    final CFG cfg = buildCFG("boolean fun(Object from, Object to) {return to != null && from != null && from.equals(to.origin());}");
    TestScanner report = new TestScanner();
    SymbolicExecutor executor = new SymbolicExecutor(report);
    executor.execute(cfg);
    assertThat(report.isEmpty()).as("No error should have been reported").isTrue();
  }

  @Test
  public void cascadedAndReturnNPE() {
    final CFG cfg = buildCFG("boolean fun(Object from, Object to) {return to == null && from != null && from.equals(to.origin());}");
    TestScanner report = new TestScanner();
    SymbolicExecutor executor = new SymbolicExecutor(report);
    executor.execute(cfg);
    assertThat(report.size()).as("Number of errors").isEqualTo(1);
    assertThat(report.getMessage(1)).as("NPE expected at line 1").isEqualTo("NullPointerException might be thrown as 'to' is nullable here");
  }

  @Test
  public void cascadedAndIndirectReturn() {
    final CFG cfg = buildCFG("boolean fun(Object from, Object to) {boolean result = to != null && from != null && from.equals(to.origin()); return result;}");
    TestScanner report = new TestScanner();
    SymbolicExecutor executor = new SymbolicExecutor(report);
    executor.execute(cfg);
    assertThat(report.isEmpty()).as("No error should have been reported").isTrue();
  }

  @Test
  public void cascadedAndIndirectReturnNPE() {
    final CFG cfg = buildCFG("boolean fun(Object from, Object to) {boolean result = to == null && from != null && from.equals(to.origin()); return result;}");
    TestScanner report = new TestScanner();
    SymbolicExecutor executor = new SymbolicExecutor(report);
    executor.execute(cfg);
    assertThat(report.size()).as("Number of errors").isEqualTo(1);
    assertThat(report.getMessage(1)).as("NPE expected at line 1").isEqualTo("NullPointerException might be thrown as 'to' is nullable here");
  }

  @Test
  public void cascadedAndInIf() {
    final CFG cfg = buildCFG("boolean fun(Object from, Object to) {if(to != null && from != null && from.equals(to.origin())) {to.collectResult(from.device());}}");
    TestScanner report = new TestScanner();
    SymbolicExecutor executor = new SymbolicExecutor(report);
    executor.execute(cfg);
    assertThat(report.isEmpty()).as("No error should have been reported").isTrue();
  }

  @Test
  public void cascadedAndInIfNPE() {
    final CFG cfg = buildCFG("boolean fun(Object from, Object to) {if(to != null && from == null && from.equals(to.origin())) {to.collectResult(from.device());}}");
    TestScanner report = new TestScanner();
    SymbolicExecutor executor = new SymbolicExecutor(report);
    executor.execute(cfg);
    assertThat(report.size()).as("Number of errors").isEqualTo(1);
    assertThat(report.getMessage(1)).as("NPE expected at line 1").isEqualTo("NullPointerException might be thrown as 'from' is nullable here");
  }

  @Test
   public void nullableField() {
    final CompilationUnitExecutor executor = buildCFGFromResource("/NullableFieldNPE.java");
    TestScanner report = new TestScanner();
    executor.execute(report);
    assertThat(report.size()).as("Number of errors").isEqualTo(1);
    assertThat(report.getMessage(11)).as("NPE expected at line 1").isEqualTo("NullPointerException might be thrown as 'from' is nullable here");
   }
}
