import java.net._
import java.io._

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}

import scala.io.Source

object EchoServer {
  def props: Props = Props[EchoServer]
  final case class MySocket(socket: Socket)
}

class EchoServer extends Actor {
  import EchoServer._

  def read_and_write(in: BufferedReader, out:BufferedWriter): Unit = {

    //println("Enter html name file")
    val request = in.readLine()
    try {
      val arrayOfFirstLineStrings = (request.split(" "))
      if (arrayOfFirstLineStrings(0).equals("GET")) {
        read_all(arrayOfFirstLineStrings, out)
      }
      else {
        out.write("HTTP/1.0 500 \r\n")
        out.write("\r\n")
        out.write("Server Error Unable to Fulfill Request")
        out.flush()
      }
    }
    catch{
      case n : NullPointerException => None
    }
    out.write(in.readLine())
    out.flush()
    in.close()
    out.close()
  }

  def read_all(arrayOfFirstLineStrings: Array[String], out: BufferedWriter): Unit = {
    val url = arrayOfFirstLineStrings(1)

  val req_name = url.substring(1)
    if (url.equals("/")) {
      out.write("HTTP/1.0 200 OK \r\n") //
      out.write("\r\n\n")
      try {
        for (line <- Source.fromFile("src/test.html").getLines) {
          out.write(line)
          out.flush()
        }
      } catch {
        case e: Exception => println(e.getMessage)
      }
    }
    else {
      var req_name = url.substring(1)
      if (!req_name.contains(".html")) {
        val html = ".html"
        req_name = req_name.concat(html)
      }
      try {
        out.write("HTTP/1.1 200 OK \r\n")
        out.write("\r\n\n")
        out.write("Content-Type: text/plain")
        out.write("Connection: close")
        for (line <- Source.fromFile(req_name).getLines) {
          out.write(line.toString)
        }
      } catch {
        case noFile: FileNotFoundException => resourceNotFound(out)
        case e: Exception => println(e.getMessage)
      }
      out.close()
    }

    def resourceNotFound(out: BufferedWriter): Unit ={
      out.write("HTTP/1.1 404 Resource Not Found \r\n")
      out.write("\r\n")
      out.write("Resource Not Found")
      out.flush()
    }
  }

  def receive: PartialFunction[Any, Unit] = {
    case MySocket(s) =>
      val in = new BufferedReader(new InputStreamReader(s.getInputStream))
      val out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream))

      read_and_write(in, out)

      s.close()
  }
}

object AkkaEchoServer {
  import EchoServer._

  val system: ActorSystem = ActorSystem("AkkaEchoServer")
  val my_server: ActorRef = system.actorOf(EchoServer.props)

  def main(args: Array[String]) {
    val server = new ServerSocket(9999)
    while(true) {
      val s = server.accept()
      my_server ! MySocket(s)
    }
  }
}