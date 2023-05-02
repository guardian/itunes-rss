import type {GuStackProps} from "@guardian/cdk/lib/constructs/core";
import {GuParameter, GuStack} from "@guardian/cdk/lib/constructs/core";
import type {App} from "aws-cdk-lib";
import {aws_ssm, Duration} from "aws-cdk-lib";
import {GuPlayApp} from "@guardian/cdk";
import {AccessScope} from "@guardian/cdk/lib/constants";
import {InstanceClass, InstanceSize, InstanceType, Vpc} from "aws-cdk-lib/aws-ec2";
import {policies} from "./policies";
import {GuVpc} from "@guardian/cdk/lib/constructs/ec2";
import {AutoScalingAction} from "aws-cdk-lib/aws-cloudwatch-actions";
import {AdjustmentType, StepScalingAction} from "aws-cdk-lib/aws-autoscaling";
import {Alarm, ComparisonOperator, Metric} from "aws-cdk-lib/aws-cloudwatch";
import {ApplicationProtocol, ListenerAction} from "aws-cdk-lib/aws-elasticloadbalancingv2";

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

    const app = new GuPlayApp(this, {
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
        maximumInstances: 20,
      },
      userData: {
        distributable: {
          fileName: "podcasts-rss_1.0_all.deb",
          executionStatement: "dpkg -i /podcasts-rss/podcasts-rss_1.0_all.deb"
        }
      },
      vpc,
    });

    app.loadBalancer.addListener("HttpToHttps", {
      protocol: ApplicationProtocol.HTTP,
      port: 80,
      defaultAction: ListenerAction.redirect({
        protocol: "HTTPS",
        port: "443",
        permanent: true,
      })
    });

    const cpuHighAlarm = new Alarm(this, "HighCPU", {
      actionsEnabled: true,
      alarmDescription: "CPU utilization alarm for autoscaling",
      comparisonOperator: ComparisonOperator.GREATER_THAN_THRESHOLD,
      datapointsToAlarm: 5,
      evaluationPeriods: 5,
      metric: new Metric({
        dimensionsMap: {
          AutoScalingGroupName: app.autoScalingGroup.autoScalingGroupName,
        },
        metricName: "CPUUtilization",
        namespace: "AWS/EC2",
        period: Duration.minutes(1),
        statistic: "Average",
      }),
      threshold: 50,
      treatMissingData: undefined
    });

    const scaleUpStep = new StepScalingAction(this, "ScaleUp", {
      adjustmentType: AdjustmentType.PERCENT_CHANGE_IN_CAPACITY,
      autoScalingGroup: app.autoScalingGroup,
      cooldown: Duration.minutes(5),
    });
    scaleUpStep.addAdjustment({
      lowerBound: 0,
      adjustment: 100
    });
    cpuHighAlarm.addAlarmAction(new AutoScalingAction(scaleUpStep));

    const scaleDownStep = new StepScalingAction(this, "ScaleDown", {
      adjustmentType: AdjustmentType.CHANGE_IN_CAPACITY,
      autoScalingGroup: app.autoScalingGroup,
      cooldown: Duration.minutes(5),
    });
    scaleDownStep.addAdjustment({
      lowerBound: 0,
      adjustment: -1,
    });
    cpuHighAlarm.addOkAction(new AutoScalingAction(scaleDownStep));

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
