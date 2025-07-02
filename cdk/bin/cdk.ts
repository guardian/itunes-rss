import "source-map-support/register";
import { App } from "aws-cdk-lib";
import { PodcastsRss } from "../lib/podcasts-rss";

const app = new App();
new PodcastsRss(app, "PodcastsRss-CODE", { stack: "content-api", stage: "CODE", env: { region: "eu-west-1" } });
new PodcastsRss(app, "PodcastsRss-PROD", { stack: "content-api", stage: "PROD", env: { region: "eu-west-1" } });
