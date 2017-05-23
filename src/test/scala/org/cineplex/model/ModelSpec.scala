package org.cineplex.model

import org.scalatest.{Matchers, WordSpec}

/**
  * Created by neo on 21/05/2017.
  */
class ModelSpec
  extends WordSpec
    with Matchers {

  "MovieRegistration" should {

    "not created with invalid imdbId " in {

      assertThrows[IllegalArgumentException] {
        MovieRegistration("", screenId = "screen_123456")
      }
    }

    "not created with invalid screenId" in {

      assertThrows[IllegalArgumentException] {
        MovieRegistration(imdbId = "tt1234567", screenId = "screen_1")
      }
    }

    "not exceed limits of availableSeats" in {

      assertThrows[IllegalArgumentException] {
        MovieRegistration(imdbId = "tt1234567", availableSeats = Int.MaxValue, screenId = "screen_123456")
      }

      assertThrows[IllegalArgumentException] {
        MovieRegistration(imdbId = "tt1234567", availableSeats = -1, screenId = "screen_123456")
      }
    }

    "be created with proper parameters" in {
      val model = MovieRegistration(imdbId = "tt1234567", screenId = "screen_123456")

      model.imdbId shouldEqual "tt1234567"
      model.availableSeats shouldEqual 1000
      model.screenId shouldEqual "screen_123456"
    }
  }

  "ReservationRequest" should {
    "not created with invalid imdbId " in {
      assertThrows[IllegalArgumentException] {
        ReservationRequest("", screenId = "screen_123456")
      }
    }

    "not created with invalid screenId" in {
      assertThrows[IllegalArgumentException] {
        ReservationRequest(imdbId = "tt1234567", screenId = "screen_1")
      }
    }

    "be created with proper parameters" in {
      val model = ReservationRequest(imdbId = "tt1234567", screenId = "screen_123456")

      model.imdbId shouldEqual "tt1234567"
      model.screenId shouldEqual "screen_123456"
    }
  }

  "ReservationStatus" should {
    "not created with invalid imdbId " in {
      assertThrows[IllegalArgumentException] {
        ReservationStatus(imdbId = "", "screen_123456", "")
      }
    }

    "not created with invalid screenId" in {
      assertThrows[IllegalArgumentException] {
        ReservationStatus("tt1234567", screenId = "screen_1", "")
      }
    }

    "not exceed limits of availableSeats" in {

      assertThrows[IllegalArgumentException] {
        ReservationStatus(imdbId = "tt1234567", screenId = "screen_123456", "", availableSeats = 100000)
      }

      assertThrows[IllegalArgumentException] {
        ReservationStatus(imdbId = "tt1234567", screenId = "screen_123456", "", availableSeats = -1)
      }
    }

    "not exceed limits of reservedSeats" in {

      assertThrows[IllegalArgumentException] {
        ReservationStatus(imdbId = "tt1234567", screenId = "screen_123456", "", availableSeats = 10, reservedSeats = 11)
      }

      assertThrows[IllegalArgumentException] {
        ReservationStatus(imdbId = "tt1234567", screenId = "screen_123456", "", reservedSeats = -1)
      }
    }

    "be created with proper parameters" in {
      val model = ReservationStatus(imdbId = "tt1234567", screenId = "screen_123456", "")

      model.imdbId shouldEqual "tt1234567"
      model.screenId shouldEqual "screen_123456"
      model.movieTitle shouldEqual ""
      model.availableSeats shouldEqual MAX_SEATING
      model.reservedSeats shouldEqual 0
    }
  }
}
