package sbtjson

object Http {

  def request(url: String, connectTimeout: Int = 5000, readTimeout: Int = 5000, requestMethod: String = "GET"): Either[SbtJsonFailure, String] = {
    try {
      Right(executeRequest(url, connectTimeout, readTimeout, requestMethod))
    } catch {
      case ex: java.io.IOException =>
        Left(NetworkFailure(url, ex))
      case ex: java.net.SocketTimeoutException =>
        Left(HttpTimeout(url, ex))
    }
  }

  @throws(classOf[java.io.IOException])
  @throws(classOf[java.net.SocketTimeoutException])
  private def executeRequest(url: String, connectTimeout: Int = 5000, readTimeout: Int = 5000, requestMethod: String = "GET") = {
    import java.net.{HttpURLConnection, URL}
    val connection = new URL(url).openConnection.asInstanceOf[HttpURLConnection]
    connection.setConnectTimeout(connectTimeout)
    connection.setReadTimeout(readTimeout)
    connection.setRequestMethod(requestMethod)
    val inputStream = connection.getInputStream
    val content     = io.Source.fromInputStream(inputStream).mkString
    if (inputStream != null) inputStream.close()
    content
  }
}
