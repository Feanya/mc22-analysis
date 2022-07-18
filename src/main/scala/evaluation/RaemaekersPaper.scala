package evaluation

import evaluation.Filter.{fiveTuplesToRows, postgresInteractor, sevenTuplesToRows, threeTuplesToRows, twoTuplesToRows, writeCsvFile}
import model.{AggregatedGA, PairResults}
import slick.jdbc.ActionBasedSQLInterpolation
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

class RaemaekersPaper {

  /**
   * Version string patterns and frequencies of occurrence in upgrade-jars .
   */
  def reproduceRPtable1(): Unit = {
    val result = postgresInteractor.runAndWait(
      sql"""SELECT originaltime.versionscheme, count_originaltime, count_alltime FROM

            (SELECT d.versionscheme, COUNT(*) AS count_originaltime
            FROM allids_with_timestamp
                     JOIN data d on ids = d.id
            WHERE allids_with_timestamp.timestamp  < '2011-07-11'
            GROUP BY d.versionscheme) AS originaltime

            JOIN

            (SELECT d.versionscheme, COUNT(*) AS count_alltime
            FROM allids_with_timestamp
            JOIN data d on ids = d.id
            GROUP BY d.versionscheme) AS alltime

            ON originaltime.versionscheme=alltime.versionscheme""".as[(Int, Int, Int)]) // attention, this is unsafe SQL!

    val prefix: String = s"rpaper/1-alltime"
    val filename = s"results/$prefix.csv"

    val rows = threeTuplesToRows(result)
    println(writeCsvFile(filename, Array("pattern", "count_originaltime", "count_alltime"), rows))

  }


  /**
   * Version string patterns and frequencies of occurrence in upgrade-jars.
   */
  def myrpTable1Alltime(): Unit = {
    val result = postgresInteractor.runAndWait(
      sql"""SELECT a.versionscheme, count_originaltime, count_alltime,
            count_originaltime*100.0/SUM(count_originaltime) over() AS freq_originaltime,
            count_alltime*100.0/SUM(count_alltime) over() AS freq_alltime
           FROM(
            (SELECT COUNT(*) AS count_originaltime, versionscheme FROM data
            WHERE timestamp < '2011-07-31'
            GROUP BY versionscheme)) AS originaltime

            JOIN

            (SELECT COUNT(*) AS count_alltime, versionscheme FROM data GROUP BY versionscheme) AS a
           ON a.versionscheme=originaltime.versionscheme""".as[(Int, String, String, Float, Float)]) // attention, this is unsafe SQL!

    val prefix: String = s"rpaper/1-alltime-alljars"
    val filename = s"results/$prefix.csv"

    val rows = fiveTuplesToRows(result)
    println(writeCsvFile(filename, Array("pattern", "count_originaltime", "count_alltime", "freq_originaltime", "freq_alltime"), rows))
  }

  def rptable2(): Unit = {
    val result = postgresInteractor.runAndWait(
      sql"""
           SELECT nozero.resultname, sum_originaltime012, sum_alltime012, sum_originaltime34, sum_alltime34 FROM

        -- non-major zero values

    (SELECT alltime.resultname, sum_originaltime012, sum_alltime012 FROM

        (SELECT resultname, SUM(value) AS sum_originaltime012
         FROM pairresult_backup_with_timestamp
         WHERE versionjump IN (0,1,2)
           AND timestamp < '2011-07-31' -- filter for original time
         GROUP BY resultname) AS originaltime

            JOIN

        (SELECT resultname, SUM(value) AS sum_alltime012
         FROM pairresult_backup2
         WHERE versionjump IN (0,1,2)
         GROUP BY resultname) AS alltime

        ON alltime.resultname=originaltime.resultname) AS nozero

        JOIN

        -- join major zero values
    (SELECT alltime.resultname, sum_originaltime34, sum_alltime34 FROM

        (SELECT resultname, SUM(value) AS sum_originaltime34
         FROM pairresult_backup_with_timestamp
         WHERE versionjump IN (3,4)
           AND timestamp < '2011-07-31' -- filter for original time
         GROUP BY resultname) AS originaltime

            JOIN

        (SELECT resultname, SUM(value) AS sum_alltime34
         FROM pairresult_backup2
         WHERE versionjump IN (3,4)
         GROUP BY resultname) AS alltime

        ON alltime.resultname=originaltime.resultname) AS majorzero

    ON nozero.resultname=majorzero.resultname
ORDER BY sum_originaltime012
         """.as[(String, String, String, String, String)])

    val prefix: String = s"rpaper/2-original-alltime-jump012"
    val filename = s"results/$prefix.csv"

    val rows = fiveTuplesToRows(result)
    println(writeCsvFile(filename, Array("resultname", "sum_originaltime012", "sum_alltime012", "sum_originaltime34", "sum_alltime34"), rows))

  }


