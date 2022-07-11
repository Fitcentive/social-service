package io.fitcentive.social.domain.config

import com.typesafe.config.Config

case class Neo4jConfig(databaseUri: String, username: String, password: String, instanceName: String)

object Neo4jConfig {
  def fromConfig(config: Config): Neo4jConfig =
    Neo4jConfig(
      databaseUri = config.getString("uri"),
      username = config.getString("username"),
      password = config.getString("password"),
      instanceName = config.getString("instance-name")
    )
}

