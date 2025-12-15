package ru.xllifi.rewards.config

import kotlinx.datetime.TimeZone
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import ru.xllifi.rewards.configDir
import ru.xllifi.rewards.serializers.text.Component
import java.nio.file.Path

val defaultMainConfig = MainConfig(
  prefix = Component.text("[Rewards]"),
  database = SqliteConfig,
)

@Serializable
data class MainConfig(
  val prefix: Component,
  val database: DatabaseConfig,
  private val timeZone: TimeZone? = null,
) {
  val timeZoneForSure: TimeZone get() = this.timeZone ?: TimeZone.currentSystemDefault()
}

val localDbPath: Path = configDir.resolve("database.db")

@Serializable
sealed interface DatabaseConfig {
  val username: String
  val password: String
  val jdbcUrl: String
  val driver: String
}

@Serializable
@SerialName("sqlite")
object SqliteConfig : DatabaseConfig {
  @Transient
  override val jdbcUrl: String
    get() = "jdbc:sqlite:${localDbPath}"
  @Transient
  override val driver: String
    get() = "org.sqlite.JDBC"
  @Transient
  override val username: String
    get() = ""
  @Transient
  override val password: String
    get() = ""

}

@Serializable
@SerialName("postgres")
data class PostgresConfig(
  val host: String,
  val port: String?,
  val databaseName: String,
  override val username: String,
  override val password: String,
  @Transient
  override val jdbcUrl: String = "jdbc:postgresql://${host}${if (port != null) ":${port}" else ""}${databaseName}",
  @Transient
  override val driver: String = "org.postgresql.Driver",
) : DatabaseConfig