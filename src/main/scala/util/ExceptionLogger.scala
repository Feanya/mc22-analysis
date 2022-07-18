package util

import java.io.{FileOutputStream, PrintStream}

class ExceptionLogger(postfix: String) {
  val ps = new PrintStream(new FileOutputStream(s"exception$postfix.log", true))
  val pszwo = new PrintStream(new FileOutputStream(s"exceptiongas$postfix.log", true))

    def log(gav: String, text: String): Unit = {
      Console.withOut(pszwo) {
        println(gav)
      }
      Console.withOut(ps) {
        println(text)
      }
    }
}
