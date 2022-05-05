package com.jobandtalent.clients

import com.jobandtalent.caching.ApplicationCaching
import com.jobandtalent.models.{DomainError, TwitterFollowerResponse, TwitterResponse, ValidationErrors}
import zio.ZIO

trait TwitterClient {
  val twitterService: TwitterClient.Service
}


object TwitterClient {
  trait Service {
    def getUserDetails(username: String): ZIO[TwitterClient with ApplicationCaching, DomainError, Either[ValidationErrors, TwitterResponse]]

    def getFollowers(userId: String): ZIO[TwitterClient with ApplicationCaching, DomainError, Either[ValidationErrors, TwitterFollowerResponse]]
  }
}