package com.second.actors

import akka.actor.{Actor, ActorRef, Cancellable}
import akka.event.LoggingReceive

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

case class Bid(amount: Float)
case object Relist
case object BidTimer
case object DeleteTimer
case object InitializeAuction
case class SuccessfulBid(amount: Float)
case object FailedBid

class Auction(val i: Int) extends Actor {
  var timer: Cancellable = null
  var bidValue = 0.0

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
      println(s"auction $i - previous amount: $bidValue new amount: $amount")
      bidValue = amount
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
      println(s"auction $i - previous amount: $bidValue new amount: $amount")
      bidValue = amount
      sender ! SuccessfulBid(amount)
    case BidTimer =>
      println(s"Auction $i -> sold for $bidValue")
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
}
