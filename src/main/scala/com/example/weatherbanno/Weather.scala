package com.example.weatherbanno

import cats.implicits._
import cats.{Monad, MonadError}

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneOffset}
import scala.language.higherKinds

trait Weather[F[_]] {
  def summary(coords: Weather.Coords): F[Weather.Summary]
}

object Weather {
  implicit def apply[F[_]](implicit ev: Weather[F]): Weather[F] = ev

  final case class Coords(lon: Double, lat: Double)

  object Coords {
    // Not sure why but SBT won't accept MonadError[*, Throwable]
    type MonadThrowable[A[_]] = MonadError[A, Throwable]
    def fromStrings[F[_]: MonadThrowable](
        lonStr: String,
        latStr: String
    ): F[Coords] =
      for {
        lon <- MonadError[F, Throwable].catchNonFatal(lonStr.toDouble)
        lat <- MonadError[F, Throwable].catchNonFatal(latStr.toDouble)
      } yield Weather.Coords(lon, lat)
  }

  final case class Summary(
      timestamp: String,
      current_temperature_feel: String,
      current_conditions: Seq[String],
      // This re-uses the type from OpenWeather, which should be
      // immediately changed if any changes to this data structure
      // are desired
      current_alerts: Seq[OpenWeatherApi.OneCallResponseAlert]
  )

  object Summary extends DeriveEncoders[Summary]

  def impl[F[_]: Monad](owa: OpenWeatherApi[F]): Weather[F] = new Weather[F] {
    override def summary(coords: Weather.Coords): F[Weather.Summary] =
      for {
        oneCallResponse <- owa.oneCall(coords.lon, coords.lat)
      } yield Summary(
        timestamp = DateTimeFormatter.ISO_DATE_TIME.format(
          Instant.ofEpochSecond(oneCallResponse.current.dt).atOffset(ZoneOffset.UTC)
        ),
        // This makes a subjective call about what counts as what
        // temperature "feel". Because this is an opinion undefined by
        // the requirements, it uses the magic numbers 270K and 297K
        // as placeholder boundaries. For now this is left inlined so
        // that it will be easier to refactor later.
        current_temperature_feel = oneCallResponse.current.feels_like match {
          case t if t < 270  => "cold"
          case t if t <= 297 => "moderate"
          case _             => "hot"
        },
        current_conditions = oneCallResponse.current.weather.map(_.main),
        current_alerts = oneCallResponse.alerts
      )
  }
}
