import type {GuStackProps} from "@guardian/cdk/lib/constructs/core";
import {GuParameter, GuStack} from "@guardian/cdk/lib/constructs/core";
import type {App} from "aws-cdk-lib";
import {GuEc2App, GuPlayApp} from "@guardian/cdk";
import {AccessScope} from "@guardian/cdk/lib/constants";
import {InstanceClass, InstanceSize, InstanceType, Vpc} from "aws-cdk-lib/aws-ec2";
import {policies} from "./policies";
import {aws_ssm} from "aws-cdk-lib";
import {GuVpc} from "@guardian/cdk/lib/constructs/ec2";

export class PodcastsRss extends GuStack {
  constructor(scope: App, id: string, props: GuStackProps) {
    super(scope, id, props);

    const urgentAlarmTopicArn = aws_ssm.StringParameter.fromStringParameterName(this, "urgent-alarm-arn", "/account/content-api-common/alarms/urgent-alarm-topic");
    const nonUrgentAlarmTopicArn = aws_ssm.StringParameter.fromStringParameterName(this, "non-urgent-alarm-arn", "/account/content-api-common/alarms/non-urgent-alarm-topic");

    const vpcId = aws_ssm.StringParameter.valueForStringParameter(this, this.getVpcIdPath());
    const vpc = Vpc.fromVpcAttributes(this, "vpc", {
      vpcId: vpcId,
      availabilityZones: ["eu-west-1a","eu-west-1b" ,"eu-west-1c"]
    });

    const subnetsList = new GuParameter(this, "subnets", {
      description: "Subnets to deploy into",
      default: this.getDeploymentSubnetsPath(),
      fromSSM: true,
      type: "List<String>"
    });
    const deploymentSubnets = GuVpc.subnets(this, subnetsList.valueAsList);

    const hostedZone = aws_ssm.StringParameter.valueForStringParameter(this, `/account/services/content-aws.guardianapis/${this.stage}/hostedzoneid`);


    new GuPlayApp(this, {
      access: {
        scope: AccessScope.PUBLIC,
      },
      app: "podcasts-rss",
      applicationLogging: {
        enabled: true,
      },
      certificateProps: {
        domainName: this.stage==="CODE" ? "itunes-feed.content.code.dev-guardianapis.com" : "itunes-feed.content-aws.guardianapis.com",
        hostedZoneId: hostedZone,
      },
      instanceType: InstanceType.of(InstanceClass.T4G, InstanceSize.SMALL),
      monitoringConfiguration: {
        snsTopicName: urgentAlarmTopicArn.stringValue,
        http5xxAlarm: {
          tolerated5xxPercentage: 20,
          numberOfMinutesAboveThresholdBeforeAlarm: 2,
        },
        unhealthyInstancesAlarm: true,
      },
      privateSubnets: deploymentSubnets,
      publicSubnets: deploymentSubnets,
      roleConfiguration: {
        additionalPolicies: [policies(this)],
      },
      scaling: {
        minimumInstances: 2,
        maximumInstances: 4,
      },
      userData: {
        distributable: {
          fileName: "podcasts-rss.deb",
          executionStatement: "dpkg -i podcasts-rss.deb"
        }
      },
      vpc,
    })
  }

  getAccountPath(elementName: string) {
    const basePath = "/account/vpc";
    if(this.stack.includes("preview")) {
      return this.stage=="CODE" ? `${basePath}/CODE-preview/${elementName}` : `${basePath}/PROD-preview/${elementName}`;
    } else {
      return this.stage=="CODE" ? `${basePath}/CODE-live/${elementName}` : `${basePath}/PROD-live/${elementName}`;
    }
  }

  getVpcIdPath() {
    return this.getAccountPath("id");
  }

  getDeploymentSubnetsPath() {
    return this.getAccountPath("subnets")
  }
}
