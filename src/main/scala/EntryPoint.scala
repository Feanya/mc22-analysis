import application.{EvaluatorApplication, PostgresApplication}

import scala.sys.exit

object EntryPoint {

  final def main(args: Array[String]): Unit = {

    if (args.contains("--import")) {
      println("Clean database and import from rclones lsl")
    }
    if (args.contains("--run-analysis")) {
      println("Run: PostgresAnalysis! \uD83D\uDE80")
      println(s"🔧 Config: ${args.mkString(",")}")
      exit(42)
      val app = new PostgresApplication()
      app.main(args)
    }
    if (args.contains("--evaluate")) {
      println("Evaluate database…")
      exit(43)
      val app = new EvaluatorApplication()
      app.main(args)
    }
    else {
      println("Please select at least one task to run! Tasks are: --import, --run-analysis, --evaluate.")
    }
  }

}
