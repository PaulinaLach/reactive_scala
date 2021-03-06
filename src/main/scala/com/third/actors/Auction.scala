package com.third.actors

import akka.actor.{ActorRef, FSM}

import scala.concurrent.duration._

case class Bid(amount: Float)
case class WinAuction(amount: Float, i: Int)
case object Relist
case object BidTimer
case object DeleteTimer
case object InitializeAuction
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
case class AuctionData(bidValue: Float, winner: ActorRef) extends Data

class Auction(val i: Int) extends FSM[State, Data] {
  startWith(InitialState, NotInitialized)

  when(InitialState) {
    case Event(InitializeAuction, NotInitialized) =>
      goto(Created) using NotInitialized
  }

  when(Created, stateTimeout = 10 seconds) {
    case Event(StateTimeout, NotInitialized) =>
      println(s"Auction $i -> ignored")
      goto(Ignored) using NotInitialized

    case Event(Bid(amount), NotInitialized) =>
      var auctionData = AuctionData(0.0f, sender)
      auctionData = bidAuction(auctionData, amount)
      goto(Activated) using auctionData
    case Event(_, NotInitialized) =>
      sender ! FailedBid
      stay using NotInitialized
  }

  when(Ignored, stateTimeout = 20 seconds) {
    case Event(Relist, NotInitialized) =>
      println(s"Auction $i -> relist")
      goto(Created) using NotInitialized
    case Event(DeleteTimer, NotInitialized) =>
      println(s"Auction $i -> delete")
      stay using NotInitialized
  }

  when(Activated, stateTimeout = 10 seconds) {
    case Event(Bid(amount), auctionData: AuctionData) if auctionData.bidValue < amount =>
      val newAuctionData = bidAuction(auctionData, amount)
      stay using newAuctionData
    case Event(StateTimeout, auctionData: AuctionData) =>
      println(s"Auction $i -> sold for ${auctionData.bidValue}")
      auctionData.winner ! WinAuction(auctionData.bidValue, i)
      goto(Sold) using auctionData
    case Event(_, auctionData: AuctionData) =>
      sender ! FailedBid
      stay using auctionData
  }

  when(Sold, stateTimeout = 10 seconds) {
    case Event(StateTimeout, auctionData: AuctionData) =>
      println(s"Auction $i -> delete")
      stay using auctionData
  }

  private def bidAuction(auctionData: AuctionData, amount: Float): AuctionData = {
    println(s"auction $i - previous amount: ${auctionData.bidValue} new amount: $amount")
    sender ! SuccessfulBid(amount)
    AuctionData(amount, sender)
  }
}
