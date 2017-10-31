package org.cineplex.actors

import akka.actor.Actor
import org.cineplex.domain.Contains
import org.cineplex.model.ScreenID


class ScreeningActor extends Actor{
  override def receive: Receive = {
    case Contains(screenId: ScreenID) => sender ! {screenId == "screen_123456"}
  }
}