package sbtjson

object ErrorMessages {
  def mkMessage(error: SbtJsonFailure): String = {
    error match {
        // Todo: implement awesome error messages
      case e@_ => e.toString
    }
  }
}
