package model

class Versionjump()

case class Major() extends Versionjump
case class Minor() extends Versionjump
case class Patch() extends Versionjump

/** According to rule 4 anything may happen in major version 0 (https://semver.org/#spec-item-4) */
case class MajorZero() extends Versionjump

/** Mischievous or confusing things happened here */
case class Other() extends Versionjump
case class Same() extends Versionjump

