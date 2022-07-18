package evaluation

import evaluation.Filter.{postgresInteractor, threeTuplesToRows, writeCsvFile}
import slick.jdbc.PostgresProfile.api._

class Datasetpaper {
//  SELECT COUNT(*) AS count_all FROM data;
//  SELECT COUNT(*) AS count_primary FROM primary_data;
//  SELECT SUM(count) AS count_primary_with_other_table FROM aggregated_ga; -- gleiches Ergebnis, nur anders
//    SELECT COUNT(*) AS count_primary_12 FROM primary_data WHERE (versionscheme = 1 OR versionscheme = 2);
//  SELECT COUNT(*) AS count_visited FROM allids_with_timestamp;
//  SELECT COUNT(*) AS count_visited_2006to2021 FROM allids_with_timestamp
//  WHERE timestamp between '2006-01-01' AND '2022-01-01';
//  SELECT COUNT(*) AS c, classifier FROM data GROUP BY classifier ORDER BY c DESC;
//
//  SELECT COUNT(*) AS c, classifier FROM data GROUP BY classifier ORDER BY c DESC LIMIT 4;
//
//  SELECT SUM(count_topfour_classifier) FROM
//    (SELECT COUNT(*) AS count_topfour_classifier, classifier
//      FROM data GROUP BY classifier ORDER BY count_topfour_classifier DESC LIMIT 4) AS counttopfcl;
//
//  SELECT SUM(count_topeight_classifier) FROM
//    (SELECT COUNT(*) AS count_topeight_classifier, classifier
//      FROM data GROUP BY classifier ORDER BY count_topeight_classifier DESC LIMIT 8) AS counttopcl;
//
//  SELECT COUNT(*) AS count_all_jars FROM data;
//
//  SELECT * FROM
//  (SELECT COUNT(*) AS count_j,versionscheme  FROM data GROUP BY versionscheme) AS j
//  JOIN
//  (SELECT COUNT(*) AS count_a,versionscheme FROM primary_data GROUP BY versionscheme) AS a
//  ON a.versionscheme=j.versionscheme;  -- j and v by version scheme
//
//  SELECT SUM(size) AS totalsize_j from data;
//  SELECT SUM(size) AS size, classifier FROM data GROUP BY classifier ORDER BY size DESC LIMIT 8; -- sizes by classifier
//    SELECT SUM(size) AS totalsize_a FROM (SELECT size FROM allids_with_timestamp JOIN data d on allids_with_timestamp.ids = d.id) AS foo;
//  SELECT SUM(size) AS size_s FROM data WHERE classifier = 'test-sources';
//  SELECT SUM(size) AS size_com FROM data WHERE classifier = 'sources-commercial';
//  SELECT SUM(size) AS size_res FROM data WHERE classifier = 'resources';
//  SELECT COUNT(*) FROM allids_with_timestamp;
//
//  SELECT COUNT(*) AS emptysources FROM data WHERE classifier = 'sources' AND size < 200;
//  SELECT COUNT(*) AS emptyjavadocs FROM data WHERE classifier = 'javadoc' AND size < 200;
//
//
//  SELECT SUM(size) AS emptysources FROM data WHERE classifier = 'sources' AND size < 200;
//  SELECT SUM(size) AS emptyjavadocs FROM data WHERE classifier = 'javadoc' AND size < 200;
//
//  SELECT COUNT(*) AS count_all FROM data;
//  SELECT COUNT(*) AS count_primary FROM primary_data;
//  SELECT SUM(count) AS count_primary_with_other_table FROM aggregated_ga; -- gleiches Ergebnis, nur anders
//    SELECT COUNT(*) AS count_sources FROM data WHERE classifier='sources';
//  SELECT COUNT(*) AS count_primary_12 FROM primary_data WHERE (versionscheme = 1 OR versionscheme = 2);
//  SELECT COUNT(*) AS count_visited FROM allids_with_timestamp;
//  SELECT COUNT(*) AS count_a_visited_2006to2021 FROM allids_with_timestamp
//  WHERE timestamp between '2006-01-01' AND '2022-01-01';
//  SELECT COUNT(*) AS c, classifier FROM data GROUP BY classifier ORDER BY c DESC;
//
//  SELECT COUNT(*) AS count_ga_a FROM (
//    SELECT COUNT(*) AS ga_a_with_no_av FROM(
//      (SELECT ids FROM allids_with_timestamp
//        WHERE timestamp between '2006-01-01' AND '2022-01-01') AS eins
//    JOIN
//  (SELECT groupid, artifactname, id FROM data) AS zwei
//  ON eins.ids=zwei.id
//  )
//  GROUP BY groupid, artifactname) AS gas_a;
//
//  SELECT COUNT(*) AS count_ga_j FROM aggregated_ga;
//
//  SELECT COUNT(*) AS size_delta FROM upgrade_years
//  WHERE year between 2006 AND 2022;
//
//  SELECT COUNT(*) AS size_delta_forreal FROM
//    (SELECT p.id, EXTRACT(YEAR FROM d.timestamp) AS year
//  FROM pairresult_backup2 AS p
//    JOIN data d on d.id = p."jarTwoID"
//  WHERE resultname='CWronglyRemoved') AS pairs_with_years
//    WHERE year between 2006 AND 2022;
//
//  SELECT COUNT(DISTINCT groupid) AS count_g_a FROM data JOIN allids_with_timestamp ON id=allids_with_timestamp.ids
//  SELECT COUNT(DISTINCT groupid) AS count_g_j FROM data

  /**
   *
   */
  def reproductionstuff_jars_by_year(): Unit = {
    val result = postgresInteractor.runAndWait(
      sql"""SELECT originaltime.versionscheme, count_originaltime, count_alltime FROM

            (SELECT d.versionscheme, COUNT(*) AS count_originaltime
            FROM allids_with_timestamp
                     JOIN data d on ids = d.id
            WHERE allids_with_timestamp.timestamp  < '2011-07-31'
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
   *
   */
  def jars_by_year_in_j_and_a(): Unit = {
    val result = postgresInteractor.runAndWait(
      sql"""SELECT j.year AS year, jars_in_j, jars_in_a FROM
    (SELECT year, COUNT(*) AS jars_in_j FROM
        (SELECT EXTRACT(YEAR from timestamp) AS year FROM data) AS years
     GROUP BY year) AS j

        FULL OUTER JOIN

    (SELECT year, COUNT(*) AS jars_in_a FROM
        (SELECT EXTRACT(YEAR from timestamp) AS year
         FROM allids_with_timestamp
         WHERE timestamp BETWEEN '2006-01-01' AND '2022-01-01') AS years
     GROUP BY year) AS a

    ON j.year=a.year
    ORDER BY j.year""".as[(Int, Int, Int)]) // attention, this is unsafe SQL!

    val prefix: String = s"dataset/jars_per_year"
    val filename = s"results/$prefix.csv"

    val rows = threeTuplesToRows(result)
    println(writeCsvFile(filename, Array("year", "jars_in_j", "jars_in_a"), rows))

  }







}
