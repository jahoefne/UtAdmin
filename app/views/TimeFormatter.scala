package views

import org.joda.time.{Duration, Period, DateTime}
import org.joda.time.format.PeriodFormatterBuilder
import play.twirl.api.Html


object TimeFormatter {
  def timeToString(time: DateTime) = {
    time.toString("dd.MMM.yyyy (HH:mm:ss)")
  }

  def formatDuration(d: Duration): String ={
    s"${d.getStandardHours}h ${d.getStandardMinutes-d.getStandardHours*60}m ${d.getStandardSeconds-d.getStandardMinutes*60}s Binary: (${d.getStandardHours.toBinaryString}h ${(d.getStandardMinutes-d.getStandardHours*60).toBinaryString}m ${(d.getStandardSeconds-d.getStandardMinutes*60).toBinaryString}s)"
  }

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
