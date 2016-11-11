package com.lab2first.actors

import akka.actor.{ActorRef, FSM}

import scala.concurrent.duration._

case class Bid(amount: Float)
case class WinAuction(amount: Float, i: String)
case object Relist
case object BidTimer
case object DeleteTimer
case class InitializeAuction(title: String)
case class SuccessfulBid(amount: Float)
case object FailedBid

sealed trait State
case object InitialState extends State
case object Created extends State
case object Ignored extends State
case object Activated extends State
case object Sold extends State

sealed trait Data
case object NotInitialized extends Data
case class Initialized(title: String, seller: ActorRef) extends Data
case class HasBeenBid(title: String, bidValue: Float, seller: ActorRef, winner: ActorRef) extends Data

class Auction extends FSM[State, Data] {
  startWith(InitialState, NotInitialized)

  when(InitialState) {
    case Event(InitializeAuction(title), NotInitialized) =>
      context.actorSelection("/user/auctionSearch") ! SubscribeToSearch(title)
      goto(Created) using Initialized(title, sender)
  }

  when(Created, stateTimeout = 10 seconds) {
    case Event(StateTimeout, initialized: Initialized) =>
      println(s"Auction ${self.toString()} -> ignored")
      goto(Ignored) using initialized

    case Event(Bid(amount), initialized: Initialized) =>
      var auctionData = HasBeenBid(initialized.title, 0.0f, initialized.seller, sender)
      auctionData = bidAuction(auctionData, amount)
      goto(Activated) using auctionData
    case Event(_, initialized: Initialized) =>
      sender ! FailedBid
      stay using initialized
  }

  when(Ignored, stateTimeout = 20 seconds) {
    case Event(Relist, initialized: Initialized) =>
      println(s"Auction ${self.toString()} -> relist")
      goto(Created) using initialized
    case Event(DeleteTimer, initialized: Initialized) =>
      println(s"Auction ${self.toString()} -> delete")
      stay using initialized
  }

  when(Activated, stateTimeout = 10 seconds) {
    case Event(Bid(amount), auctionData: HasBeenBid) if auctionData.bidValue < amount =>
      val newAuctionData = bidAuction(auctionData, amount)
      stay using newAuctionData
    case Event(StateTimeout, auctionData: HasBeenBid) =>
      println(s"Auction ${self.toString()} -> sold for ${auctionData.bidValue}")
      auctionData.seller ! WinAuction(auctionData.bidValue, self.toString())
      auctionData.winner ! WinAuction(auctionData.bidValue, self.toString())
      goto(Sold) using auctionData
    case Event(_, auctionData: HasBeenBid) =>
      sender ! FailedBid
      stay using auctionData
  }

  when(Sold, stateTimeout = 10 seconds) {
    case Event(StateTimeout, auctionData: HasBeenBid) =>
      println(s"Auction ${self.toString()} -> delete")
      stay using auctionData
  }

  override def preStart(): Unit = {
    println("Auction started")
  }

  private def bidAuction(auctionData: HasBeenBid, amount: Float): HasBeenBid = {
    println(s"auction ${self.toString()} - previous amount: ${auctionData.bidValue} new amount: $amount")
    sender ! SuccessfulBid(amount)
    if (sender != auctionData.winner) auctionData.winner ! NotificateBided(amount)
    HasBeenBid(auctionData.title, amount, auctionData.seller, sender)
  }
}
