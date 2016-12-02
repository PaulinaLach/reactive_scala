package com.lab6.actors

import akka.actor.{Actor, ActorRef}
import akka.persistence.fsm.PersistentFSM
import akka.persistence.fsm.PersistentFSM.FSMState
import com.lab6.notifications.{AuctionNotification, EndedNoOffers, EndedWithOffer, NewOffer}

import scala.concurrent.duration._
import scala.reflect.{ClassTag, _}

case class Bid(amount: Float)
case class WinAuction(amount: Float, i: String)
case object Relist
case object BidTimer
case object DeleteTimer
case class InitializeAuction(title: String)
case class SuccessfulBid(amount: Float)
case object FailedBid

sealed trait State extends FSMState
case object InitialState extends State {
  override def identifier: String = "InitialState"
}
case object Created extends State {
  override def identifier: String = "CreatedState"
}
case object Ignored extends State {
  override def identifier: String = "IgnoredState"
}
case object Activated extends State {
  override def identifier: String = "ActivatedState"
}
case object Sold extends State {
  override def identifier: String = "SoldState"
}

sealed trait Data
case class AuctionData(var title: String, var bidValue: Float, var seller: ActorRef, var winner: ActorRef) extends Data {
  def bidAuction(amount: Float, sender: ActorRef): Unit = {
    println(s"auction $title - previous amount: $bidValue new amount: $amount")
    sender ! SuccessfulBid(amount)
    if (sender != winner) winner ! NotificateBided(amount)

    bidValue = amount
    winner = sender
  }
}

sealed trait AuctionEvent
trait AuctionEventWithTime extends AuctionEvent {
  val currentMillis = System.currentTimeMillis
}

case class AuctionCreatedEvent() extends AuctionEventWithTime
case class AuctionActivatedEvent() extends AuctionEventWithTime
case class AuctionIgnoredEvent() extends AuctionEventWithTime
case object AuctionInitializedEvent extends AuctionEvent
case object AuctionSoldEvent extends AuctionEvent

class Auction(val name: String, notifier: ActorRef) extends Actor with PersistentFSM[State, AuctionData, AuctionEvent] {
  override def persistenceId = "persistent-" + name
  override def domainEventClassTag: ClassTag[AuctionEvent] = classTag[AuctionEvent]
  val auctionData = new AuctionData(null, 0.0f, null, null)

  startWith(InitialState, auctionData)

  when(InitialState) {
    case Event(InitializeAuction(title), _) =>
      auctionData.title = title
      auctionData.seller = sender
      goto(Created) applying AuctionInitializedEvent // applying Initialized(title, sender)
  }

  when(Created) {
    case Event(StateTimeout, _) =>
      println(s"Auction ${self.toString()} -> ignored")
      sendNotifierNotification(EndedNoOffers(name))
      goto(Ignored) applying AuctionIgnoredEvent() // applying initialized

    case Event(Bid(amount), _) =>
      auctionData.bidValue = 0.0f
      auctionData.winner = sender
      auctionData.bidAuction(amount, sender)
      sendNotifierNotification(NewOffer(name, amount, sender()))
      goto(Activated) applying AuctionActivatedEvent() // applying auctionData
    case Event(_, _) =>
      sender ! FailedBid
      stay applying AuctionIgnoredEvent()
  }

  when(Ignored) {
    case Event(Relist, _) =>
      println(s"Auction ${self.toString()} -> relist")
      goto(Created) applying AuctionCreatedEvent()
    case Event(DeleteTimer, _) =>
      println(s"Auction ${self.toString()} -> delete")
      stay applying AuctionIgnoredEvent()
  }

  when(Activated) {
    case Event(Bid(amount), _) if auctionData.bidValue < amount =>
      auctionData.bidAuction(amount, sender)
      sendNotifierNotification(NewOffer(name, amount, sender()))
      stay applying AuctionActivatedEvent()
    case Event(StateTimeout, _) =>
      println(s"Auction ${self.toString()} -> sold for ${auctionData.bidValue}")
      auctionData.seller ! WinAuction(auctionData.bidValue, self.toString())
      auctionData.winner ! WinAuction(auctionData.bidValue, self.toString())
      sendNotifierNotification(EndedWithOffer(name, auctionData.bidValue, auctionData.winner))
      goto(Sold) applying AuctionSoldEvent
    case Event(_, _) =>
      sender ! FailedBid
      stay applying AuctionActivatedEvent()
  }

  when(Sold) {
    case Event(StateTimeout, _) =>
      println(s"Auction ${self.toString()} -> delete")
      stay applying AuctionSoldEvent
  }

  override def preStart(): Unit = {
    println("Auction started")
  }

  override def applyEvent(domainEvent: AuctionEvent, data: AuctionData): AuctionData = domainEvent match {
    case AuctionCreatedEvent() =>
      setTimer("expirationTimer", StateTimeout, 10 seconds, false)
      data
    case AuctionActivatedEvent() =>
      setTimer("expirationTimer", StateTimeout, 10 seconds, false)
      data
    case AuctionIgnoredEvent() =>
      setTimer("expirationTimer", StateTimeout, 10 seconds, false)
      data
    case _ =>
      data
  }

  private def sendNotifierNotification(notification: AuctionNotification): Unit = {
    notifier ! notification
  }
}
