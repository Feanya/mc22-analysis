package evaluation

import evaluation.Filter._
import slick.jdbc.PostgresProfile.api._

class RQ4 {

  /**
   *
   */
  def four_a(): Unit = {

    val result = postgresInteractor.runAndWait(
      sql"""
     select versionjump, COUNT(Distinct "jarTwoID") AS count_jars_with_deprecationtag
      FROM pairresult_backup_with_timestamp
      WHERE ((resultname =
             'CDeprecatedInPrev' AND
             value > 0) or
             (resultname =
             'MDeprecatedInPrevPub' AND
             value > 0))
      AND EXTRACT(YEAR FROM timestamp) BETWEEN 2006 and 2021
      GROUP BY versionjump
         """.as[(String, String)]) // attention, this is unsafe SQL!

    val prefix: String = s"d"
    val filename = s"results/study/4a-$prefix-alldepr.csv"

    val rows = twoTuplesToRows(result)
    println(writeCsvFile(filename, Array("versionjump", "count_jars_with_deprecationtag"), rows))

  }


  def four_ac(resultname:String): Unit = {

    val result = postgresInteractor.runAndWait(
      sql"""
     select versionjump, COUNT(Distinct "jarTwoID") AS count_jars_with_deprecationtag
      FROM pairresult_backup_with_timestamp
      WHERE ((resultname =
             ${resultname} AND
             value > 0))
      AND EXTRACT(YEAR FROM timestamp) BETWEEN 2006 and 2021
      GROUP BY versionjump
         """.as[(String, String)]) // attention, this is unsafe SQL!

    val prefix: String = resultname
    val filename = s"results/study/4a-$prefix-alldepr.csv"

    val rows = twoTuplesToRows(result)
    println(writeCsvFile(filename, Array("versionjump", "count_jars_with_deprecationtag"), rows))

  }


  def four_ap(): Unit = {

    val result = postgresInteractor.runAndWait(
      sql"""
     select versionjump, COUNT(Distinct "jarTwoID") AS count_jars_with_deprecationtag
      FROM pairresult_backup_with_timestamp
      WHERE ((resultname =
             'MDeprecatedInPrevPub' AND
             value > 0))
      AND EXTRACT(YEAR FROM timestamp) BETWEEN 2006 and 2021
      GROUP BY versionjump
         """.as[(String, String)]) // attention, this is unsafe SQL!

    val prefix: String = s"d"
    val filename = s"results/study/4a-$prefix-alldepr.csv"

    val rows = twoTuplesToRows(result)
    println(writeCsvFile(filename, Array("versionjump", "count_jars_with_deprecationtag"), rows))

  }


  def four_atc(): Unit = {

    val result = postgresInteractor.runAndWait(
      sql"""
     select versionjump, COUNT(Distinct "jarTwoID") AS count_jars_with_deprecationtag
      FROM pairresult_backup_with_timestamp
      WHERE ((resultname =
             'CDeprecatedAndRemoved' AND
             value > 0) or
             (resultname =
             'MDeprecatedAndRemovedPub' AND
             value > 0))
      AND EXTRACT(YEAR FROM timestamp) BETWEEN 2006 and 2021
      GROUP BY versionjump
         """.as[(String, String)]) // attention, this is unsafe SQL!

    val prefix: String = "correct"
    val filename = s"results/study/4a-$prefix-alldepr.csv"

    val rows = twoTuplesToRows(result)
    println(writeCsvFile(filename, Array("versionjump", "count_corr_rem"), rows))

  }


