package com.lab2first

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit}
import org.scalatest.{BeforeAndAfterAll, FunSpec, FunSpecLike}

class AuctionSearchSpec extends TestKit(ActorSystem("AuctionSearchSpec")) with FunSpecLike with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    system.terminate
  }

  describe("A AuctionSearch") {
    describe("on received Subscribe") {
      it("add sender to list") {
      }

    }
  }
}
