package model

/**
 * I guess it is possible to write this bidirectional conversion way more elegantly but I don't know how atm.
 */
class Versionjump() {
  def toInt: Int = {
    this match {
      case Major() => 0
      case Minor() => 1
      case Patch() => 2
      case MajorZeroMinor() => 3
      case MajorZeroPatch() => 4
      case Same() => 6
      case Other() => 5
    }
  }

  def intToVersionjump(int: Int): Versionjump = {
    int match {
      case 0 => Major()
      case 1 => Minor()
      case 2 => Patch()
      case 3 => MajorZeroMinor()
      case 4 => MajorZeroPatch()
      case 6 => Same()
      case _ => Other()
    }
  }
}

case class Major() extends Versionjump {}

case class Minor() extends Versionjump {}

case class Patch() extends Versionjump {}

/** According to rule 4 anything may happen in major version 0 (https://semver.org/#spec-item-4)
 * We'll still habe a look at them for comparison reasons */
case class MajorZeroMinor() extends Versionjump {}

case class MajorZeroPatch() extends Versionjump {}

/** Mischievous or confusing things happened here */
case class Other() extends Versionjump {}

case class Same() extends Versionjump {}
