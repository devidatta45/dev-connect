package com.jobandtalent.clients

import com.jobandtalent.models._
import com.jobandtalent.utils.JsonSupport
import org.json4s.native.JsonMethods.parse
import sttp.capabilities.zio.ZioStreams
import sttp.client3.httpclient.zio.HttpClientZioBackend
import sttp.client3._
import zio.{Task, ZIO}
import cats.implicits._
import com.jobandtalent.caching.ApplicationCaching
import com.jobandtalent.conf.TwitterConfig

trait LiveZioTwitterClient extends TwitterClient with ZioSttpBackendWrapper[Task, ZioStreams] with JsonSupport {

  override val backend: Task[SttpBackend[Task, ZioStreams]] = HttpClientZioBackend()

  override val twitterService: TwitterClient.Service = new TwitterClient.Service {
    override def getUserDetails(username: String): ZIO[TwitterClient with ApplicationCaching, DomainError, Either[ValidationErrors, TwitterResponse]] = {
      val request = basicRequest
        .get(uri"https://api.twitter.com/2/users/by/username/$username")
        .header("Authorization", TwitterConfig.bearerToken)

      for {
        backend <- backend.mapError(error => TwitterError(error.getMessage))
        cachedResult <- ZIO.accessM[ApplicationCaching](_.applicationCaching.getTwitterResponse(username))
        finalTwitterResponse <- if (cachedResult.isEmpty) {
          for {
            twitterResponse <- request.send(backend).mapError(error => TwitterError(error.getMessage))
            result <- twitterResponse.body match {
              case Right(rawValue) => serializeToTwitterResponseAndUpdateCache(username, rawValue)
              case Left(errorResponse) => ZIO.fromEither {
                ValidationErrors(Vector(ErrorResponse("Github", errorResponse, twitterResponse.code.toString()))).asLeft.asRight
              }
            }
          } yield result
        } else {
          val convertedResult = cachedResult match {
            case Some(result) => result.asRight.asRight
            case None => TwitterError(s"$username not found in cache").asLeft
          }
          ZIO.fromEither(convertedResult)
        }
      } yield finalTwitterResponse
    }

    override def getFollowers(userId: String): ZIO[TwitterClient with ApplicationCaching, DomainError, Either[ValidationErrors, TwitterFollowerResponse]] = {
      for {
        backend <- backend.mapError(error => TwitterError(error.getMessage))
        cachedResult <- ZIO.accessM[ApplicationCaching](_.applicationCaching.getTwitterFollowerResponse(userId))
        finalTwitterResponse <- if (cachedResult.isEmpty) {
          for {
            twitterFollowerResponse <- basicRequest
              .get(uri"https://api.twitter.com/2/users/${userId}/followers")
              .header("Authorization", TwitterConfig.bearerToken)
              .send(backend)
              .mapError(error => TwitterError(error.getMessage))
            result <- twitterFollowerResponse.body match {
              case Right(rawValue) => serializeToTwitterFollowerResponseAndUpdateCache(userId, rawValue)
              case Left(errorResponse) => ZIO.fromEither {
                ValidationErrors(Vector(ErrorResponse("Twitter", errorResponse, twitterFollowerResponse.code.toString()))).asLeft.asRight
              }
            }
          } yield result
        } else {
          val convertedResult = cachedResult match {
            case Some(result) => result.asRight.asRight
            case None => TwitterError(s"$userId not found in cache").asLeft
          }
          ZIO.fromEither(convertedResult)
        }
      } yield finalTwitterResponse
    }
  }

  def serializeToTwitterResponseAndUpdateCache(username: String, rawValue: String): ZIO[ApplicationCaching, DomainError,
    Either[ValidationErrors, TwitterResponse]] = {
    for {
      serializedValue <- ZIO.fromEither(Either.catchNonFatal {
        parse(rawValue).extract[TwitterResponse]
      }.leftMap(error => SerializationError(error, "Twitter_Details")))
      _ <- ZIO.accessM[ApplicationCaching](_.applicationCaching.saveTwitterResponse(username, serializedValue))
    } yield serializedValue.asRight
  }

  def serializeToTwitterFollowerResponseAndUpdateCache(userId: String, rawValue: String): ZIO[ApplicationCaching, DomainError,
    Either[ValidationErrors, TwitterFollowerResponse]] = {
    for {
      serializedValue <- ZIO.fromEither(Either.catchNonFatal {
        parse(rawValue).extract[TwitterFollowerResponse]
      }.leftMap(error => SerializationError(error, "Twitter_Follower")))
      _ <- ZIO.accessM[ApplicationCaching](_.applicationCaching.saveTwitterFollowerResponse(userId, serializedValue))
    } yield serializedValue.asRight
  }
}

object LiveZioTwitterClient extends LiveZioTwitterClient