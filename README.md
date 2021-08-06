# itunes-rss

A Play app to provide iTunes formatted RSS feeds for podcast tags.

## Method

Given a CAPI podcast tag id as a request path variable, requests CAPI content for this tag.
Renders the audio elements as an iTunes formatted RSS feed.

Requires a configured CAPI API key to make it's CAPI calls.


## Acast ads and membership callouts

Depending on the tag, Acast ad urls and membership calls to action may be inserted into the iTunes feed.


## To run locally

You need to export an env variable `API_KEY`:

```
$ export API_KEY="some-internal-tier-key"
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



## Docker build

```
sbt docker:publishLocal
```

```
docker run -p 9000:9000 podcasts-rss:0.1.0-SNAPSHOT
```