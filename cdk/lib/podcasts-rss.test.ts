import { App } from "aws-cdk-lib";
import { Template } from "aws-cdk-lib/assertions";
import { PodcastsRss } from "./podcasts-rss";

describe("The PodcastsRss stack", () => {
  it("matches the snapshot", () => {
    const app = new App();
    const stack = new PodcastsRss(app, "PodcastsRss", { stack: "content-api", stage: "TEST", env: { region: "eu-west-1" } });
    const template = Template.fromStack(stack);
    expect(template.toJSON()).toMatchSnapshot();
  });
});
