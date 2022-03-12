package model

import java.io.InputStream
import java.net.URL


/**
 * Used for identification (Pattern matching) of jar file
 *
 * @param is jar file stream
 */
case class JarFile(is: InputStream, url: URL)


/**
 * Used for identification (Pattern matching) of aar file
 *
 * @param is aar file stream
 */
case class AarFile(is: InputStream, url: URL)


class FileTypes {

}
