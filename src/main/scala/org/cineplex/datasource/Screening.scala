package org.cineplex.datasource

import org.cineplex.datasource.Screening.ScreenID

import scala.concurrent.{ExecutionContext, Future}

/** Defines the behavior of Screening information data source */
trait Screening {

  /**
    * Asynchronously returns existence of the given screen id.
    *
    * @param screenId the unique screen id the system is validate from external data source
    * @param ec implicit variable required for Future execution
    * @return Future of Boolean, contains true if the screening is scheduled, otherwise false
    */
  def contains(screenId: ScreenID)(implicit ec: ExecutionContext): Future[Boolean]

}

/** Implementing Screening trait to mimic external data source */
final class ScreeningDemo() extends Screening {

  override def contains(screenId: ScreenID)(implicit ec: ExecutionContext): Future[Boolean] = {
    Future.successful(screenId == "screen_123456")  //only use the screenID given in the assignment for testing purpose
  }
}

object Screening {

  type ScreenID = String

  /**
    *  the screen id validation is reduced to the one given in the assignment,
    *  while the actual validation for such information should be defined differently as per system required.
    **/

  val SCREENID_REGEX = """screen_\d{6}"""

}