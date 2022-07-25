package io.fitcentive.social.domain.location

import org.neo4j.driver.{Value, Values}
import play.api.libs.json.{Json, Reads, Writes}

case class Coordinates(latitude: Double, longitude: Double) {
  import Coordinates._

  def toNeo4jPoint: Value =
    Values.point(SRID, longitude, latitude)
}

object Coordinates {

  // points in geographic wgs84 coordinates (epsg:4326)
  val SRID = 4326

  def fromString(pointString: String): Coordinates = {
    val mainSection: List[String] = pointString.drop(6).dropRight(1).split(" ").toList
    mainSection match {
      case latitude :: longitude :: Nil => Coordinates(latitude.toDouble, longitude.toDouble)
      case _                            => throw new IllegalStateException("Bad string coordinates received")
    }
  }

  implicit lazy val reads: Reads[Coordinates] = Json.reads[Coordinates]
  implicit lazy val writes: Writes[Coordinates] = Json.writes[Coordinates]
}
