package strucjure.view;

import clojure.lang.RT;
import clojure.lang.Var;

public class Failure extends Exception {
  public static Var prstr = RT.var("clojure.core", "pr-str");
  public String test;
  public String pattern;
  public Object input;
  public Failure lastFailure;

  public Failure(String test, String pattern, Object input, Failure lastFailure) {
    super();
    this.test = test;
    this.pattern = pattern;
    this.input = input;
    this.lastFailure = lastFailure;
  }

  public String getMessage() {
    StringBuilder builder = new StringBuilder();
    this.getMessage(builder);
    return builder.toString();
  }

  public void getMessage(StringBuilder builder) {
    builder.append("\nFailed test " + test + " in pattern " + pattern + " on input " + prstr.invoke(input));
    if (lastFailure != null)
      lastFailure.getMessage(builder);
  }
}
