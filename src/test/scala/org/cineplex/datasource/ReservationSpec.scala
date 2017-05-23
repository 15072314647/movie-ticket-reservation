package org.cineplex.datasource


import org.cineplex.model.ReservationStatus
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by neo on 21/05/2017.
  */
class ReservationSpec
  extends FlatSpec
    with Matchers
    with ScalaFutures
    with MockFactory {

  private val executionContext = scala.concurrent.ExecutionContext.Implicits.global
  private val reservation = mock[Reservation]
  private val status = ReservationStatus("tt1122334", "screen_123456", "")


  behavior of "Reservation.get"

  it should "returns Some(detail) if the external source does contain related information" in {

    (reservation.get(_: String, _: String)(_: ExecutionContext))
      .expects("a", "b", executionContext)
      .returning {
        Future.successful(Some(status))
      }

    val test1 = reservation.get("a", "b")(executionContext)

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
      .expects("d", "e", executionContext)
      .returning {
        Future.successful(None)
      }
    val test2 = reservation.get("d", "e")(executionContext)

    whenReady(test2) { result =>
      assert(result === None)
    }

  }

  behavior of "Reservation.put"

  it should "returns true if the update is successful" in {

    (reservation.put(_: String, _: String, _: ReservationStatus)(_: ExecutionContext))
      .expects("a", "b", *, executionContext)
      .returning {
        Future.successful(true)
      }

    val test1 = reservation.put("a", "b", status)(executionContext)

    whenReady(test1) { result => assert(result) }
  }

  it should "otherwise returns false" in {

    (reservation.put(_: String, _: String, _: ReservationStatus)(_: ExecutionContext))
      .expects("e", "d", *, executionContext)
      .returning {
        Future.successful(false)
      }

    val test2 = reservation.put("e", "d", status)(executionContext)

    whenReady(test2) { result => assert(!result) }

  }
  behavior of "Reservation.reserveSeat"

  it should "returns true if the screening is scheduled" in {

    (reservation.reserveSeat(_: String, _: String)(_: ExecutionContext))
      .expects("b", "b", executionContext)
      .returning {
        Future.successful(true)
      }

    val test1 = reservation.reserveSeat("b", "b")(executionContext)

    whenReady(test1) { result => assert(result) }
  }

  it should "otherwise returns false" in {

    (reservation.reserveSeat(_: String, _: String)(_: ExecutionContext))
      .expects("d", "d", executionContext)
      .returning {
        Future.successful(false)
      }

    val test2 = reservation.reserveSeat("d", "d")(executionContext)

    whenReady(test2) { result => assert(!result) }

  }

}
