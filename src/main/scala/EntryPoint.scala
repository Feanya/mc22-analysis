import application.PostgresApplication

object EntryPoint {

  final def main(args: Array[String]): Unit = {

    if (args.contains("--import")) {
      println("Clean database and import from rclones lsl")
    } else {
      println("Run: PostgresAnalysis! \uD83D\uDE80")
      println(s"🔧 Config: ${args.mkString(",")}")
      val app = new PostgresApplication()
      app.main(args)
    }

  }

}
