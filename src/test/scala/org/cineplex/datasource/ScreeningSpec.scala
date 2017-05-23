package org.cineplex.datasource

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures

import org.scalatest._
import scala.concurrent._

/**
  * Created by neo on 21/05/2017.
  */
class ScreeningSpec
  extends FlatSpec
    with Matchers
    with ScalaFutures
    with MockFactory {

  private val executionContext = scala.concurrent.ExecutionContext.Implicits.global
  private val mockScreening = mock[Screening]

  behavior of "Screening.contains"

  it should "returns true if the screening is scheduled" in {

    (mockScreening.contains(_: String)(_: ExecutionContext))
      .expects("screen_123456", executionContext)
      .returning {
        Future.successful(true)
      }

    val test1 = mockScreening.contains("screen_123456")(executionContext)

    whenReady(test1) { result => assert(result) }
  }

  it should "otherwise returns false" in {

    (mockScreening.contains(_: String)(_: ExecutionContext))
      .expects(" ", executionContext)
      .returning {
        Future.successful(false)
      }

    val test2 = mockScreening.contains(" ")(executionContext)

    whenReady(test2) { result => assert(!result) }

  }
}
