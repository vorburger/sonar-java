public class SymbolicExecutorTestClass {
  @interface Nullable {
  }
  
  @Nullable
  private String to;
  
  private String text = "Hello";

  public boolean nullableFieldNPE(@Nullable String from) {
    to.collectResult(from.device());
  }
  
  public boolean cascadedAndInIfNPE(Object from, Object to) {
    if(to != null && from == null && from.equals(to.origin())) {
      to.collectResult(from.device());
    }
  }
}
