package com.example.weatherbanno

import cats.effect.Concurrent
import io.circe.generic.decoding.DerivedDecoder
import io.circe.generic.encoding.DerivedAsObjectEncoder
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}
import shapeless.Lazy

trait DeriveEncoders[A] {
  implicit def encoder(implicit
      encode: Lazy[DerivedAsObjectEncoder[A]]
  ): Encoder[A] = deriveEncoder

  implicit def entityEncoder[F[_]](implicit
      encode: Lazy[DerivedAsObjectEncoder[A]]
  ): EntityEncoder[F, A] = jsonEncoderOf[F, A](encoder)

  implicit def decoder(implicit decode: Lazy[DerivedDecoder[A]]): Decoder[A] =
    deriveDecoder

  implicit def entityDecoder[F[_]: Concurrent](implicit
      decode: Lazy[DerivedDecoder[A]]
  ): EntityDecoder[F, A] = jsonOf[F, A](Concurrent[F], decoder)
}
