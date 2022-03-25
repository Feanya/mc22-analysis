package model

import java.io.File

/**
 * Class representing the result of metrics calculations for a given file or directory. As an analysis
 * may calculate multiple metrics values for a single file / directory, it contains a list of
 * MetricValues.
 *
 * @param analysisName Name of the analysis that produced this result
 * @param jarFileOne / Two JAR files that has been analyzed
 * @param success Boolean indicating the success of all calculations involved
 * @param results Iterable of Results for the files
 */
case class PairResult (analysisName: String, jarFileOne: File, jarFileTwo: File, success: Boolean, results: Iterable[TwoJarResult])

object PairResult {
  /**
   * Generates a PairResult for a failed analysis.
   * @param jarFileOne JAR file or directory for which the analysis has failed
   * @param jarFileTwo Comparison goes 1→2
   * @return MetricsResult with success set to false, and an empty List of MetricValues
   */
  def analysisFailed(analysisName: String, jarFileOne: File, jarFileTwo: File): PairResult =
    PairResult(analysisName, jarFileOne, jarFileTwo, success = false, List())
}

/**
 *
 * @param jarNameOne Entity analysis was operated on. Usually a jar.
 * @param jarNameTwo Comparison goes 1→2
 * @param metricName Name of the metric that was calculated here
 * @param value Value calculated for this entity
 */

case class TwoJarResult(jarNameOne: String, jarNameTwo: String, metricName: String, value: Double)
