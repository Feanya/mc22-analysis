import application.PostgresApplication

object EntryPoint {

  final def main(args: Array[String]): Unit = {

    if (args.contains("--test")) {
      println("SFA")
    } else {
      println("Run: PostgresAnalysis! \uD83D\uDE80")
      val app = new PostgresApplication()
      app.main(args)
    }

  }

}
