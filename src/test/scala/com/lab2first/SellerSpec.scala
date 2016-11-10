package com.lab2first

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalatest.{BeforeAndAfterAll, FunSpecLike}

class SellerSpec extends TestKit(ActorSystem("SellerSpec")) with FunSpecLike with BeforeAndAfterAll {
  override def afterAll(): Unit = {
    system.terminate
  }
}
