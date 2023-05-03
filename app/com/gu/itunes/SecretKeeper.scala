package com.gu.itunes

import com.gu.{ AppIdentity, AwsIdentity, DevIdentity }
import org.slf4j.LoggerFactory
import play.api.Configuration
import software.amazon.awssdk.auth.credentials.{ AwsCredentialsProviderChain, EnvironmentVariableCredentialsProvider, InstanceProfileCredentialsProvider, ProfileCredentialsProvider, SystemPropertyCredentialsProvider }
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest

import scala.util.{ Failure, Success, Try }

object SecretKeeper {
  private val logger = LoggerFactory.getLogger(getClass)
  lazy val initialStage = Option(System.getProperty("stage"))

  lazy val credentialsProviderChain = AwsCredentialsProviderChain.builder().credentialsProviders(
    EnvironmentVariableCredentialsProvider.create(),
    SystemPropertyCredentialsProvider.create(),
    ProfileCredentialsProvider.create("capi"),
    ProfileCredentialsProvider.create(),
    InstanceProfileCredentialsProvider.create()).build()

  def getIdentity() = if (initialStage.contains("DEV") || initialStage.contains("LOCAL")) {
    Success(DevIdentity("porter"))
  } else {
    AppIdentity.whoAmI("podcasts-rss", () => credentialsProviderChain.resolveCredentials())
  }

  private def loadKeyFromSecretsManagerImp(): Try[String] = {
    //we'll just use a basic, blocking client here as it's only used in startup
    val client = SecretsManagerClient.builder().credentialsProvider(credentialsProviderChain).build()
    for {
      identity <- getIdentity()
      result <- identity match {
        case AwsIdentity(app, stack, stage, region) =>
          val ssmKey = s"/$stage/$stack/$app/capiKey"
          logger.info(s"Loading API key from secrets manager at $ssmKey")
          Try { client.getSecretValue(GetSecretValueRequest.builder().secretId(ssmKey).build()) }
        case _ =>
          logger.warn("When running locally you should set the API_KEY environment variable or apiKey in application.conf")
          Failure(new RuntimeException("Not running in AWS"))
      }
    } yield result.secretString()
  }

  private def loadKeyFromSecretsManager(): Option[String] = loadKeyFromSecretsManagerImp() match {
    case Success(result) if result != "" => Some(result)
    case Success(result) if result == "" =>
      logger.error("Loaded API key but it was an empty string")
      None
    case Failure(err) =>
      logger.warn(s"Could not load API key: ${err.getMessage}")
      None
  }

  def getApiKey(config: Configuration): Option[String] = config.getOptional[String]("apiKey") match {
    case fromConfig @ Some(apiKey) if apiKey != "" =>
      logger.info("Using CAPI key from configuration file")
      fromConfig
    case _ =>
      loadKeyFromSecretsManager()
  }
}
