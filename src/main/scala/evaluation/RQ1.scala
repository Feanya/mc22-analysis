package evaluation

import evaluation.utils._
import slick.jdbc.PostgresProfile.api._
import util.PostgresUtils

class RQ1(postgresInteractor: PostgresUtils) {

  def run(): Unit = {
    one_a()
    one_az()
    one_anz()
    one_b()
    one_c("CWronglyRemoved", 0)
    one_c("CWronglyRemoved", 1)
    for (i <- 3 to 4) one_c("CWronglyRemoved", i)
    one_c("MWronglyRemovedPub", 0)
    one_c("MWronglyRemovedPub", 1)
    for (i <- 3 to 4) one_c("MWronglyRemovedPub", i)
    one_ctot()
  }

  /**
   * */
  def one_a(): Unit = {

    val result = postgresInteractor.runAndWait(sql"""
           SELECT a.versionscheme, count_studytime, count_alltime,
       count_studytime*100.0/SUM(count_studytime) over() AS freq_studytime,
       count_alltime*100.0/SUM(count_alltime) over() AS freq_alltime
        FROM(
        (SELECT COUNT(*) AS count_studytime, versionscheme FROM data
         WHERE EXTRACT(YEAR FROM timestamp) BETWEEN 2006 AND 2021 AND classifier IS NULL
         GROUP BY versionscheme)) AS studytime

        JOIN

    (SELECT COUNT(*) AS count_alltime, versionscheme
     FROM data
     WHERE classifier IS NULL
     GROUP BY versionscheme) AS a
    ON a.versionscheme=studytime.versionscheme
         """.as[(Int, String, String, Float, Float)]) // attention, this is unsafe SQL!

    val prefix: String = s"study/1a-syntax-alltime-primaries"
    val filename = s"results/$prefix.csv"

    val rows = fiveTuplesToRows(result)
    println(writeCsvFile(filename, Array("pattern", "count_studytime", "count_alltime", "freq_studytime", "freq_alltime"), rows))

  }


  def one_az(): Unit = {

    val result = postgresInteractor.runAndWait(sql"""
        SELECT versionscheme, count_studytime
        FROM(
        (SELECT COUNT(*) AS count_studytime, versionscheme FROM data
         WHERE EXTRACT(YEAR FROM timestamp) BETWEEN 2006 AND 2021 AND classifier IS NULL
         AND version<'1'
         GROUP BY versionscheme)) AS studytime
         """.as[(Int, String)]) // attention, this is unsafe SQL!

    val prefix: String = s"study/1a-syntax-alltime-primaries-majorzero"
    val filename = s"results/$prefix.csv"

    val rows = twoTuplesToRows(result)
    println(writeCsvFile(filename, Array("pattern", "count_studytime"), rows))

  }


  def one_anz(): Unit = {

    val result = postgresInteractor.runAndWait(sql"""
        SELECT versionscheme, count_studytime
        FROM(
        (SELECT COUNT(*) AS count_studytime, versionscheme FROM data
         WHERE EXTRACT(YEAR FROM timestamp) BETWEEN 2006 AND 2021 AND classifier IS NULL
         AND version>='1'
         GROUP BY versionscheme)) AS studytime
         """.as[(Int, String)]) // attention, this is unsafe SQL!

    val prefix: String = s"study/1a-syntax-alltime-primaries-nonzero"
    val filename = s"results/$prefix.csv"

    val rows = twoTuplesToRows(result)
    println(writeCsvFile(filename, Array("pattern", "count_studytime"), rows))

  }


  def one_b(): Unit = {

    val result = postgresInteractor.runAndWait(sql"""
    SELECT a.versionjump, count_total_upgrades, count_total_upgrades*100.0/SUM(count_total_upgrades) over() AS percent FROM
    (SELECT COUNT(*) AS count_total_upgrades, versionjump
     FROM pairresult_backup_with_timestamp
     WHERE resultname = 'CWronglyRemoved'
       AND EXTRACT(YEAR FROM timestamp) BETWEEN 2006 AND 2021
     GROUP BY versionjump) AS a
         """.as[(Int, String, String)]) // attention, this is unsafe SQL!

    val prefix: String = s"study/1b-upgradetypes-alltime-distribution"
    val filename = s"results/$prefix.csv"

    val rows = threeTuplesToRows(result)
    println(writeCsvFile(filename, Array("release_type", "count_total", "percent"), rows))

  }


  def one_c(resultname: String, vJump: Int): Unit = {

    val result = postgresInteractor.runAndWait(sql"""
        SELECT upgrades_total,
               crem_total,
              (crem_total*100/upgrades_total) AS percent FROM (
       SELECT SUM(count_total) AS upgrades_total,
              SUM(count_crem) AS crem_total
       FROM
    (SELECT COUNT(*) AS count_crem, year
     FROM pairresult_backup2 AS p
              JOIN upgrade_years ON p.id=upgrade_years.id
     WHERE resultname = $resultname AND value > 0
       AND versionjump = $vJump
     AND year BETWEEN 2006 AND 2021
     GROUP BY year) AS rem
        JOIN
    (SELECT COUNT(*) AS count_total, year
     FROM pairresult_backup2 AS p
              JOIN upgrade_years ON p.id=upgrade_years.id
     WHERE resultname = $resultname
       AND versionjump = $vJump
     AND year BETWEEN 2006 AND 2021
     GROUP BY year) AS total
    ON rem.year = total.year)AS foo
         """.as[(String, String, String)]) // attention, this is unsafe SQL!

    val prefix: String = s"${resultname}_upgrades_by_year"
    val filename = s"results/study/1c-$prefix-$vJump.csv"

    val rows = threeTuplesToRows(result)
    println(writeCsvFile(filename, Array("count_total", "count_rem", "percent"), rows))

  }


  def one_ctot(): Unit = {

    val result = postgresInteractor.runAndWait(sql"""
     select versionjump, COUNT(Distinct "jarTwoID") AS count_breaking_upgrades
      FROM pairresult_backup_with_timestamp
      WHERE ((resultname =
             'CWronglyRemoved' AND
             value > 0) or
             (resultname =
             'MWronglyRemovedPub' AND
             value > 0))
      AND EXTRACT(YEAR FROM timestamp) BETWEEN 2006 and 2021
      GROUP BY versionjump
         """.as[(String, String)]) // attention, this is unsafe SQL!

    val prefix: String = s"upgrades_by_year"
    val filename = s"results/study/1c-$prefix-allbreaking.csv"

    val rows = twoTuplesToRows(result)
    println(writeCsvFile(filename, Array("versionjump", "count_total_break"), rows))

  }


}
