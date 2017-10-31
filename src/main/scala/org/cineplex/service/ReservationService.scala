package org.cineplex.service

import akka.actor.ActorRef
import org.cineplex.model._
import org.cineplex.{domain, interpreter}

import scala.concurrent._
import cats.instances.future._
import cats.~>
import org.cineplex.domain.Service


/** Provide the behavior of seat reservation by composing the data from various data source*/
final class ReservationService(imdbSource: ActorRef,
                               screenInfo: ActorRef,
                               reservationSource: ActorRef) {
  private val actorRuntime: Service ~> Future = interpreter.actorInterpreter(imdbSource,screenInfo,reservationSource)
  /**
    * Asynchronously retrieve external data for Movie details and screening info,
    * once both are obtained, then insert/update the ReservationStatus in the data base
    *
    *  @param registration the MovieRegistration constructed by the routing DSL
    *  @param ec implicit variable required for Future execution
    *  @return Future of Boolean, contains true if update was successful, otherwise false
    *
    */
  def saveOrUpdate(registration: MovieRegistration)(implicit ec: ExecutionContext): Future[Boolean] = {
    domain.saveOrUpdate(registration).foldMap(actorRuntime)
  }

  /**
    * Asynchronously increase the count of reservedSeats based on the given request
    *
    *  @param request the ReservationRequest constructed by the routing DSL
    *  @param ec implicit variable required for Future execution
    *  @return Future of Boolean, contains true if increment was successful, otherwise false
    *
    */
  def add(request: ReservationRequest)(implicit ec: ExecutionContext): Future[Boolean] = {
    domain.reserve(request).foldMap(actorRuntime)
  }

  /**
    * Asynchronously obtain current ReservationRequest based on the given request
    *
    *  @param request the ReservationRequest constructed by the routing DSL
    *  @param ec implicit variable required for Future execution
    *  @return a Future of current reservation status of given request,
    *          returns Some(detail) if database does contain related information, otherwise None
    */
  def fetch(request: ReservationRequest)(implicit ec: ExecutionContext): Future[Option[ReservationStatus]] = {
    domain.fetchStatus(request).foldMap(actorRuntime)
  }
}
