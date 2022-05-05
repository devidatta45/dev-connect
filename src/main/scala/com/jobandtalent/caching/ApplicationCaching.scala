package com.jobandtalent.caching

import com.jobandtalent.models.{DomainError, GithubResponse, TwitterFollowerResponse, TwitterResponse}
import zio.ZIO

trait ApplicationCaching {
  val applicationCaching: ApplicationCaching.Service
}

object ApplicationCaching {
  trait Service {
    def saveOrganisations(username: String, organisations: Vector[GithubResponse]): ZIO[ApplicationCaching, DomainError, Unit]

    def getOrganisations(username: String): ZIO[ApplicationCaching, DomainError, Vector[GithubResponse]]

    def saveTwitterResponse(username: String, twitterResponse: TwitterResponse): ZIO[ApplicationCaching, DomainError, Unit]

    def getTwitterResponse(username: String): ZIO[ApplicationCaching, DomainError, Option[TwitterResponse]]

    def saveTwitterFollowerResponse(userId: String, twitterFollowerResponse: TwitterFollowerResponse): ZIO[ApplicationCaching, DomainError, Unit]

    def getTwitterFollowerResponse(userId: String): ZIO[ApplicationCaching, DomainError, Option[TwitterFollowerResponse]]
  }
}
