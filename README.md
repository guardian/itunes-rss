# itunes-rss

A Play app to provide iTunes formatted RSS feeds for podcast tags.

## Method

Given a CAPI podcast tag id as a request path variable, requests CAPI content for this tag.
Renders the audio elements as an iTunes formatted RSS feed.

Requires a configured CAPI API key to make it's CAPI calls.

When running locally, this API key is retrieved from the environment variables.  When running in production,
it's obtained from Secrets Manager with the key `/{stage}/{stack}/{app}/capiKey`.

## Acast ads and membership callouts

Depending on the tag, Acast ad urls and membership calls to action may be inserted into the iTunes feed.


## To run locally

First, you'll need Janus credentials for your account. Make sure that you set the AWS_PROFILE environment variable
to point to the correct profile (i.e. capi).
This is used to obtain the signing secret for Play configuration.

You need to export an env variable `API_KEY`:

```
$ export API_KEY="some-internal-tier-key"
```

and an AWS region:

```
$export AWS_REGION=eu-west-1
```

and a fastly signature salt:

```
$export FASTLY_SALT=astringthatactuallyworkswillbeneeded
```

then

```
$ sbt run
```

## To generate the RSS feed locally:

```
$ curl localhost:9000/{some-podcast-id}/podcast.xml
```

## To run the tests:

```
$ sbt test
```
