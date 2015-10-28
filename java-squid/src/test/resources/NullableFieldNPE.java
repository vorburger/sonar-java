public class NullableFieldNPE {
  @interface Nullable {
  }
  
  @Nullable
  private String to;
  
  private String text = "Hello";

  public boolean nullableFieldNPE(@Nullable String from) {
    to.collectResult(from.device());
  }
}
