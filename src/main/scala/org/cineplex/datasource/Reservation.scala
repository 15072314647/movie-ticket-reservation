package org.cineplex.datasource

import org.cineplex.model._
import IMDB._
import Screening._

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}


/** Defines the behavior of seat reservation */
trait Reservation {

  /**
    * Asynchronously returns the reservation status from external database (noSql or SQL) given the composed key of
    * { imdbId, screenId }
    *
    *  @param imdbId the unique imdb id
    *  @param screenId the unique screen id
    *  @param ec implicit variable required for Future execution
    *  @return a Future of current reservation status of given movie and screening info,
    *          returns Some(detail) if database does contain related information, otherwise None
    */
  def get(imdbId: IMDBID, screenId: ScreenID)(implicit ec: ExecutionContext): Future[Option[ReservationStatus]]

  /**
    * Asynchronously insert or update the database,
    * given the composed key of { imdbId, screenId } and obtained movie details from other resources.
    *
    * Correctness and the uniqueness of data should be guaranteed by the database implementation, not by this system
    *
    *  @param imdbId the unique imdb id
    *  @param screenId the unique screen id
    *  @param ec implicit variable required for Future execution
    *  @return Future of Boolean, contains true if update was successful, otherwise false
    *
    */
  def put(imdbId: IMDBID, screenId: ScreenID, status: ReservationStatus)(implicit ec: ExecutionContext): Future[Boolean]

  /**
    * Asynchronously increase the count reservedSeats, given the composed key of { imdbId, screenId }.
    * However, Correctness and the uniqueness of this count should be guaranteed by the database implementation
    *
    *  @param imdbId the unique imdb id
    *  @param screenId the unique screen id
    *  @param ec implicit variable required for Future execution
    *  @return Future of Boolean, contains true if update was successful, otherwise false
    *
    */
  def reserveSeat(imdbId: IMDBID, screenId: ScreenID)(implicit ec: ExecutionContext): Future[Boolean]

}

/**
  * Implementing Reservation trait to mimic actual data base behavior
  * Note: the ACID behavior of any real database is not guaranteed by this implementation
  *
  * */
final class ReservationDemo() extends Reservation {
  type Key = (IMDBID, ScreenID)

  private val demoStorage: mutable.Map[Key, ReservationStatus] = mutable.Map.empty

  override def get(imdbId: IMDBID,
                   screenId: ScreenID)(implicit ec: ExecutionContext): Future[Option[ReservationStatus]] = {
    Future.successful(demoStorage.get((imdbId, screenId)))
  }

  override def put(imdbId: IMDBID,
                   screenId: ScreenID,
                   status: ReservationStatus)(implicit ec: ExecutionContext): Future[Boolean] = {
    this.get(imdbId, screenId) flatMap {
      case Some(oldStatus) =>
        val updatedStatus = oldStatus.copy(movieTitle = status.movieTitle, availableSeats = status.availableSeats)
        demoStorage.put((imdbId, screenId), updatedStatus)
        Future.successful(true)
      case None =>
        if (imdbId == "tt0111161" && screenId == "screen_123456")
          Future {
            demoStorage.put((imdbId, screenId), status)
            true
          }
        else {
          Future.successful(false)
        }
    }
  }

  override def reserveSeat(imdbId: IMDBID,
                           screenId: ScreenID)(implicit ec: ExecutionContext): Future[Boolean] = {
    demoStorage.get((imdbId, screenId)) match {
      case Some(status) =>
        val newStaus = if (status.reservedSeats < status.availableSeats) {
          status.copy(reservedSeats = status.reservedSeats + 1)
        } else {
          status
        }
        demoStorage.put((imdbId, screenId), newStaus)
        Future.successful(true)
      case None => Future.successful(false)
    }
  }
}

