package com.lab2first

import akka.actor.FSM.StateTimeout
import akka.actor.{ActorSystem, Props}
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
    val seller = system.actorOf(Props(classOf[Seller], Array("asd")))
    actor = TestFSMRef(new Auction(seller))
  }

  describe("An Auction") {
    describe("when InitialState") {
      it("initializes properly") {
        actor ! InitializeAuction

        assert(actor.stateName == Created)
        assert(actor.stateData == NotInitialized)
      }
    }

    describe("when Created") {
      it("is bidded and go to different state") {
        val proxy = TestProbe()
        actor.setState(Created)

        proxy.send(actor, Bid(20.0f))

        proxy.expectMsg(SuccessfulBid(20.0f))

        assert(actor.stateName == Activated)
        assert(actor.stateData == AuctionData(20.0f, proxy.ref))
      }

      it("returns to Ignored after timeout") {
        actor.setState(Created)

        actor ! StateTimeout

        assert(actor.stateName == Ignored)
        assert(actor.stateData == NotInitialized)
      }
    }

    describe("when Ignored") {
      it("returns to Created after Relist") {
        actor.setState(Ignored)

        actor ! Relist

        assert(actor.stateName == Created)
        assert(actor.stateData == NotInitialized)
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
        val proxy = TestProbe()
        actor.setState(Activated, AuctionData(0f, proxy.ref))

        proxy.send(actor, StateTimeout)

        assert(actor.stateName == Sold)
        assert(actor.stateData == AuctionData(0f, proxy.ref))
      }

      it("is bidded after Bid event") {
        val proxy = TestProbe()
        actor.setState(Activated, AuctionData(20.0f, proxy.ref))

        proxy.send(actor, Bid(40.0f))

        proxy.expectMsg(SuccessfulBid(40.0f))
        assert(actor.stateName == Activated)
        assert(actor.stateData == AuctionData(40.0f, proxy.ref))
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
