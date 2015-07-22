package jarekprzygodzki.tools.jarversion

import java.io._
import java.util.zip._

import resource._
import scopt.OptionParser

case class ClassVersion(major: Int, minor: Int) extends Ordered[ClassVersion] {

  import scala.math.Ordered.orderingToOrdered

  override def compare(that: ClassVersion): Int = (major, minor) compare(that.major, that.minor)
}

class ZipInputStreamEntryIterator(zip: ZipInputStream) extends Iterator[(ZipEntry, InputStream)] {

  var entry: ZipEntry = null

  override def hasNext: Boolean = {
    entry = zip.getNextEntry
    entry != null
  }

  override def next(): (ZipEntry, InputStream) = (entry, zip)
}

object JarUtils {

  /**
   * Return jar file version number defined as the  maximum version of all classes contained within jar file
   * @param jarFile
   * @return
   */
  def getJarVersion(jarFile: File): ClassVersion = {
    (for (f <- managed(new FileInputStream(jarFile)))  yield  {
      val versions = new ZipInputStreamEntryIterator(new ZipInputStream(f)).
        filter  { case (entry, _) => entry.getName.endsWith(".class") }.
        flatMap { case (_, zip) => getClassVersion(zip) }
      versions.max
    }).opt.get
  }

  def getClassVersion(classInputStream: InputStream): Option[ClassVersion] = {
    val ds = new DataInputStream(classInputStream)
    val magic = ds.readInt()
    val (minor, major) = (ds.readUnsignedShort(), ds.readUnsignedShort())
    val magicHeader = 0xCAFEBABE
    if (magic == magicHeader) Some(new ClassVersion(major, minor)) else None
  }
}

case class Config(jars: Seq[File] = Seq(), verbose: Boolean = false)


object App {

  def main(args: Array[String]): Unit = {

    val parser = new OptionParser[Config]("jarversion") {
      head("jarversion", "1.x")
      opt[Seq[File]]('j', "jars") required() valueName("<jar1>,<jar2>...") action { (x,c) =>
        c.copy(jars = x) } text("jars to include")
      opt[Unit]("verbose") action { (_, c) =>
        c.copy(verbose = true)
      } text ("be verbose")
      help("help") text ("print usage text")
      note("Print jar file version number defined as the maximum version of all classes contained within jar file")

      override def showUsageOnError = true
    }

    parser.parse(args, Config()) map { config =>
      config.jars.foreach { jarFile =>
        val ver = JarUtils.getJarVersion(jarFile)
        println(s"${jarFile.getCanonicalPath} = $ver")
      }
    } getOrElse {
      // arguments are bad, usage message will have been displayed
      System exit 1
    }
  }

}