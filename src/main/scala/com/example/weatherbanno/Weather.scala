package com.example.weatherbanno

import cats.Applicative
import cats.implicits._
import io.circe.{Encoder, Json}
import org.http4s.EntityEncoder
import org.http4s.circe._

trait Weather[F[_]]{
  def weather(n: Weather.Name): F[Weather.Summary]
}

object Weather {
  implicit def apply[F[_]](implicit ev: Weather[F]): Weather[F] = ev

  final case class Coords(lat: Double, lon: Double) extends AnyVal
  /**
    * More generally you will want to decouple your edge representations from
    * your internal data structures, however this shows how you can
    * create encoders for your data.
    **/
  final case class Summary(greeting: String) extends AnyVal
  object Summary {
    implicit val greetingEncoder: Encoder[Summary] = new Encoder[Summary] {
      final def apply(a: Summary): Json = Json.obj(
        ("message", Json.fromString(a.greeting)),
      )
    }
    implicit def greetingEntityEncoder[F[_]]: EntityEncoder[F, Summary] =
      jsonEncoderOf[F, Summary]
  }

  def impl[F[_]: Applicative](): Weather[F] = new Weather[F]{
    def weather(c: Weather.Coords): F[Weather.Summary] =
        Summary("Weather, " + n.name).pure[F]
  }
}
