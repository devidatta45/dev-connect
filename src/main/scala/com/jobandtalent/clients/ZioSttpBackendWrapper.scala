package com.jobandtalent.clients

import sttp.client3.SttpBackend

trait ZioSttpBackendWrapper[F[_], G] {
  val backend: F[SttpBackend[F, G]]
}
