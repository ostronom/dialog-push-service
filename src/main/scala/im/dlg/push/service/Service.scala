package im.dlg.push.service

import java.util.concurrent.{Executor, Executors}

import io.grpc._
import io.grpc.stub.StreamObserver
import im.dlg.push.service.push_service._

import scala.concurrent.Future

object PushService {
  def defaultExecutor =
    Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors())
}

final case class PushService(host: String, port: Int, useTls: Boolean = true, executor: Executor = PushService.defaultExecutor) {
  private val channel = {
    val builder: ManagedChannelBuilder[_ <: ManagedChannelBuilder[_]] = ManagedChannelBuilder.forAddress(host, port)
    if (!useTls) builder.usePlaintext(true)
    builder.executor(executor)
    builder.build
  }

  private val asyncStub = PushingGrpc.stub(channel)

  def ping(): Future[PongResponse] =
    asyncStub.ping(PingRequest())

  def single(push: Push): Future[Response] =
    asyncStub.singlePush(push)

  def stream(failures: StreamObserver[Response]): StreamObserver[Push] =
    asyncStub.pushStream(failures)

  def gracefulShutdown() = channel.shutdown()

  def forcedShutdown() = channel.shutdownNow()

  def isShutdown: Boolean = channel.isShutdown()

  def isTerminated: Boolean = channel.isTerminated()
}
