SELECT nozero.resultname, sum_originaltime012, sum_alltime012, sum_originaltime34, sum_alltime34 FROM

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
ORDER BY sum_originaltime012