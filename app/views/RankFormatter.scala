package views

import models.Group
import play.twirl.api.Html

object RankFormatter {
  def labelForRank(rank: Group) = Html{
    rank match {
     /* case x if x.level >= 90 => {
        """<span class="label label-god">"""  + rank.name + "</span>"
      }
      case x if x.level >= 80 => {
        """<span class="label label-headadmin">"""  + rank.name + "</span>"
      }
      case x if x.level >= 60 => {
        """<span class="label label-admin">"""  + rank.name + "</span>"
      }
      case x if x.level >= 55 => {
        """<span class="label label-mod">"""  + rank.name + "</span>"
      }
      case x if x.level >= 50 => {
        """<span class="label label-member">"""  + rank.name + "</span>"
      }
      case x if x.level >= 40 => {
        """<span class="label label-recruit">"""  + rank.name + "</span>"
      }
      case x if x.level >= 30 => {
        """<span class="label label-friend">"""  + rank.name + "</span>"
      }
      case x if x.level >= 20 => {
        """<span class="label label-regular">"""  + rank.name + "</span>"
      }*/
      case _ => {
        """<span class="highlight">"""+rank.name+"""</span>"""
      }
    }
  }
  def adminStar(rank:Group) = Html(
    if(rank.level>55) """<i class="material-icons amber-text">stars</i>"""
    else ""
  )
}
