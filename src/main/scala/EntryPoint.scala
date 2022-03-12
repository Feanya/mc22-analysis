import application.{PostgresApplication, PostgresApplicationObject}

object EntryPoint {

  final def main(args: Array[String]): Unit = {

    if (args.contains("--test")) {
      println("SFA")
      PostgresApplicationObject.main(args)
    } else {
      println("MFA")
    }

  }

}
