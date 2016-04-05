package views

import models.Group
import play.twirl.api.Html

object RankFormatter {
  def labelForRank(rank: Group) = Html{
    rank match {
      case x if x.level >= 80 => {
        """<span class="label label-success">"""  + rank.name + "</span>"
      }
      case x if x.level >= 55 => {
        """<span class="label label-primary">"""  + rank.name + "</span>"
      }
      case x if x.level >= 40 => {
        """<span class="label label-warning">"""  + rank.name + "</span>"
      }
      case _ => {
        """<span class="label label-default">"""  + rank.name + "</span>"
      }
    }
  }
}
