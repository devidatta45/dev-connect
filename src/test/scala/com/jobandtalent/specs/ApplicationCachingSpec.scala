package com.jobandtalent.specs

import com.jobandtalent.caching.{ApplicationCaching, InMemoryApplicationCaching}
import com.jobandtalent.models.{DataResponse, GithubResponse, TwitterFollowerResponse, TwitterResponse}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatest.{EitherValues, OptionValues, Suite}
import zio.Runtime
import zio.internal.Platform

class ApplicationCachingSpec extends AnyFlatSpec
  with Suite with should.Matchers
  with OptionValues with EitherValues {

  object TestEnvironment extends ApplicationCaching {
    override val applicationCaching: ApplicationCaching.Service = InMemoryApplicationCaching.applicationCaching
  }

  val myRuntime: Runtime[ApplicationCaching] = Runtime(TestEnvironment, Platform.default)

  behavior of "Application Caching"

  it should "save mappings for github correctly" in {
    myRuntime.unsafeRun(InMemoryApplicationCaching.applicationCaching.
      saveOrganisations("user1", Vector(GithubResponse("org1")))) shouldBe()
  }

  it should "get mappings for github correctly" in {
    myRuntime.unsafeRun(InMemoryApplicationCaching.applicationCaching.
      saveOrganisations("user1", Vector(GithubResponse("org1")))) shouldBe()

    myRuntime.unsafeRun(InMemoryApplicationCaching.applicationCaching.
      getOrganisations("user1")) shouldBe Vector(GithubResponse("org1"))
  }

  it should "save mappings for twitter details correctly" in {
    myRuntime.unsafeRun(InMemoryApplicationCaching.applicationCaching.
      saveTwitterResponse("user1", TwitterResponse(DataResponse("user1", "user1")))) shouldBe()
  }

  it should "get mappings for twitter details correctly" in {
    myRuntime.unsafeRun(InMemoryApplicationCaching.applicationCaching.
      saveTwitterResponse("user1", TwitterResponse(DataResponse("user1", "user1")))) shouldBe()

    myRuntime.unsafeRun(InMemoryApplicationCaching.applicationCaching.
      getTwitterResponse("user1")) shouldBe Some(TwitterResponse(DataResponse("user1", "user1")))
  }

  it should "save mappings for twitter followers details correctly" in {
    myRuntime.unsafeRun(InMemoryApplicationCaching.applicationCaching.
      saveTwitterFollowerResponse("user1", TwitterFollowerResponse(Vector(DataResponse("user1", "user1"))))) shouldBe()
  }

  it should "get mappings for twitter followers detailscorrectly" in {
    myRuntime.unsafeRun(InMemoryApplicationCaching.applicationCaching.
      saveTwitterFollowerResponse("user1", TwitterFollowerResponse(Vector(DataResponse("user1", "user1"))))) shouldBe()

    myRuntime.unsafeRun(InMemoryApplicationCaching.applicationCaching.
      getTwitterFollowerResponse("user1")) shouldBe Some(TwitterFollowerResponse(Vector(DataResponse("user1", "user1"))))
  }
}
