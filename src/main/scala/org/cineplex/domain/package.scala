package org.cineplex

import model._
import cats.free._
import cats.free.Free._

package object domain {

  sealed trait Service[A]

  type ServiceF[A] = Free[Service, A]

  sealed trait IMDB[A] extends Service[A]

  sealed trait Screening[A] extends Service[A]

  sealed trait Reservation[A] extends Service[A]

  final case class GetIMDB(imdbId: IMDBID) extends IMDB[Option[MovieDetail]]

  final case class Contains(screenId: ScreenID) extends Screening[Boolean]

  final case class GetReservation(request: ReservationRequest) extends Reservation[Option[ReservationStatus]]

  final case class PutReservation(request: ReservationRequest, status: ReservationStatus) extends Reservation[Boolean]

  final case class ReserveSeat(request: ReservationRequest) extends Reservation[Boolean]

  import Service._

  def saveOrUpdate(registration: MovieRegistration): ServiceF[Boolean] = {
    for {
      movieOption <- get(registration.imdbId)
      screeningScheduled <- contains(registration.screenId)
      registered <- if (movieOption.isDefined && screeningScheduled) {
        put(registration.imdbId,
          registration.screenId,
          ReservationStatus(registration.imdbId,
            registration.screenId,
            movieOption.get.movieTitle,
            registration.availableSeats))

      } else {
        Free.pure[Service, Boolean](false)
      }
    } yield registered

  }

  def reserve(request: ReservationRequest): ServiceF[Boolean] = reserveSeat(request)

  def fetchStatus(request: ReservationRequest): ServiceF[Option[ReservationStatus]] = get(request)

  object Service {

    def get(imdbId: IMDBID): ServiceF[Option[MovieDetail]] = liftF[Service, Option[MovieDetail]](GetIMDB(imdbId))

    def contains(screenId: ScreenID): ServiceF[Boolean] = liftF[Service, Boolean](Contains(screenId))

    def put(imdbId: IMDBID, screenId: ScreenID, status: ReservationStatus): ServiceF[Boolean] = liftF[Service, Boolean](PutReservation(ReservationRequest(imdbId, screenId), status))

    def reserveSeat(request: ReservationRequest): ServiceF[Boolean] = liftF[Service, Boolean](ReserveSeat(request))

    def get(request: ReservationRequest): ServiceF[Option[ReservationStatus]] = liftF[Service, Option[ReservationStatus]](GetReservation(request))

  }

}
