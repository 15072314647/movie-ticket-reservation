package org.cineplex.datasource

import scala.concurrent._
import org.cineplex.datasource.IMDB._

/** Defines the behavior of IMDB data source */
trait IMDB {

  /**
    * Asynchronously returns the full movie detail from external data source for given imdb id
    *
    *  @param imdbId the unique imdb id the system is trying query
    *  @param ec implicit variable required for Future execution
    *  @return a Future of potential details of the movie,
    *          returns Some(detail) if the external source does contain related information, otherwise None
    */
  def getMovieDetail(imdbId: IMDBID)(implicit ec: ExecutionContext): Future[Option[MovieDetail]]

}

/** Implementing IMDB trait to mimic external data source */
final class IMDBDemo() extends IMDB {

  //only use the movieID given in the assignment for testing purpose
  override def getMovieDetail(imdbId: IMDBID)(implicit ec: ExecutionContext): Future[Option[MovieDetail]] = {
    Future.successful {
      if (imdbId == "tt0111161") {
        Some(MovieDetail("tt0111161", "The Shawshank Redemption"))
      } else {
        None
      }
    }
  }
}


object IMDB {

  type IMDBID = String

  /**
    *  the imdb id validation is reduced to the one given in the assignment,
    *  while the actual regex rule for imdb should be covering more combination
    **/
  val IMDBID_REGEX = """tt\d{7}"""

  /** data model for the movie detail */
  final case class MovieDetail(imdbId: IMDBID, movieTitle: String)

}