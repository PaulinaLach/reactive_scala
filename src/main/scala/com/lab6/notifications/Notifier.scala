package com.lab6.notifications

import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor.{Actor, ActorLogging, ActorNotFound, ActorSelection, OneForOneStrategy, Props, SupervisorStrategy}
import com.lab6.notifications.Notifier.{Notification, NotificationPayload}
import com.lab6.notifications.NotifierRequest.SuccessDeliver

class Notifier(auctionPublisher: () => ActorSelection) extends Actor with ActorLogging {
  private val maxRetries = 1000

  override def receive: Receive = {
    case payload: NotificationPayload => sendNotification(payload)
    case SuccessDeliver(payload) => println(s"Delivere successful: $payload")
  }

  override val supervisorStrategy: SupervisorStrategy = OneForOneStrategy(maxNrOfRetries = maxRetries, loggingEnabled = false) {
    case _: ActorNotFound =>
      log.debug("Didn't found auctionPublisher")
      Restart

    case _ => Escalate
  }

  private def sendNotification(payload: NotificationPayload): Unit = {
    context.actorOf(NotifierRequest.props(notificationToSend = Notification(auctionPublisher(), payload)))
  }
}

object Notifier {
  def props(externalPublisher: () => ActorSelection): Props = Props(new Notifier(externalPublisher))
  case class Notification(target: ActorSelection, payload: NotificationPayload)
  trait NotificationPayload
}
