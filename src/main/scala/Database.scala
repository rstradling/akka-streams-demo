import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.io.IOResult
import akka.stream.scaladsl._
import akka.util.ByteString
import java.io.ByteArrayInputStream
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import cats.data._

object Database {

  case class DocId(id: String)

  trait Database {
    def getDocumentStream(id: DocId): Source[ByteString, Future[IOResult]]
  }

  object Documents {
    def getDocument(id: DocId) =
      Reader {
        (db: Database) =>
          db.getDocumentStream(id)
      }
  }
  object ResourceDatabase extends Database {
    def getDocumentStream(id: DocId): Source[ByteString, Future[IOResult]] = {
      val i = id.id.toByte
      val ba = new ByteArrayInputStream(Array.fill[Byte](10000)(i))
      StreamConverters.fromInputStream(() => ba, 1000)
    }
  }
}

object Application extends App {

  import Database._

  val source = Source(Vector(DocId("1"), DocId("2")))

  def flow(): Flow[DocId, Reader[Database, Source[ByteString, Future[IOResult]]], akka.NotUsed] = {
    Flow[DocId] map Documents.getDocument 
  }

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val system = ActorSystem("application-streams")
  implicit val materializer = ActorMaterializer()

  val f = source.via(flow).mapAsync(6) { x => Future { x } }.flatMapConcat(x => x.run(ResourceDatabase)).runForeach(println(_))

  f onSuccess {
    case t => println("Success")
  }
  f onFailure {
    case t => println("Failure")
  }

  Await.result(f, 10 seconds)
  val terminate = system.terminate()
  terminate.onSuccess {
    case x => println("Finished")
  }
  Await.result(terminate, 30 seconds)
}

object HttpApplication extends App {
}
