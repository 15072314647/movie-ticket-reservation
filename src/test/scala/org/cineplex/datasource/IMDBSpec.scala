package org.cineplex.datasource

import org.cineplex.datasource.IMDB.MovieDetail
import org.scalamock.scalatest.MockFactory
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent._

/**
  * Created by neo on 21/05/2017.
  */
class IMDBSpec
  extends FlatSpec
    with Matchers
    with ScalaFutures
    with MockFactory {

  private val executionContext = scala.concurrent.ExecutionContext.Implicits.global
  private val mockIMDB = mock[IMDB]

  behavior of "mockIMDB.getMovieDetail"

  it should "returns Some(detail) if the external source does contain related information" in {

    (mockIMDB.getMovieDetail(_: String)(_: ExecutionContext))
      .expects("tt0111161", executionContext)
      .returning {
        Future.successful(Some(MovieDetail("tt0111161", "")))
      }

    val test1 = mockIMDB.getMovieDetail("tt0111161")(executionContext)

    whenReady(test1) { result =>
      assert(result.get.imdbId === "tt0111161")
    }
  }

  it should "otherwise returns None" in {

    (mockIMDB.getMovieDetail(_: String)(_: ExecutionContext))
      .expects(" ", executionContext)
      .returning {
        Future.successful(None)
      }
    val test2 = mockIMDB.getMovieDetail(" ")(executionContext)

    whenReady(test2) { result =>
      assert(result === None)
    }

  }
}
