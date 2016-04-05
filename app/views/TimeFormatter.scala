package views

import org.joda.time.{Period, DateTime}
import org.joda.time.format.PeriodFormatterBuilder
import play.twirl.api.Html

object TimeFormatter {
  def timePassedSince(time: DateTime) = Html {
    time.isBefore(DateTime.now().minusSeconds(10)) match {
      case true => {
        new PeriodFormatterBuilder().appendDays()
          .appendSuffix("d", "d")
          .appendSeparator(" ")
          .appendHours()
          .appendSuffix("h", "h")
          .appendMinutes()
          .appendSuffix("m", "m")
          .appendSeparator(" ")
          .appendSeconds()
          .appendSuffix("s", "s")
          .toFormatter().print(new Period(time, DateTime.now()))
      }
      case _ => {
        "<i>just now</i>"
      }
    }
  }
}
