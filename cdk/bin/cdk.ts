import "source-map-support/register";
import { App } from "aws-cdk-lib";
import { PodcastsRss } from "../lib/podcasts-rss";

const app = new App();
new PodcastsRss(app, "PodcastsRss-CODE", { stack: "content-api", stage: "CODE" });
new PodcastsRss(app, "PodcastsRss-PROD", { stack: "content-api", stage: "PROD" });
