package evaluation

import slick.jdbc.PostgresProfile.api._
import util.PostgresUtils

object Views {

  def createViews(postgresInteractor: PostgresUtils): Unit = {
    createViewOnUpgradesWithYear(postgresInteractor)
    createViewOnUpgradeIds(postgresInteractor)
    createViewPairsWithTimestamp(postgresInteractor)
    createViewPrimaries(postgresInteractor)
  }

  private def createViewOnUpgradesWithYear(postgresInteractor: PostgresUtils): Unit = {

    println("Creating view")
    val createUpgradeView = postgresInteractor.runAndWait(
      sqlu"""CREATE OR REPLACE VIEW upgrade_years AS
            (SELECT p.id, EXTRACT(YEAR FROM d.timestamp) AS year
            FROM pairresult_backup2 AS p
            JOIN data d on d.id = p."jarTwoID")
         """)

    assert(createUpgradeView == 0)
    println("Done!")
    println("Test view:")
    val testView = postgresInteractor.runAndWait(sql"""SELECT * FROM upgrade_years LIMIT 5""".as[(Int, Int)])

    println(testView)
  }

  private def createViewOnUpgradeIds(postgresInteractor: PostgresUtils): Unit = {
    println("Materializing view on jars that are contained in an upgrade")
    val createView = postgresInteractor.runAndWait(
      sqlu"""
             CREATE MATERIALIZED VIEW IF NOT EXISTS allids_with_timestamp AS
                SELECT ids, timestamp FROM
                ((SELECT "jarOneID" AS ids FROM pairresult_backup2)
                UNION DISTINCT
                (SELECT "jarTwoID" AS ids FROM pairresult_backup2)) upgradeids
                JOIN data ON upgradeids.ids=data.id
                """)
    assert(createView == 0)
    println("Done")
  }

  private def createViewPairsWithTimestamp(postgresInteractor: PostgresUtils): Unit = {
    println("Materializing view on jars that are contained in an upgrade")
    val createView = postgresInteractor.runAndWait(
      sqlu"""
             CREATE OR REPLACE VIEW pairresult_backup_with_timestamp AS
                SELECT alltime.*, timestamps.timestamp
                FROM
                    (SELECT * FROM pairresult_backup2) AS alltime
                        JOIN
                        (SELECT * FROM allids_with_timestamp) AS timestamps
                            ON alltime."jarTwoID"=timestamps.ids""")
    assert(createView == 0)
    println("Done")
  }

  private def createViewPrimaries(postgresInteractor: PostgresUtils): Unit = {
    println("Creating view on primary jars")
    val createView = postgresInteractor.runAndWait(
      sqlu"""
             CREATE OR REPLACE VIEW primary_data AS
                SELECT * FROM data WHERE classifier IS NULL""")
    assert(createView == 0)
    println("Done")
  }

}
