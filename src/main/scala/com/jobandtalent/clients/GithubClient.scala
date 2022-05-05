package com.jobandtalent.clients

import com.jobandtalent.caching.ApplicationCaching
import com.jobandtalent.models.{DomainError, GithubResponse, ValidationErrors}
import zio._

trait GithubClient {
  val githubService: GithubClient.Service
}

object GithubClient {
  trait Service {
    def getOrganisations(username: String): ZIO[GithubClient with ApplicationCaching, DomainError, Either[ValidationErrors, Vector[GithubResponse]]]
  }
}