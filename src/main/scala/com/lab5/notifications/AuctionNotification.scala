package com.lab5.notifications

import akka.actor.ActorRef
import com.lab5.notifications.Notifier.NotificationPayload

sealed trait AuctionNotification extends NotificationPayload {
  def auctionTitle: String
}

case class NewOffer(auctionTitle: String, amount: Float, sender: ActorRef) extends AuctionNotification

case class EndedNoOffers(auctionTitle: String) extends AuctionNotification

case class EndedWithOffer(auctionTitle: String, amount: Float, sender: ActorRef) extends AuctionNotification