package com.gu.itunes

import com.gu.{ AppIdentity, AwsIdentity, DevIdentity }
import org.slf4j.LoggerFactory
import play.api.Configuration
import software.amazon.awssdk.auth.credentials.{ AwsCredentialsProviderChain, EnvironmentVariableCredentialsProvider, InstanceProfileCredentialsProvider, ProfileCredentialsProvider, SystemPropertyCredentialsProvider }
import software.amazon.awssdk.regions.Region
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
    Success(DevIdentity("podcasts-rss"))
  } else {
    AppIdentity.whoAmI("podcasts-rss", () => credentialsProviderChain.resolveCredentials())
  }

  private def loadFromSecretsManagerImp(lookupKey: String): Try[String] = {
    for {
      identity <- getIdentity()
      result <- identity match {
        case AwsIdentity(app, stack, stage, region) =>
          //we'll just use a basic, blocking client here as it's only used in startup
          val client = SecretsManagerClient.builder().credentialsProvider(credentialsProviderChain).region(Region.of(region)).build()

          val ssmKey = s"/$stage/$stack/$app/$lookupKey"
          logger.info(s"Loading $lookupKey key from secrets manager at $ssmKey")
          Try { client.getSecretValue(GetSecretValueRequest.builder().secretId(ssmKey).build()) }
        case _ =>
          if (lookupKey == "capiKey") {
            logger.warn("When running locally you should set the API_KEY environment variable or apiKey in application.conf")
          } else {
            logger.warn(s"When running locally you should set $lookupKey in application.conf")
          }
          Failure(new RuntimeException("Not running in AWS"))
      }
    } yield result.secretString()
  }

  private def loadKeyFromSecretsManager(): Option[String] = loadFromSecretsManagerImp("capiKey") match {
    case Success(result) if result != "" => Some(result)
    case Success(result) if result == "" =>
      logger.error("Loaded API key but it was an empty string")
      None
    case Failure(err) =>
      logger.warn(s"Could not load API key: ${err.getMessage}")
      None
  }

  private def loadFastlySignatureSaltFromSecretsManager(): Option[String] = loadFromSecretsManagerImp("fastlyImageResizerSignatureSalt") match {
    case Success(result) if result != "" => Some(result)
    // In the event we can't load the signature salt, or we load
    // an empty value, this shouldn't crash the application.
    // Instead we just suppress the generation of episodic artwork
    // images if we determine that the salt is NONE (or an empty
    // string which we treat as NONE)
    case Success(result) if result == "" =>
      logger.warn("Loaded the fastly image resizer signature salt but it was an empty string")
      None
    case Failure(err) =>
      logger.warn(s"Could not load Fastly image resizer signature salt: ${err.getMessage}")
      None
  }

  def getApiKey(config: Configuration): Option[String] = config.getOptional[String]("apiKey") match {
    case fromConfig @ Some(apiKey) if apiKey != "" =>
      logger.info("Using CAPI key from configuration file")
      fromConfig
    case _ =>
      loadKeyFromSecretsManager()
  }

  def getImageResizerSignatureSalt(config: Configuration): Option[String] = config.getOptional[String]("fastlyImageResizerSignatureSalt") match {
    case fromConfig @ Some(sigSalt) if sigSalt != "" =>
      logger.info("Loaded the fastly image resizer signature salt from configuration")
      fromConfig
    case fromConfig @ Some(sigSalt) if sigSalt == "" =>
      logger.warn("Loaded the fastly image resizer signature salt from configuration but it was an empty string")
      None
    case _ =>
      loadFastlySignatureSaltFromSecretsManager()
  }
}
