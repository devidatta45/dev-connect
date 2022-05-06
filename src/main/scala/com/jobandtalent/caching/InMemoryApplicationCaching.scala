package com.jobandtalent.caching

import com.jobandtalent.caching.InMemoryApplicationCaching.{TwitterFollowerResponseStore, TwitterResponseStore, UserStore}
import com.jobandtalent.models.{DomainError, GithubResponse, TwitterFollowerResponse, TwitterResponse}
import zio.Runtime.default
import zio.{Ref, ZIO}

trait InMemoryApplicationCaching extends ApplicationCaching {

  override val applicationCaching: ApplicationCaching.Service = new ApplicationCaching.Service {
    val ref: Ref[UserStore] = default.unsafeRun(Ref.make(UserStore(Map())))
    val twitterRef: Ref[TwitterResponseStore] = default.unsafeRun(Ref.make(TwitterResponseStore(Map())))
    val twitterFollowerRef: Ref[TwitterFollowerResponseStore] = default.unsafeRun(Ref.make(TwitterFollowerResponseStore(Map())))

    override def saveOrganisations(username: String, organisations: Vector[GithubResponse]): ZIO[ApplicationCaching, DomainError, Unit] = {
      ref.modify(_.saveAll(username, organisations))
    }

    override def getOrganisations(username: String): ZIO[ApplicationCaching, DomainError, Vector[GithubResponse]] = {
      ref.modify(_.getByUserName(username))
    }

    override def saveTwitterResponse(username: String, twitterResponse: TwitterResponse): ZIO[ApplicationCaching, DomainError, Unit] = {
      twitterRef.modify(_.saveAll(username, twitterResponse))
    }

    override def getTwitterResponse(username: String): ZIO[ApplicationCaching, DomainError, Option[TwitterResponse]] = {
      twitterRef.modify(_.getByUserName(username))
    }

    override def saveTwitterFollowerResponse(userId: String, twitterFollowerResponse: TwitterFollowerResponse): ZIO[ApplicationCaching, DomainError, Unit] = {
      twitterFollowerRef.modify(_.saveAll(userId, twitterFollowerResponse))
    }

    override def getTwitterFollowerResponse(userId: String): ZIO[ApplicationCaching, DomainError, Option[TwitterFollowerResponse]] = {
      twitterFollowerRef.modify(_.getByUserName(userId))
    }
  }
}

object InMemoryApplicationCaching extends InMemoryApplicationCaching {
  final case class UserStore(storage: Map[String, Vector[GithubResponse]]) {

    def saveAll(username: String, organisations: Vector[GithubResponse]): (Unit, UserStore) = {
      ((), copy(storage = storage + (username -> organisations)))
    }

    def getByUserName(userName: String): (Vector[GithubResponse], UserStore) = {
      (storage.getOrElse(userName, Vector.empty), this)
    }

    def removeAll: (Unit, UserStore) = {
      ((), copy(storage = Map.empty[String, Vector[GithubResponse]]))
    }
  }

  final case class TwitterResponseStore(storage: Map[String, TwitterResponse]) {

    def saveAll(username: String, twitterResponse: TwitterResponse): (Unit, TwitterResponseStore) = {
      ((), copy(storage = storage + (username -> twitterResponse)))
    }

    def getByUserName(userName: String): (Option[TwitterResponse], TwitterResponseStore) = {
      (storage.get(userName), this)
    }

    def removeAll: (Unit, TwitterResponseStore) = {
      ((), copy(storage = Map.empty[String, TwitterResponse]))
    }
  }

  final case class TwitterFollowerResponseStore(storage: Map[String, TwitterFollowerResponse]) {

    def saveAll(userId: String, twitterFollowerResponse: TwitterFollowerResponse): (Unit, TwitterFollowerResponseStore) = {
      ((), copy(storage = storage + (userId -> twitterFollowerResponse)))
    }

    def getByUserName(userId: String): (Option[TwitterFollowerResponse], TwitterFollowerResponseStore) = {
      (storage.get(userId), this)
    }

    def removeAll: (Unit, TwitterFollowerResponseStore) = {
      ((), copy(storage = Map.empty[String, TwitterFollowerResponse]))
    }
  }
}