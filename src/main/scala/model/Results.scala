package model

/**
 * Class representing the result of metrics calculations for a given file or directory. As an analysis
 * may calculate multiple metrics values for a single file / directory, it contains a list of
 * MetricValues.
 *
 * @param analysisName Name of the analysis that produced this result
 * @param jarFileOne   / Two JAR files that has been analyzed
 * @param success      Boolean indicating the success of all calculations involved
 * @param results      Iterable of Results for the files
 */
case class LibraryResult(analysisName: String,
                         groupid: String,
                         artifactname: String,
                         success: Boolean,
                         results: Iterable[Result])

object LibraryResult {
  /**
   * Generates a LibraryResult for a failed analysis.
   *
   * @return MetricsResult with success set to false, and an empty List of MetricValues
   */
  def analysisFailed(analysisName: String, groupid: String, artifactname: String): LibraryResult =
    LibraryResult(analysisName, groupid, artifactname, success = false, List())
}

case class Result(name: String, value: Int)


/**
 * Class representing the result of metrics calculations for a given file or directory. As an analysis
 * may calculate multiple metrics values for a single file / directory, it contains a list of
 * MetricValues.
 *
 * @param analysisName Name of the analysis that produced this result
 * @param jarOneID     , jarTwoID   / Database ID of the JAR files that have been analyzed
 * @param success      Boolean indicating the success of all calculations involved
 * @param results      Iterable of Results for the files
 */
case class PairResult(analysisName: String,
                      jarOneInfo: JarInfo,
                      jarTwoInfo: JarInfo,
                      versionjump: Versionjump,
                      success: Boolean,
                      results: Iterable[Result])

object PairResult {
  /**
   * Generates a PairResult for a failed analysis.
   *
   * @return MetricsResult with success set to false, and an empty List of MetricValues
   */
  def analysisFailed(analysisName: String, jarOneInfo: JarInfo, jarTwoInfo: JarInfo): PairResult =
    PairResult(analysisName, jarOneInfo, jarTwoInfo, Other(), success = false, List())
}
