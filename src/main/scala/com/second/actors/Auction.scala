package com.second.actors

import akka.actor.{Actor, ActorRef, Cancellable}
import akka.event.LoggingReceive

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

case class Bid(amount: Float)
case class WinAuction(amount: Float, i: Int)
case object Relist
case object BidTimer
case object DeleteTimer
case object InitializeAuction
case class SuccessfulBid(amount: Float)
case object FailedBid

class Auction(val i: Int) extends Actor {
  var timer: Cancellable = null
  var bidValue = 0.0f
  var winner: ActorRef = null

  def receive: Receive = LoggingReceive {
    case InitializeAuction =>
      rescheduleOnce(10 seconds, BidTimer)
      context become created
  }

  def created: Receive = LoggingReceive {
    case BidTimer =>
      println(s"Auction $i -> ignored")
      rescheduleOnce(20 seconds, DeleteTimer)
      context become ignored

    case Bid(amount) if bidValue < amount =>
      bidAuction(amount)
      context become activated
    case _ => sender ! FailedBid
  }

  def ignored: Receive = LoggingReceive {
    case Relist =>
      println(s"Auction $i -> ignored")
      rescheduleOnce(10 seconds, BidTimer)
      context become created
    case DeleteTimer =>
      println(s"Auction $i -> delete")
      context stop self
  }

  def activated: Receive = LoggingReceive {
    case Bid(amount) if bidValue < amount =>
      bidAuction(amount)
    case BidTimer =>
      println(s"Auction $i -> sold for $bidValue")
      winner ! WinAuction(bidValue, i)
      rescheduleOnce(20 seconds, DeleteTimer)
      context become sold
    case _ => sender ! FailedBid
  }

  def sold: Receive = LoggingReceive {
    case DeleteTimer =>
      println(s"Auction $i -> delete")
      context stop self
  }

  private def rescheduleOnce(value: FiniteDuration, caseObject: Object) = {
    if (timer != null) timer.cancel()
    timer = context.system.scheduler.scheduleOnce(value, self, caseObject)
  }

  private def bidAuction(amount: Float): Unit = {
    winner = sender
    println(s"auction $i - previous amount: $bidValue new amount: $amount")
    bidValue = amount
    sender ! SuccessfulBid(amount)
  }
}
