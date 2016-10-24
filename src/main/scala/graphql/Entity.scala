package graphql

import akka.actor.{Actor, Props, ActorSystem}

import akka.cluster.sharding.ClusterSharding
import akka.cluster.sharding.ClusterShardingSettings
import akka.cluster.sharding.ShardRegion

/**
 * Created by niuzhaojie on 23/10/16.
 */
object Entity {
  val extractEntityId: ShardRegion.ExtractEntityId = {
    case cmd: Command => (cmd.id, cmd)
  }

  val extractShardId: ShardRegion.ExtractShardId = {
    case cmd: Command => (cmd.id.hashCode % 100).toString
  }

  def startSharding(system: ActorSystem, shardName: String, entityProps: Props) =
    ClusterSharding(system).start(
      typeName = shardName,
      entityProps = entityProps,
      settings = ClusterShardingSettings(system),
      extractEntityId = extractEntityId,
      extractShardId = extractShardId)

}

trait Entity extends Actor {
  import context.dispatcher

  def entityName: String

  protected var record: Any = _

}
