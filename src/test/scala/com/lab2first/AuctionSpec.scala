package com.lab2first

import akka.actor.FSM.StateTimeout
import akka.actor.{ActorRefFactory, ActorSystem, Props}
import akka.testkit.{TestFSMRef, TestKit, TestProbe}
import com.lab2first.actors._
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSpecLike}

class AuctionSpec extends TestKit(ActorSystem("AuctionSpec")) with FunSpecLike with BeforeAndAfterAll
  with BeforeAndAfterEach{

  override def afterAll(): Unit = {
    system.terminate
  }

  var actor : TestFSMRef[State, Data, Auction] = _
  var seller : TestFSMRef[State, Data, Auction] = _

  override protected def beforeEach(): Unit = {
    val seller = TestProbe()
    actor = TestFSMRef(new Auction())
  }

  describe("An Auction") {
    describe("when InitialState") {
      it("initializes properly") {
        val proxy = TestProbe()
        proxy.send(actor, InitializeAuction("title"))

        assert(actor.stateName == Created)
        assert(actor.stateData == Initialized("title", proxy.ref))
      }
    }

    describe("when Created") {
      it("is bidded and go to different state") {
        val proxy = TestProbe()
        actor.setState(Created, Initialized("title", null))

        proxy.send(actor, Bid(20.0f))

        proxy.expectMsg(SuccessfulBid(20.0f))

        assert(actor.stateName == Activated)
        assert(actor.stateData == HasBeenBid("title", 20.0f, null, proxy.ref))
      }

      it("returns to Ignored after timeout") {
        actor.setState(Created, Initialized("title", null))

        actor ! StateTimeout

        assert(actor.stateName == Ignored)
        assert(actor.stateData == Initialized("title", null))
      }
    }

    describe("when Ignored") {
      it("returns to Created after Relist") {
        actor.setState(Ignored, Initialized("title", null))

        actor ! Relist

        assert(actor.stateName == Created)
        assert(actor.stateData == Initialized("title", null))
      }

      it("stays after timeout") {
        actor.setState(Ignored)

        actor ! StateTimeout

        assert(actor.stateName == Ignored)
        assert(actor.stateData == NotInitialized)
      }
    }

    describe("when Activated") {
      it("gets sold after timeout") {
        val seller = TestProbe()
        val proxy = TestProbe()
        actor.setState(Activated, HasBeenBid("title", 0f, seller.ref, proxy.ref))

        proxy.send(actor, StateTimeout)

        assert(actor.stateName == Sold)
        assert(actor.stateData == HasBeenBid("title", 0f, seller.ref, proxy.ref))
      }

      it("is bidded after Bid event") {
        val seller = TestProbe()
        val proxy = TestProbe()
        actor.setState(Activated, HasBeenBid("title", 0f, seller.ref, proxy.ref))

        proxy.send(actor, Bid(40.0f))

        proxy.expectMsg(SuccessfulBid(40.0f))
        assert(actor.stateName == Activated)
        assert(actor.stateData == HasBeenBid("title", 40.0f, seller.ref, proxy.ref))
      }

      it("should notify previous bidder") {
        val seller = TestProbe()
        val prevBid = TestProbe()
        val proxy = TestProbe()
        actor.setState(Activated, HasBeenBid("title", 0f, seller.ref, prevBid.ref))

        proxy.send(actor, Bid(40.0f))

        proxy.expectMsg(SuccessfulBid(40.0f))
        prevBid.expectMsg(NotificateBided(40.0f))
        assert(actor.stateName == Activated)
        assert(actor.stateData == HasBeenBid("title", 40.0f, seller.ref, proxy.ref))
      }
    }

    describe("when Sold") {
      it("stays after delete") {
        actor.setState(Sold)

        actor ! StateTimeout

        assert(actor.stateName == Sold)
      }
    }
  }
}
