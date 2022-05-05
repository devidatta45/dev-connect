package com.jobandtalent.models

sealed trait DomainError {
  def code: String

  def message: String
}

case class GithubError(override val message: String,
                       override val code: String = DomainError.GITHUB_ERROR) extends DomainError

final case class SerializationError(error: Throwable,
                                    field: String,
                                    override val code: String = DomainError.SERIALIZATION_ERROR) extends DomainError {
  override def message: String = s"Serialization failed while parsing $field with error ${error.getMessage} "
}

case class TwitterError(override val message: String,
                        override val code: String = DomainError.TWITTER_ERROR) extends DomainError

object DomainError {
  val GITHUB_ERROR = "GITHUB_ERROR"
  val SERIALIZATION_ERROR = "SERIALIZATION_ERROR"
  val TWITTER_ERROR = "TWITTER_ERROR"
}
