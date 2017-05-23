package org.cineplex.routing

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.CircuitBreaker
import org.cineplex.model._
import org.cineplex.service.ReservationService
import spray.json.DefaultJsonProtocol

import scala.concurrent.ExecutionContext

import scala.util.{Failure, Success}

/**
  * Route builder with Json marshalling/unmarshalling support
  */
final class RoutingPlan(service: ReservationService,
                        breaker: CircuitBreaker)
  extends SprayJsonSupport
    with DefaultJsonProtocol {

  private implicit val reservationRequestFormat = jsonFormat2(ReservationRequest)

  private implicit val reservationResponseFormat = jsonFormat5(ReservationStatus)

  private implicit val movieRequestFormat = jsonFormat3(MovieRegistration)


  /**
    * Request URL: PUT http://<host>/movie
    * with following 'application/json' as the body
    * {
    * "imdbId": "tt0111161",
    * "availableSeats": 100,
    * "screenId": "screen_123456"
    * }
    *
    * Response contains HTTP Status only.
    *
    * 200 if new movie registered or the details of the movie updated,
    * 304 if nothing changed, could be caused by missing external information or other error.
    *
    **/
  private def putMovie(implicit ec: ExecutionContext): Route = put {
    entity(as[MovieRegistration]) { movie =>
      val createMovieRegistration = service.saveOrUpdate(movie)
      onCompleteWithBreaker(breaker)(createMovieRegistration) {
        case Success(value) => value match {
          case true => complete(StatusCodes.OK)
          case false => complete(StatusCodes.NotModified)
        }
        case Failure(ex) =>
          extractLog { log =>
            log.debug(s"An error occurred: ${ex.getMessage}")
            complete(StatusCodes.InternalServerError)
          }
      }
    }
  }

  /**
    * Request URL: GET http://<host>/movie?imdbId=tt0111161&screenId=screen_123456
    * with no HTTP body attached.
    *
    * Response contains HTTP Status only.
    *
    * 200 if movie information is registered or seating informaton is updated.
    * 404 if no movie information is registered previously in the system
    *
    **/
  private def getMovie(implicit ec: ExecutionContext): Route = get {
    parameters('imdbId.as[String], 'screenId.as[String]) { (imdbId, screenId) =>
      val request = ReservationRequest(imdbId, screenId)
      val maybeItem = service.fetch(request)
      onCompleteWithBreaker(breaker)(maybeItem) {
        case Success(value) => value match {
          case Some(item) => complete(StatusCodes.OK, item)
          case None => complete(StatusCodes.NotFound)
        }
        case Failure(ex) =>
          extractLog { log =>
            log.debug(s"An error occurred: ${ex.getMessage}")
            complete(StatusCodes.InternalServerError)
          }
      }
    }
  }

  /** Compose the put and get routes defined above to work with the "http://<host>/movie" path */
  def movie(implicit ec: ExecutionContext): Route = path("movie") {
    putMovie ~ getMovie
  }

  /**
    * Request URL: POST  http://<host>/reservation
    * with following 'application/json' as the body
    * {
    * "imdbId": "tt0111161",
    * "screenId": "screen_123456"
    * }
    *
    * Response contains HTTP Status only.
    *
    * 200 if movie information is registered or seating informaton is updated.
    * 404 if no movie information is registered previously in the system
    *
    **/
  def reservation(implicit ec: ExecutionContext): Route = path("reservation") {
    post {
      entity(as[ReservationRequest]) { requestOrder =>
        val saved = service.add(requestOrder)
        onCompleteWithBreaker(breaker)(saved) {
          case Success(value) => value match {
            case true => complete(StatusCodes.OK)
            case false => complete(StatusCodes.Forbidden)
          }
          case Failure(ex) =>
            extractLog { log =>
              log.debug(s"An error occurred: ${ex.getMessage}")
              complete(StatusCodes.InternalServerError)
            }
        }
      }
    }
  }

}

