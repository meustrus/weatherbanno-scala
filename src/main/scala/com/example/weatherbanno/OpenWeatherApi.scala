package com.example.weatherbanno

import cats.effect.Concurrent
import io.circe.Decoder.Result
import io.circe.generic.decoding.DerivedDecoder
import io.circe.{Decoder, HCursor, Json, JsonObject}
import org.http4s.Query
import org.http4s.client.Client
import org.http4s.implicits.http4sLiteralsSyntax
import shapeless.Lazy

trait OpenWeatherApi[F[_]] {
  def oneCall(lon: Double, lat: Double): F[OpenWeatherApi.OneCallResponse]
}

object OpenWeatherApi {
  final case class OneCallResponse(
      current: OneCallResponseCurrentConditions,
      alerts: Seq[OneCallResponseAlert]
  )

  object OneCallResponse extends DeriveEncoders[OneCallResponse] {
    implicit override def decoder(implicit decode: Lazy[DerivedDecoder[OneCallResponse]]): Decoder[OneCallResponse] = new Decoder[OneCallResponse] {
      override def apply(c: HCursor): Result[OneCallResponse] = for {
        obj <- c.value.as[JsonObject]
        current <- obj("current").getOrElse(Json.Null).as[OneCallResponseCurrentConditions]
        alerts <- obj("alerts").getOrElse(Json.arr()).as[Seq[OneCallResponseAlert]]
      } yield OneCallResponse(current, alerts)
    }
  }

  final case class OneCallResponseCurrentConditions(
      dt: Long,
      feels_like: Float,
      weather: Seq[OneCallResponseWeather]
  )

  object OneCallResponseCurrentConditions
      extends DeriveEncoders[OneCallResponseCurrentConditions]

  final case class OneCallResponseWeather(
      main: String
  )

  object OneCallResponseWeather extends DeriveEncoders[OneCallResponseWeather]

  final case class OneCallResponseAlert(
      sender_name: String,
      event: String,
      description: String
  )

  object OneCallResponseAlert extends DeriveEncoders[OneCallResponseAlert]

  def impl[F[_]: Concurrent](
      httpClient: Client[F],
      apiKey: String
  ): OpenWeatherApi[F] =
    new OpenWeatherApi[F] {
      override def oneCall(lon: Double, lat: Double): F[OneCallResponse] =
        httpClient.expect[OneCallResponse](
          uri"https://api.openweathermap.org/data/2.5/onecall".copy(query =
            Query(
              "lon" -> Option(lon.toString),
              "lat" -> Option(lat.toString),
              "exclude" -> Option("minutely,hourly,daily"),
              "cnt" -> Option("0"),
              "appid" -> Option(apiKey)
            )
          )
        )
    }
}
