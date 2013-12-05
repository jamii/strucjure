package strucjure.view;

import clojure.lang.RT;
import clojure.lang.Var;

public class Failure extends Exception {
  public static Var prstr = RT.var("clojure.core", "pr-str");
  public String test;
  public Object input;

  public Failure(String test, Object input) {
    super();
    this.test = test;
    this.input = input;
  }

  public String getMessage() {
    return "Failed test " + test + " on input " + prstr.invoke(input);
  }
}
