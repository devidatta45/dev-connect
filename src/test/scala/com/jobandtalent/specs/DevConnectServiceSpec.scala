package com.jobandtalent.specs

import com.jobandtalent.clients.{GithubClient, TwitterClient}
import com.jobandtalent.models.ConnectedResponse
import com.jobandtalent.services.DevConnectService
import com.jobandtalent.utils.{TestZioGithubClient, TestZioTwitterClient}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatest.{EitherValues, OptionValues, Suite}
import zio.Runtime
import zio.internal.Platform
import cats.implicits._

class DevConnectServiceSpec extends AnyFlatSpec
  with Suite with should.Matchers
  with OptionValues with EitherValues {

  object TestEnvironment extends GithubClient with TwitterClient {
    override val githubService: GithubClient.Service = TestZioGithubClient.githubService
    override val twitterService: TwitterClient.Service = TestZioTwitterClient.twitterService
  }

  val myRuntime: Runtime[GithubClient with TwitterClient] = Runtime(TestEnvironment, Platform.default)

  behavior of "DevConnectService"

  it should "give correct response if devs are connected" in {
    myRuntime.unsafeRun(DevConnectService.service.
      areConnected("user1", "user2")) shouldBe ConnectedResponse(connected = true, Some(Vector("org2", "org3"))).asRight
  }

  it should "not be connected if devs are not following each other even though having common organisations" in {
    myRuntime.unsafeRun(DevConnectService.service.
      areConnected("user1", "user3")) shouldBe ConnectedResponse(connected = false, None).asRight
    myRuntime.unsafeRun(DevConnectService.service.
      areConnected("user2", "user3")) shouldBe ConnectedResponse(connected = true, Some(Vector("org3", "org4"))).asRight
  }

  it should "not be connected if devs are not having common organisation even if they follow each other" in {
    myRuntime.unsafeRun(DevConnectService.service.
      areConnected("user1", "user4")) shouldBe ConnectedResponse(connected = false, None).asRight
  }

}
