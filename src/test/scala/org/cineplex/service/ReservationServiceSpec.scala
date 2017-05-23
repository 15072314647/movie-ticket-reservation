package org.cineplex.service

import org.cineplex.datasource.IMDB.MovieDetail
import org.cineplex.datasource._
import org.cineplex.model.{MovieRegistration, ReservationRequest, ReservationStatus}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest._

import scala.concurrent._

/**
  * Created by neo on 20/05/2017.
  */
class ReservationServiceSpec
  extends FlatSpec
    with Matchers
    with ScalaFutures
    with MockFactory {

  private val executionContext = scala.concurrent.ExecutionContext.Implicits.global
  private val imbdSource = mock[IMDB]
  private val screenInfo = mock[Screening]
  private val reservation = mock[Reservation]

  private val reservationService = new ReservationService(imbdSource, reservation, screenInfo)

  private val status = ReservationStatus("tt1122334", "screen_123456", "")

  behavior of "reservationService.fetch"

  it should "returns Some(detail) if the external sources does contain related information" in {

    (reservation.get(_: String, _: String)(_: ExecutionContext))
      .expects("tt1122334", "screen_123456", executionContext)
      .returning {
        Future.successful(Some(status))
      }

    val test1 = reservationService.fetch(ReservationRequest("tt1122334", "screen_123456"))(executionContext)

    whenReady(test1) { result =>
      assert(result.get.imdbId === "tt1122334")
      assert(result.get.screenId === "screen_123456")
      assert(result.get.movieTitle === "")
      assert(result.get.availableSeats === 1000)
      assert(result.get.reservedSeats === 0)
    }
  }

  it should "otherwise returns None" in {

    (reservation.get(_: String, _: String)(_: ExecutionContext))
      .expects("tt1122335", "screen_123456", executionContext)
      .returning {
        Future.successful(None)
      }

    val test2 = reservationService.fetch(ReservationRequest("tt1122335", "screen_123456"))(executionContext)

    whenReady(test2) { result =>
      assert(result === None)
    }

  }

  behavior of "reservationService.saveOrUpdate"

  it should "returns true if the update is successful" in {

    (imbdSource.getMovieDetail(_: String)(_: ExecutionContext))
      .expects("tt0111161", executionContext)
      .returning {
        Future.successful(Some(MovieDetail("tt0111161", "")))
      }

    (screenInfo.contains(_: String)(_: ExecutionContext))
      .expects("screen_123456", executionContext)
      .returning {
        Future.successful(true)
      }

    (reservation.put(_: String, _: String, _: ReservationStatus)(_: ExecutionContext))
      .expects("tt0111161", "screen_123456", *, executionContext)
      .returning {
        Future.successful(true)
      }

    val test1 = reservationService.saveOrUpdate(MovieRegistration("tt0111161", 1000, "screen_123456"))(executionContext)

    whenReady(test1) { result => assert(result) }
  }

  it should "otherwise returns false" in {

    (imbdSource.getMovieDetail(_: String)(_: ExecutionContext))
      .expects("tt0111168", executionContext)
      .returning {
        Future.successful(None)
      }

    (screenInfo.contains(_: String)(_: ExecutionContext))
      .expects("screen_123457", executionContext)
      .returning {
        Future.successful(false)
      }

    val test2 = reservationService.saveOrUpdate(MovieRegistration("tt0111168", 1000, "screen_123457"))(executionContext)

    whenReady(test2) { result => assert(!result) }

  }

  behavior of "reservationService.add"

  it should "returns true if the screening is scheduled" in {

    (reservation.reserveSeat(_: String, _: String)(_: ExecutionContext))
      .expects("tt1122334", "screen_123456", executionContext)
      .returning {
      Future.successful(true)
    }

    val test1 = reservationService.add(ReservationRequest("tt1122334", "screen_123456"))(executionContext)

    whenReady(test1) { result => assert(result) }
  }

  it should "otherwise returns false" in {

    (reservation.reserveSeat(_: String, _: String)(_: ExecutionContext))
      .expects("tt1122335", "screen_123456", executionContext)
      .returning {
      Future.successful(false)
    }

    val test2 = reservationService.add(ReservationRequest("tt1122335", "screen_123456"))(executionContext)

    whenReady(test2) { result => assert(!result) }

  }
}
