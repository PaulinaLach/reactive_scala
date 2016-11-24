package com.lab5.notifications

import com.lab5.actors.Bid
import com.lab5.notifications.Notifier.NotificationPayload

sealed trait AuctionNotification extends NotificationPayload {
  def auctionTitle: String
}

case class NewOfferArrived(auctionTitle: String, newHighestOffer: Bid) extends AuctionNotification

case class EndedWithoutOffers(auctionTitle: String) extends AuctionNotification

case class EndedWithWinner(auctionTitle: String, winningOffer: Bid) extends AuctionNotification