  def four_atw(): Unit = {

    val result = postgresInteractor.runAndWait(
      sql"""
     select versionjump, COUNT(Distinct "jarTwoID") AS count_jars_with_deprecationtag
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

    val prefix: String = "wrong"
    val filename = s"results/study/4a-$prefix-alldepr.csv"

    val rows = twoTuplesToRows(result)
    println(writeCsvFile(filename, Array("versionjump", "count_wrong_rem"), rows))

  }


  def four_atwp(): Unit = {

    val result = postgresInteractor.runAndWait(
      sql"""
SELECT versionjump, COUNT(*) FROM
     (SELECT Distinct "jarTwoID"
      FROM pairresult_backup_with_timestamp
      WHERE ((resultname =
             'CWronglyRemoved' AND
             value = 0) or
             (resultname =
             'MWronglyRemovedPub' AND
             value = 0)
          )
      AND EXTRACT(YEAR FROM timestamp) BETWEEN 2006 and 2021) AS onlycorr

      JOIN
      (SELECT "jarTwoID", versionjump
      FROM pairresult_backup_with_timestamp
      WHERE ((resultname =
             'CDeprecatedAndRemoved' AND
             value > 0) or
             (resultname =
             'MDeprecatedAndRemovedPub' AND
             value > 0)
          )
      AND EXTRACT(YEAR FROM timestamp) BETWEEN 2006 and 2021) AS onecorr
         ON onecorr."jarTwoID"=onlycorr."jarTwoID"
            GROUP BY versionjump ORDER BY versionjump

         """.as[(String, String)]) // attention, this is unsafe SQL!

    val prefix: String = "no-fail-but-a-pos"
    val filename = s"results/study/4a-$prefix-alldepr.csv"

    val rows = twoTuplesToRows(result)
    println(writeCsvFile(filename, Array("versionjump", "count_wrong_rem"), rows))

  }




  def four_b(versionjump: Int): Unit = {

    val result = postgresInteractor.runAndWait(
      sql"""
     select year, COUNT(Distinct "jarTwoID") AS count_jars_with_deprecationtag FROM (
     SELECT * , EXTRACT(YEAR FROM timestamp) AS year
      FROM pairresult_backup_with_timestamp
      WHERE ((resultname =
             'CDeprecatedInPrev' AND
             value > 0) or
             (resultname =
             'MDeprecatedInPrevPub' AND
             value > 0))
        AND versionjump= ${versionjump}) AS foo
        WHERE year BETWEEN 2006 and 2021
        GROUP BY (year)
         """.as[(String, String)]) // attention, this is unsafe SQL!

    val prefix: String = s"d"
    val filename = s"results/study/4b-$prefix-${versionjump}-alldepr.csv"

    val rows = twoTuplesToRows(result)
    println(writeCsvFile(filename, Array("year", "count_jars_with_deprecationtag"), rows))

  }



  def four_b_totdepr(): Unit = {

    val result = postgresInteractor.runAndWait(
      sql"""
     select year, COUNT(Distinct "jarTwoID") AS count_jars_with_deprecationtag FROM (
     SELECT * , EXTRACT(YEAR FROM timestamp) AS year
      FROM pairresult_backup_with_timestamp
      WHERE ((resultname =
             'CDeprecatedInPrev' AND
             value > 0) or
             (resultname =
             'MDeprecatedInPrevPub' AND
             value > 0))) AS foo
        WHERE year BETWEEN 2006 and 2021
        GROUP BY (year)
         """.as[(String, String)]) // attention, this is unsafe SQL!

    val prefix: String = s"d"
    val filename = s"results/study/4b-$prefix-totdepr.csv"

    val rows = twoTuplesToRows(result)
    println(writeCsvFile(filename, Array("year", "count_jars_with_deprecationtag"), rows))

  }


  def four_btotwrong(): Unit = {

    val result = postgresInteractor.runAndWait(
      sql"""
     select year, COUNT(Distinct "jarTwoID") AS count_jars_with_deprecationtag FROM (
     SELECT * , EXTRACT(YEAR FROM timestamp) AS year
      FROM pairresult_backup_with_timestamp
      WHERE ((resultname =
             'CWronglyRemoved' AND
             value > 0) or
             (resultname =
             'MWronglyRemovedPub' AND
             value > 0))) AS foo
        WHERE year BETWEEN 2006 and 2021
        GROUP BY (year)
         """.as[(String, String)]) // attention, this is unsafe SQL!

    val prefix: String = s"d"
    val filename = s"results/study/4b-$prefix-wrongrem.csv"

    val rows = twoTuplesToRows(result)
    println(writeCsvFile(filename, Array("year", "count_upgrades_with_wrong"), rows))
  }


  def four_btotcorr(): Unit = {

    val result = postgresInteractor.runAndWait(
      sql"""
     select year, COUNT(Distinct "jarTwoID") AS count_jars_with_deprecationtag FROM (
     SELECT * , EXTRACT(YEAR FROM timestamp) AS year
      FROM pairresult_backup_with_timestamp
      WHERE ((resultname =
             'CDeprecatedAndRemoved' AND
             value > 0) or
             (resultname =
             'MDeprecatedAndRemovedPub' AND
             value > 0))) AS foo
        WHERE year BETWEEN 2006 and 2021
        GROUP BY (year)
         """.as[(String, String)]) // attention, this is unsafe SQL!

    val prefix: String = s"d"
    val filename = s"results/study/4b-$prefix-cor-rem.csv"

    val rows = twoTuplesToRows(result)
    println(writeCsvFile(filename, Array("year", "count_upgrades_with_wrong"), rows))
  }





  def four_b_totdeprmajor(): Unit = {

    val result = postgresInteractor.runAndWait(
      sql"""
SELECT wrongrem.year,
       wrongrem.count_jars_with_deprecationtag AS wrong,
       totaldepr.count_jars_with_deprecationtag AS total_depr,
       count_with_correct_removals FROM
     (select year, COUNT(Distinct "jarTwoID") AS count_jars_with_deprecationtag FROM (
     SELECT * , EXTRACT(YEAR FROM timestamp) AS year
      FROM pairresult_backup_with_timestamp
      WHERE ((resultname =
             'CDeprecatedInPrev' AND
             value > 0) or
             (resultname =
             'MDeprecatedInPrevPub' AND
             value > 0))
             AND versionjump=0) AS foo
        WHERE year BETWEEN 2006 and 2021
        GROUP BY (year)) AS totaldepr

JOIN
        (select year, COUNT(Distinct "jarTwoID") AS count_jars_with_deprecationtag FROM (
     SELECT * , EXTRACT(YEAR FROM timestamp) AS year
      FROM pairresult_backup_with_timestamp
      WHERE ((resultname =
             'CWronglyRemoved' AND
             value > 0) or
             (resultname =
             'MWronglyRemovedPub' AND
             value > 0))
            AND versionjump=0) AS foo
        WHERE year BETWEEN 2006 and 2021
        GROUP BY (year)) AS wrongrem
        ON wrongrem.year=totaldepr.year

JOIN

        (select year, COUNT(Distinct "jarTwoID") AS count_with_correct_removals FROM (
     SELECT * , EXTRACT(YEAR FROM timestamp) AS year
      FROM pairresult_backup_with_timestamp
      WHERE ((resultname =
             'CDeprecatedAndRemoved' AND
             value > 0) or
             (resultname =
             'MDeprecatedAndRemovedPub' AND
             value > 0))
      AND versionjump=0) AS foo
        WHERE year BETWEEN 2006 and 2021
        GROUP BY (year)) AS corr

       ON corr.year=wrongrem.year
        ORDER BY wrongrem.year
         """.as[(String, String,String , String)]) // attention, this is unsafe SQL!

    val prefix: String = s"all"
    val filename = s"results/study/4b-$prefix-depr.csv"

    val rows = fourTuplesToRows(result)
    println(writeCsvFile(filename, Array("year", " wrong", "total_depr","count_with_correct_removals"), rows))

  }


  def four_b_totdeprm(): Unit = {

    val result = postgresInteractor.runAndWait(
      sql"""
SELECT wrongrem.year,
       wrongrem.count_jars_with_deprecationtag AS wrong,
       totaldepr.count_jars_with_deprecationtag AS total_depr,
       count_with_correct_removals FROM
     (select year, COUNT(Distinct "jarTwoID") AS count_jars_with_deprecationtag FROM (
     SELECT * , EXTRACT(YEAR FROM timestamp) AS year
      FROM pairresult_backup_with_timestamp
      WHERE ((resultname =
             'CDeprecatedInPrev' AND
             value > 0) or
             (resultname =
             'MDeprecatedInPrevPub' AND
             value > 0))) AS foo
        WHERE year BETWEEN 2006 and 2021
        GROUP BY (year)) AS totaldepr

JOIN
        (select year, COUNT(Distinct "jarTwoID") AS count_jars_with_deprecationtag FROM (
     SELECT * , EXTRACT(YEAR FROM timestamp) AS year
      FROM pairresult_backup_with_timestamp
      WHERE ((resultname =
             'CWronglyRemoved' AND
             value > 0) or
             (resultname =
             'MWronglyRemovedPub' AND
             value > 0))) AS foo
        WHERE year BETWEEN 2006 and 2021
        GROUP BY (year)) AS wrongrem
        ON wrongrem.year=totaldepr.year

JOIN

        (select year, COUNT(Distinct "jarTwoID") AS count_with_correct_removals FROM (
     SELECT * , EXTRACT(YEAR FROM timestamp) AS year
      FROM pairresult_backup_with_timestamp
      WHERE ((resultname =
             'CDeprecatedAndRemoved' AND
             value > 0) or
             (resultname =
             'MDeprecatedAndRemovedPub' AND
             value > 0))) AS foo
        WHERE year BETWEEN 2006 and 2021
        GROUP BY (year)) AS corr

       ON corr.year=wrongrem.year
        ORDER BY wrongrem.year
         """.as[(String, String,String , String)]) // attention, this is unsafe SQL!

    val prefix: String = s"all"
    val filename = s"results/study/4b-$prefix-depr.csv"

    val rows = fourTuplesToRows(result)
    println(writeCsvFile(filename, Array("year", " wrong", "total_depr","count_with_correct_removals"), rows))

  }








}
