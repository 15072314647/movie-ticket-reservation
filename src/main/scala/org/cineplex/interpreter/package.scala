package org.cineplex

import akka.actor.ActorRef
import org.cineplex.domain._
import org.cineplex.model._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import cats.~>

import scala.concurrent.Future

package object interpreter {
  implicit val timeout = Timeout(5 seconds)
  import scala.concurrent.ExecutionContext.Implicits.global
   def actorInterpreter(imdb: ActorRef, screening: ActorRef, reservation: ActorRef): Service ~> Future = new (Service ~> Future) {
    def apply[A](i: Service[A]): Future[A] = i match {
      case getDetail@GetIMDB(imdbId: IMDBID) => (imdb ? getDetail).map(_.asInstanceOf[A])
      case contains@Contains(screenId: ScreenID) => (screening ? contains).map(_.asInstanceOf[A])
      case get@GetReservation(request: ReservationRequest) => (reservation ? get).map(_.asInstanceOf[A])
      case put@PutReservation(request: ReservationRequest, status: ReservationStatus) => (reservation ? put).map(_.asInstanceOf[A])
      case reserve@ReserveSeat(request: ReservationRequest) => (reservation ? reserve).map(_.asInstanceOf[A])
    }
  }
}