  def rptable2_012(): Unit = {
    val result = postgresInteractor.runAndWait(
      sql"""
           SELECT nozero.resultname,
       sum_originaltime012,
       sum_originaltime34,
       (sum_originaltime012+sum_originaltime34) AS totaloriginal,
       sum_alltime012,
       sum_alltime34,
       (sum_alltime012+sum_alltime34) AS totalalltime FROM

(SELECT alltime.resultname, sum_originaltime012, sum_alltime012 FROM

(SELECT resultname, SUM(value) AS sum_originaltime012
 FROM pairresult_backup_with_timestamp
 WHERE versionjump IN (0,1,2)
    AND timestamp < '2011-07-31' -- filter for original time
 GROUP BY resultname) AS originaltime

JOIN

(SELECT resultname, SUM(value) AS sum_alltime012
 FROM pairresult_backup2
 WHERE versionjump IN (0,1,2)
 GROUP BY resultname) AS alltime

ON alltime.resultname=originaltime.resultname) AS nozero

JOIN

(SELECT alltime.resultname, sum_originaltime34, sum_alltime34 FROM

    (SELECT resultname, SUM(value) AS sum_originaltime34
     FROM pairresult_backup_with_timestamp
     WHERE versionjump IN (3,4)
       AND timestamp < '2011-07-31' -- filter for original time
     GROUP BY resultname) AS originaltime

        JOIN

    (SELECT resultname, SUM(value) AS sum_alltime34
     FROM pairresult_backup2
     WHERE versionjump IN (3,4)
     GROUP BY resultname) AS alltime

    ON alltime.resultname=originaltime.resultname) AS majorzero

ON nozero.resultname=majorzero.resultname
WHERE nozero.resultname NOT IN ('MDeprecatedInPrevProt','MDeprecatedInPrevPub', 'CDeprecatedInPrev',
                                'MDeprecatedNotRemovedPub', 'MDeprecatedNotRemovedProt', 'CDeprecatedNotRemoved')
ORDER BY sum_originaltime012 DESC
         """.as[(String, String, String, String, String, String, String)])

    val prefix: String = s"rpaper/2-original-alltime-jump012"
    val filename = s"results/$prefix.csv"

    val rows = sevenTuplesToRows(result)
    println(writeCsvFile(filename, Array("resultname",
      "sum_originaltime012",
      "sum_originaltime34",
      "totaloriginal",
      "sum_alltime012",
      "sum_alltime34",
      "totalalltime"), rows))

  }


  def tab_3_upgrades_by_year(vJump: Int): Unit = {
    val prefix: String = "rpaper/3-upgrades_by_year"

    val innerJoin = postgresInteractor.runAndWait(
      sql"""SELECT COUNT(pairresult_backup2.id) AS count, year
            FROM pairresult_backup2
                JOIN upgrade_years ON pairresult_backup2.id=upgrade_years.id
            WHERE resultname = 'CDeprecatedInPrev'
            AND versionjump = ${vJump}
            GROUP BY year
            ORDER BY year""".as[(Int, Int)]) // attention, this is unsafe SQL!

    val filename = s"results/$prefix-$vJump.csv"

    val rows = twoTuplesToRows(innerJoin)
    println(writeCsvFile(filename, Array("count", "year"), rows))
  }

  def fig_5_upgrades_percentages(vJump: Int): Unit = {
    val prefix: String = "rpaper/5-upgrades_by_year"

    // Todo!!!
    val innerJoin = postgresInteractor.runAndWait(
      sql"""SELECT COUNT(pairresult_backup2.id) AS count, year
            FROM pairresult_backup2
                JOIN upgrade_years ON pairresult_backup2.id=upgrade_years.id
            WHERE resultname = 'CDeprecatedInPrev'
            AND versionjump = ${vJump}
            GROUP BY year
            ORDER BY year""".as[(Int, Int)]) // attention, this is unsafe SQL!
  // Tooodo!

    val filename = s"results/$prefix-$vJump.csv"

    val rows = twoTuplesToRows(innerJoin)
    println(writeCsvFile(filename, Array("count", "year"), rows))
  }


}
