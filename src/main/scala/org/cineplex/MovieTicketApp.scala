package org.cineplex

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.CircuitBreaker
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import org.cineplex.datasource._
import org.cineplex.routing.RoutingPlan
import org.cineplex.service.ReservationService

import scala.concurrent.duration._
import scala.io.StdIn
import scala.util.Failure

/**
  * Created by neo on 19/05/2017.
  */
object MovieTicketApp extends App {

  implicit val system = ActorSystem("movie-ticket")
  implicit val executor = system.dispatcher
  implicit val log = Logging(system, getClass)
  implicit val materializer = ActorMaterializer()


  val config = ConfigFactory.load()

  val httpConfig = config.getConfig("http")

  val imbdSource = new IMDBDemo()
  val screenInfo = new ScreeningDemo()
  val reservation = new ReservationDemo()
  val reservationService = new ReservationService(imbdSource, reservation, screenInfo)

  /** adding CircuitBreaker to regulate external work load */
  val breaker = new CircuitBreaker(system.scheduler,
    maxFailures = 5,
    callTimeout = 5.seconds,
    resetTimeout = 1.second
  )

  val routings = new RoutingPlan(reservationService, breaker)

  val handler: Route = routings.movie ~ routings.reservation

  val (httpHost, httpPort) = (httpConfig.getString("host"), httpConfig.getInt("port"))

  val bindingFuture = Http().bindAndHandle(handler, httpHost, httpPort)

  StdIn.readLine() // let it run until user presses return

  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete {
    case Failure(ex) =>
      log.error(ex, "Failed to bind to {}:{}!", httpHost, httpPort)
      system.terminate()
    case _ => system.terminate()
  } // and shutdown when done

}