package com.example.weatherbanno

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  def run(args: List[String]) =
    WeatherbannoServer.stream[IO].compile.drain.as(ExitCode.Success)
}
