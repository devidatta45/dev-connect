package com.jobandtalent.clients

import com.jobandtalent.models._
import com.jobandtalent.utils.JsonSupport
import org.json4s.native.JsonMethods.parse
import sttp.capabilities.zio.ZioStreams
import sttp.client3.httpclient.zio.HttpClientZioBackend
import sttp.client3._
import zio.{Task, ZIO}
import cats.implicits._
import com.jobandtalent.conf.TwitterConfig

trait LiveZioTwitterClient extends TwitterClient with ZioSttpBackendWrapper[Task, ZioStreams] with JsonSupport {

  override val backend: Task[SttpBackend[Task, ZioStreams]] = HttpClientZioBackend()

  override val twitterService: TwitterClient.Service = new TwitterClient.Service {
    override def getUserDetails(username: String): ZIO[TwitterClient, DomainError, Either[ValidationErrors, TwitterResponse]] = {
      val request = basicRequest
        .get(uri"https://api.twitter.com/2/users/by/username/$username")
        .header("Authorization", TwitterConfig.bearerToken)

      for {
        backend <- backend.mapError(error => TwitterError(error.getMessage))
        twitterResponse <- request.send(backend).mapError(error => TwitterError(error.getMessage))
        serializedTwitterResponse = twitterResponse.body match {
          case Right(rawValue) => serializeToTwitterResponse(rawValue)
          case Left(errorResponse) => ValidationErrors(Vector(ErrorResponse("Twitter", errorResponse, twitterResponse.code.toString()))).asLeft.asRight
        }
        finalTwitterResponse <- ZIO.fromEither(serializedTwitterResponse)
      } yield finalTwitterResponse
    }

    override def getFollowers(userId: String): ZIO[TwitterClient, DomainError, Either[ValidationErrors, TwitterFollowerResponse]] = {
      for {
        backend <- backend.mapError(error => TwitterError(error.getMessage))
        twitterFollowerResponse <- basicRequest
          .get(uri"https://api.twitter.com/2/users/${userId}/followers")
          .header("Authorization", TwitterConfig.bearerToken)
          .send(backend)
          .mapError(error => TwitterError(error.getMessage))
        serializedTwitterFollowerResponse = twitterFollowerResponse.body match {
          case Right(rawValue) => serializeToTwitterFollowerResponse(rawValue)
          case Left(errorResponse) => ValidationErrors(Vector(ErrorResponse("Twitter", errorResponse, twitterFollowerResponse.code.toString()))).asLeft.asRight
        }
        finalResponse <- ZIO.fromEither(serializedTwitterFollowerResponse)
      } yield finalResponse
    }
  }

  def serializeToTwitterResponse(rawValue: String): Either[DomainError, Either[ValidationErrors, TwitterResponse]] = {
    Either.catchNonFatal {
      parse(rawValue).extract[TwitterResponse].asRight
    }.leftMap(error => SerializationError(error, "Twitter_Details"))
  }

  def serializeToTwitterFollowerResponse(rawValue: String): Either[DomainError, Either[ValidationErrors, TwitterFollowerResponse]] = {
    Either.catchNonFatal {
      parse(rawValue).extract[TwitterFollowerResponse].asRight
    }.leftMap(error => SerializationError(error, "Twitter_Follower"))
  }
}

object LiveZioTwitterClient extends LiveZioTwitterClient