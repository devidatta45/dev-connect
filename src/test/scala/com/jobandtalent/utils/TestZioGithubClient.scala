package com.jobandtalent.utils

import com.jobandtalent.clients.{GithubClient, TwitterClient}
import com.jobandtalent.models._
import zio.ZIO
import cats.implicits._

object TestZioGithubClient extends GithubClient {
  override val githubService: GithubClient.Service = (username: String) => {
    val response: Either[DomainError, Either[ValidationErrors, Vector[GithubResponse]]] =
      TestData.githubMockData.get(username).toRight(GithubError("Does not exist")).map(_.asRight)

    ZIO.fromEither(response)
  }
}

object TestZioTwitterClient extends TwitterClient {
  override val twitterService: TwitterClient.Service = new TwitterClient.Service {
    override def getUserDetails(username: String): ZIO[TwitterClient, DomainError, Either[ValidationErrors, TwitterResponse]] = {
      val response: Either[DomainError, Either[ValidationErrors, TwitterResponse]] =
        TestData.twitterMockData.get(username).toRight(TwitterError("Does not exist"))
          .map(res => TwitterResponse(DataResponse(username, username)).asRight)

      ZIO.fromEither(response)
    }

    override def getFollowers(userId: String): ZIO[TwitterClient, DomainError, Either[ValidationErrors, TwitterFollowerResponse]] = {
      val response: Either[DomainError, Either[ValidationErrors, TwitterFollowerResponse]] =
        TestData.twitterMockData.get(userId).toRight(TwitterError("Does not exist")).map(_.asRight)

      ZIO.fromEither(response)
    }
  }
}

object TestData {
  val githubMockData = Map(
    "user1" -> Vector(GithubResponse("org1"), GithubResponse("org2"), GithubResponse("org3")),
    "user2" -> Vector(GithubResponse("org2"), GithubResponse("org3"), GithubResponse("org4")),
    "user3" -> Vector(GithubResponse("org3"), GithubResponse("org4"), GithubResponse("org5")),
    "user4" -> Vector(GithubResponse("org7"), GithubResponse("org8"), GithubResponse("org9"))
  )

  val twitterMockData = Map(
    "user1" -> TwitterFollowerResponse(Vector(DataResponse("user2", "user2"), DataResponse("user4", "user4"))),
    "user2" -> TwitterFollowerResponse(Vector(DataResponse("user1", "user1"), DataResponse("user3", "user3"))),
    "user3" -> TwitterFollowerResponse(Vector(DataResponse("user1", "user1"), DataResponse("user2", "user2"))),
    "user4" -> TwitterFollowerResponse(Vector(DataResponse("user1", "user1"))),
  )
}