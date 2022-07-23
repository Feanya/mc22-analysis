package evaluation

import slick.jdbc.PostgresProfile.api._
import evaluation.utils._
import util.PostgresUtils

class RQ2(postgresInteractor: PostgresUtils) {

  def run(): Unit = {
    println("Calculating versionschemes by year")
    for(i <- 1 to 6)
      two_a(i)
    println("Calculating all upgrades by year")
     for(i <- 2 to 6)
      two_b(i)
    for(i <- 0 to 1)
      two_ctot(i)
    for(i <- 3 to 6)
      two_ctot(i)
  }

  private def two_a(versionscheme: Int): Unit = {

    val innerJoin = postgresInteractor.runAndWait(
      sql"""
        SELECT scheme.year,
       count_scheme,
       count_total,
       (count_scheme*100.0/count_total) AS percent FROM
    (SELECT year,
            count_scheme
     FROM(
             SELECT COUNT(*) AS count_scheme, EXTRACT(YEAR FROM timestamp) AS year
             FROM data
             WHERE classifier IS NULL AND versionscheme = ${versionscheme}
             GROUP BY year) AS a) AS scheme

        JOIN

    (SELECT year,
            count AS count_total
     FROM(
             SELECT COUNT(*) AS count, EXTRACT(YEAR FROM timestamp) AS year
             FROM data
             WHERE classifier IS NULL   -- doesnt matter which exactly
             GROUP BY year) AS a) AS total
          ON total.year=scheme.year
      ORDER BY year
            """.as[(Int, String, String, String)]) // attention, this is unsafe SQL!

    val prefix: String = "study/2a-versionschemes_by_year"
    val filename = s"results/$prefix-$versionscheme.csv"

    val rows = fourTuplesToRows(innerJoin)
    println(writeCsvFile(filename, Array("year", "count_scheme", "count_total", "percent"), rows))
  }


  private def two_b(vJump: Int): Unit = {

    val innerJoin = postgresInteractor.runAndWait(
      sql"""
            SELECT scheme.year,
       count_scheme,
       count_total,
       (count_scheme*100.0/count_total) AS percent FROM
          (SELECT year,
                  count_scheme
          FROM(
          SELECT COUNT(pairresult_backup2.id) AS count_scheme, year
          FROM pairresult_backup2
            JOIN upgrade_years ON pairresult_backup2.id=upgrade_years.id
          WHERE resultname = 'CDeprecatedInPrev'
          AND versionjump = ${vJump}
          GROUP BY year
          ORDER BY year) AS a) AS scheme

          JOIN

          (SELECT year,
                 count AS count_total
          FROM(
                  SELECT COUNT(pairresult_backup2.id) AS count, year
                  FROM pairresult_backup2
                           JOIN upgrade_years ON pairresult_backup2.id=upgrade_years.id
                  WHERE resultname = 'CDeprecatedInPrev'   -- doesnt matter which exactly
                  GROUP BY year) AS a) AS total
          ON total.year=scheme.year
          ORDER BY year
            """.as[(Int, String, String, String)]) // attention, this is unsafe SQL!

    val prefix: String = "study/2b-upgrades_by_year"
    val filename = s"results/$prefix-$vJump.csv"

    val rows = fourTuplesToRows(innerJoin)
    println(writeCsvFile(filename, Array("year", "count_scheme", "count_total", "percent"), rows))
  }


  private def two_c(resultname:String, vJump: Int): Unit = {
    val result = postgresInteractor.runAndWait(
      sql"""SELECT rem.year, count_total, count_crem, (count_crem*100.0/count_total) AS percent FROM
            (SELECT COUNT(*) AS count_crem, year
             FROM pairresult_backup2 AS p
                      JOIN upgrade_years ON p.id=upgrade_years.id
             WHERE resultname = ${resultname} AND value > 0
               AND versionjump = ${vJump}
             GROUP BY year) AS rem
            JOIN
            (SELECT COUNT(*) AS count_total, year
             FROM pairresult_backup2 AS p
                      JOIN upgrade_years ON p.id=upgrade_years.id
             WHERE resultname = ${resultname}
               AND versionjump = ${vJump}
             GROUP BY year) AS total
            ON rem.year = total.year
            ORDER BY year""".as[(Int, Int, Int, Float)]) // attention, this is unsafe SQL!

    val prefix: String = s"${resultname}_upgrades_by_year"
    val filename = s"results/study/2c-$prefix-$vJump.csv"

    val rows = fourTuplesToRows(result)
    println(writeCsvFile(filename, Array("year", "count_total", "count_rem", "percent"), rows))
  }


  private def two_ctot(versionjump: Int): Unit = {

    val result = postgresInteractor.runAndWait(
      sql"""
    SELECT * FROM (
     select EXTRACT(YEAR FROM timestamp) AS year, COUNT(Distinct "jarTwoID") AS count_breaking_upgrades
      FROM pairresult_backup_with_timestamp
      WHERE ((resultname =
             'CWronglyRemoved' AND
             value > 0) or
             (resultname =
             'MWronglyRemovedPub' AND
             value > 0))
      AND versionjump = ${versionjump}
      GROUP BY year) AS foo
      WHERE year BETWEEN 2006 and 2021
         """.as[(String, String)]) // attention, this is unsafe SQL!

    val prefix: String = s"upgrades_by_year"
    val filename = s"results/study/2c-$prefix-allbreaking-$versionjump.csv"

    val rows = twoTuplesToRows(result)
    println(writeCsvFile(filename, Array("year", "count_total_break"), rows))

  }

}
