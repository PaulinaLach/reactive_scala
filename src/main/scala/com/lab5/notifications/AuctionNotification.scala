package com.lab5.notifications

import akka.actor.ActorRef
import com.lab5.notifications.Notifier.NotificationPayload

sealed trait AuctionNotification extends NotificationPayload {
  def auctionTitle: String
}

case class NewOfferArrived(auctionTitle: String, newHighestOffer: Float, sender: ActorRef) extends AuctionNotification

case class EndedWithoutOffers(auctionTitle: String) extends AuctionNotification

case class EndedWithWinner(auctionTitle: String, winningOffer: Float, sender: ActorRef) extends AuctionNotification