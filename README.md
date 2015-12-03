# itunes-rss

A Play app to provide RSS feed for podcast tags.

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
$ curl localhost:9000/itunesrss?tagId=science/series/science
```

## To run the tests:

```
$ sbt test
```
