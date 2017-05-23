package org.cineplex.service

import org.cineplex.model._
import org.cineplex.datasource._
import scala.concurrent._

/** Provide the behavior of seat reservation by composing the data from various data source*/
final class ReservationService(imdbSource: IMDB,
                               reservationSource: Reservation,
                               screenInfo: Screening) {
  /**
    * Asynchronously retrieve external data for Movie details and screening info,
    * once both are obtained, then insert/update the ReservationStatus in the data base
    *
    *  @param registration the MovieRegistration constructed by the routing DSL
    *  @param ec implicit variable required for Future execution
    *  @return Future of Boolean, contains true if update was successful, otherwise false
    *
    */
  def saveOrUpdate(registration: MovieRegistration)(implicit ec: ExecutionContext): Future[Boolean] = {

    val movieDetailF = imdbSource.getMovieDetail(registration.imdbId)
    val screeningDetailF = screenInfo.contains(registration.screenId)

    (for {
      movie <- movieDetailF
      screeningScheduled <- screeningDetailF
    } yield {
      if (movie.isDefined && screeningScheduled) {
        movie map { movieInfo =>
          ReservationStatus(
            registration.imdbId,
            registration.screenId,
            movieInfo.movieTitle,
            registration.availableSeats
          )
        }
      } else {
        None
      }
    }).flatMap {
      case Some(status) => reservationSource.put(status.imdbId, status.screenId, status)
      case None => Future.successful(false)
    }

  }

  /**
    * Asynchronously increase the count of reservedSeats based on the given request
    *
    *  @param request the ReservationRequest constructed by the routing DSL
    *  @param ec implicit variable required for Future execution
    *  @return Future of Boolean, contains true if increment was successful, otherwise false
    *
    */
  def add(request: ReservationRequest)(implicit ec: ExecutionContext): Future[Boolean] = {
    reservationSource.reserveSeat(request.imdbId, request.screenId)
  }

  /**
    * Asynchronously obtain current ReservationRequest based on the given request
    *
    *  @param request the ReservationRequest constructed by the routing DSL
    *  @param ec implicit variable required for Future execution
    *  @return a Future of current reservation status of given request,
    *          returns Some(detail) if database does contain related information, otherwise None
    */
  def fetch(request: ReservationRequest)(implicit ec: ExecutionContext): Future[Option[ReservationStatus]] = {
    reservationSource.get(request.imdbId, request.screenId)
  }
}
