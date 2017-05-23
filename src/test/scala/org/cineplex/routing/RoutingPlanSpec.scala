package org.cineplex.routing

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.pattern.CircuitBreaker
import scala.concurrent.duration._
import org.cineplex.datasource._
import org.cineplex.model._
import org.cineplex.service.ReservationService
import org.scalatest._
import spray.json.DefaultJsonProtocol


class RoutingPlanSpec
  extends WordSpec
    with Matchers
    with ScalatestRouteTest
    with SprayJsonSupport
    with DefaultJsonProtocol {

  private val imbdSource = new IMDBDemo()
  private val screenInfo = new ScreeningDemo()
  private val reservation = new ReservationDemo()
  private val reservationService = new ReservationService(imbdSource, reservation, screenInfo)

  private implicit val reservationResponseFormat = jsonFormat5(ReservationStatus)

  /** adding CircuitBreaker to regulate external work load */
  private val breaker = new CircuitBreaker(system.scheduler,
    maxFailures = 5,
    callTimeout = 5.seconds,
    resetTimeout = 1.second
  )
  val routings = new RoutingPlan(reservationService, breaker)

  "The Reservation Service Routing " should {

    "returns a 304 status code for PUT requests with mis-matching movie details in `application/json` format " in {
      val movieDetail = HttpEntity(ContentTypes.`application/json`,
        """
          {
              "imdbId" : "tt0111162",
              "availableSeats" : 100,
              "screenId" : "screen_123456"
          } """)

      Put("/movie", movieDetail) ~> routings.movie ~> check {
        status shouldEqual StatusCodes.NotModified
      }

    }
    "returns a 200 status code for PUT requests with valid movie details in `application/json` format " in {
      val movieDetail = HttpEntity(ContentTypes.`application/json`,
        """
          {
              "imdbId" : "tt0111161",
              "availableSeats" : 100,
              "screenId" : "screen_123456"
          } """)

      Put("/movie", movieDetail) ~> routings.movie ~> check {
        status shouldEqual StatusCodes.OK
      }

    }

    "returns a 200 status code for PUT requests with valid movie details in `application/json` format" +
      "disregarding the parameters listed in URL path " in {
      val movieDetail = HttpEntity(ContentTypes.`application/json`,
        """
          {
              "imdbId" : "tt0111161",
              "availableSeats" : 100,
              "screenId" : "screen_123456"
          } """)

      Put("/movie?imdbId=tt&screenId=screen_12", movieDetail) ~> routings.movie ~> check {
        status shouldEqual StatusCodes.OK
      }

    }

    "will reject a PUT requests with invalid movie details in request body " in {
      val movieDetail = HttpEntity(ContentTypes.`application/json`,
        """
          {
              "imdbId" : "tt",
              "availableSeats" : 100,
              "screenId" : "screen_1256"
          } """)

      Put("/movie", movieDetail) ~> routings.movie ~> check {
        rejection.asInstanceOf[ValidationRejection].message shouldEqual "requirement failed: Invalid IMDB Id"
      }

    }

    "will reject a PUT requests with no request body " in {
      val movieDetail = HttpEntity(ContentTypes.`application/json`,
        """
          {
              "imdbId" : "tt",
              "availableSeats" : 100,
              "screenId" : "screen_1256"
          } """)

      Put("/movie") ~> routings.movie ~> check {
        rejection.asInstanceOf[MalformedRequestContentRejection].message should startWith ("Unexpected end-of-input")
      }
    }

    "returns a 200 status code with json for GET requests with the given imdb id and screen id" in {
      val reservationDetail = ReservationStatus(
        imdbId = "tt0111161",
        screenId = "screen_123456",
        movieTitle = "The Shawshank Redemption",
        availableSeats = 100)

      Get("/movie?imdbId=tt0111161&screenId=screen_123456") ~> routings.movie ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[ReservationStatus] shouldEqual reservationDetail
      }
    }


    "return a 200 status code for POST requests to increase seat reservation" in {
      val request = HttpEntity(ContentTypes.`application/json`,
        """
          {
            "imdbId" : "tt0111161",
            "screenId" : "screen_123456"
          } """)
      Post("/reservation", request) ~> routings.reservation ~> check {
        status shouldEqual StatusCodes.OK
      }
    }

    "returns the reservation status with increased seat reservation count from the last test" in {
      val reservationDetail = ReservationStatus(
        imdbId = "tt0111161",
        screenId = "screen_123456",
        movieTitle = "The Shawshank Redemption",
        availableSeats = 100,
        reservedSeats = 1
      )
      Get("/movie?imdbId=tt0111161&screenId=screen_123456") ~> routings.movie ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[ReservationStatus] shouldEqual reservationDetail
      }
    }
  }

}
