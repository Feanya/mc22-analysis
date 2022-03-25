package model

class Versionjump() {}

case class Major() extends Versionjump {}
case class Minor() extends Versionjump {}
case class Patch() extends Versionjump {}
case class Other() extends Versionjump {}

