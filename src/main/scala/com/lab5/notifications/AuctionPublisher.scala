package com.lab5.notifications

import akka.actor.Actor
import akka.actor.Actor.Receive
import com.lab5.actors.Bid

class AuctionPublisher extends Actor {
  override def receive: Receive = {
    case NewOfferArrived(title, Bid(newOffer, buyer)) =>
      println(s"New offer for auction $title: $newOffer by $buyer")
      sender() ! ReceivedNotification

    case EndedWithoutOffers(title) =>
      println(s"There were no offers for auction $title")
      sender() ! ReceivedNotification

    case EndedWithWinner(title, Bid(winningPrice, winner)) =>
      println(s"Buyer $winner won auction $title with price $winningPrice")
      sender() ! ReceivedNotification
  }
}
