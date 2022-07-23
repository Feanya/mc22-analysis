import application.{EvaluatorApplication, PostgresApplication}

import scala.sys.exit

object EntryPoint {

  final def main(args: Array[String]): Unit = {

    if (args.contains("--import")) {
      println("Run: Import! \uD83D\uDE80")
      println("Clean database and import from rclones lsl")
      println("Sorry, not implemented yet!! Use MC-scripts")
    }
    if (args.contains("--run-analysis")) {
      println("Run: PostgresAnalysis! \uD83D\uDE80")
      println(s"🔧 Config: ${args.mkString(",")}")
      val app = new PostgresApplication()
      app.main(args)
    }
    if (args.contains("--evaluate")) {
      println("Run: Evaluation of the database! \uD83D\uDE80")
      val app = new EvaluatorApplication()
      app.main(args)
    }
    else {
      println("Please select at least one task to run! Tasks are: --import, --run-analysis, --evaluate.")
    }
  }

}
