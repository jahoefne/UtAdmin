package views

import play.twirl.api.Html

/**
 * Turns Urban Terror Strings in colored html
 **/
object Color {
  private def generate(text: String, color: String): String = {
    "<span style=\"color: " + color + ";\">" + text + "</span>"
  }

  def colorize(s: String): Html = Html({
    s.replaceAll("<", "&lt;").
      replaceAll(">", "&gt;").
      split("[\\^]+").zipWithIndex.map {
      case (x: String, i: Int) if i > 0 =>
        x.charAt(0) match {
          case '0' => generate(x.substring(1), "#F0F0F0")
          case '1' => generate(x.substring(1), "#FF1010")
          case '2' => generate(x.substring(1), "#80FF80")
          case '3' => generate(x.substring(1), "#FFFF9B")
          case '4' => generate(x.substring(1), "#5050FF")
          case '5' => generate(x.substring(1), "#00FFFF")
          case '6' => generate(x.substring(1), "#FF00FF")
          case '7' => generate(x.substring(1), "#F2F2F2")
          case '8' => generate(x.substring(1), "#D05000")
          case '9' => generate(x.substring(1), "#50503F")
          case '(' => generate(x.substring(1), "#1010FF")
          case ')' => generate(x.substring(1), "#FFFF00")
          case '/' => generate(x.substring(1), "#4C4C4C")
          case '.' => generate(x.substring(1), "#8D8D8D")
          case '-' => generate(x.substring(1), "#C7C7C7")
          case _ => x
        }
      case (x: String, i: Int) => x
    }.mkString("") match {
      case "" => s
      case x => x
    }
  }
  )

}
