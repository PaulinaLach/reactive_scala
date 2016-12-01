package auction.system.auctionsearch

import akka.actor.{Actor, ActorRef, Props}
import akka.event.LoggingReceive
import akka.routing.{ActorRefRoutee, Broadcast, Router, RoutingLogic}
import auction.system.Buyer.FindAuctions
import auction.system.Seller.{AuctionRef, Register, Unregister}
import auction.system.auctionsearch.AuctionSearch.{Registered, Unregistered}

class MasterSearch(numberOfRoutes: Int, dispatchingStrategy: RoutingLogic, routeFactory: () => ActorRef) extends Actor {

  val ackCache: RegistrationCache = RegistrationCache(numberOfRoutes)

  val router = createRouterWith(routesNumber = numberOfRoutes, routingStrategy = dispatchingStrategy)

  private def createRouterWith(routesNumber: Int, routingStrategy: RoutingLogic): Router = {
    val routes = Vector.fill(routesNumber)(singleRoute)
    Router(routingStrategy, routes)
  }

  private def singleRoute: ActorRefRoutee = {
    val route = routeFactory()
    ActorRefRoutee(route)
  }

  override def receive: Receive = LoggingReceive {
    case msg@Register(auction) =>
      scheduleRegisteredAcknowledgmentFor(auction, sender())
      router.route(Broadcast(msg), self)

    case msg@Unregister(auction) =>
      scheduleUnregisteredAcknowledgmentFor(auction, sender())
      router.route(Broadcast(msg), self)

    case msg: FindAuctions =>
      router.route(msg, sender())

    case Registered(auction) =>
      ackCache.registerActionAcknowledged(auction)

    case Unregistered(auction) =>
      ackCache.unregisterActionAcknowledged(auction)
  }

  def scheduleRegisteredAcknowledgmentFor(auction: AuctionRef, seller: ActorRef) = {
    ackCache.scheduleCallbackOnRegisterActionAcknowledged(auction, auction => seller ! Registered(auction))
  }

  def scheduleUnregisteredAcknowledgmentFor(auction: AuctionRef, seller: ActorRef) = {
    ackCache.scheduleCallbackOnUnregisterActionAcknowledged(auction, auction => seller ! Unregistered(auction))
  }
}

object MasterSearch {
  def props(numberOfRoutes: Int, dispatchingStrategy: RoutingLogic, routeFactory: () => ActorRef): Props =
    Props(new MasterSearch(numberOfRoutes, dispatchingStrategy, routeFactory))
}