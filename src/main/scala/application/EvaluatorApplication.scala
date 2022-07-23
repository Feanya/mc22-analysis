package application

import analysis.NamedAnalysis
import evaluation._



class EvaluatorApplication extends PostgresApplication {

  final override def main(args: Array[String]): Unit = {
    cfg = parseArguments(args)

    println("Me evaluation unit \uD83D\uDE80")

    Views.createViews(postgresInteractor)

    dataset_data()
    reproduceRaemaekers()
    conduct_study()

    shutdown()

  }

  def reproduceRaemaekers(): Unit = {
    log.info("Reproduce Raemaekers")
    val driver = new RaemaekersPaper(postgresInteractor)
    driver.reproduce()
  }

  def dataset_data(): Unit = {
    log.info("Generate data for descriptive statistics")
    val driver = new Datasetpaper(postgresInteractor)
    driver.run()
      }

  def conduct_study(): Unit = {
   log.info("Answer RQ1")
   val driverA = new RQ1(postgresInteractor)
    driverA.run()


    log.info("Answer RQ2")
    val driverB = new RQ2(postgresInteractor)
    driverB.run()


    log.info("Answer RQ4")
    val driverD = new RQ4(postgresInteractor)
    driverD.run()

  }


  override def shutdown(): Unit = { postgresInteractor.closeConnection() }
  override def buildAnalysis(): Seq[NamedAnalysis] = {Seq()}
}
