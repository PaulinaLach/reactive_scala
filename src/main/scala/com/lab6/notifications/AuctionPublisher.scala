package com.lab6.notifications

import akka.actor.Actor
import com.lab6.notifications.AuctionPublisher.NotificationReceived

class AuctionPublisher extends Actor {
  override def receive: Receive = {
    case NewOffer(title, amount, buyer) =>
      println(s"Got offer for auction $title: $amount by $buyer")
      sender ! NotificationReceived

    case EndedNoOffers(title) =>
      println(s"No offers for auction $title")
      sender ! NotificationReceived

    case EndedWithOffer(title, amount, winner) =>
      println(s"$winner won auction $title with price $amount")
      sender ! NotificationReceived
  }
}

object AuctionPublisher {
  case object NotificationReceived
}