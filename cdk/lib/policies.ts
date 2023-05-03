import {Effect, PolicyStatement} from "aws-cdk-lib/aws-iam";
import {GuStack} from "@guardian/cdk/lib/constructs/core";
import {GuPolicy} from "@guardian/cdk/lib/constructs/iam";

export const policies = (stack:GuStack) => new GuPolicy(stack, "Policies", {
        statements: [
            //FIXME - what is this needed for?
            new PolicyStatement({
                effect: Effect.ALLOW,
                actions: ["cloudformation:DescribeStackResource"],
                resources: ["*"]
            }),
            //FIXME - FAR TOO BROAD!!!!
            new PolicyStatement({
                effect: Effect.ALLOW,
                actions: ["cloudwatch:*"],
                resources: ["*"]
            }),
            new PolicyStatement({
                effect: Effect.ALLOW,
                actions: ["s3:GetObject"],
                resources: [`arn:aws:s3:::content-api-config/podcasts-rss/${stack.stage}/*`]
            }),
            //FIXME = what is this needed for?
            new PolicyStatement({
                effect: Effect.ALLOW,
                actions: [
                    "ec2:DescribeInstances",
                    "autoscaling:DescribeAutoScalingGroups",
                    "autoscaling:DescribeAutoScalingInstances"
                ],
                resources: ["*"]
            }),
            //Allow rotating secret access
            new PolicyStatement({
                effect: Effect.ALLOW,
                actions: ["ssm:GetParameters"],
                resources: [`arn:aws:ssm:${stack.region}:${stack.account}:parameter/ANY/content-api/podcasts-rss/play-secret`]
            }),
            //Allow API key secret access
            new PolicyStatement({
                effect: Effect.ALLOW,
                actions: ["secretsmanager:GetSecretValue"],
                resources: [`arn:aws:secretsmanager:/${stack.region}:${stack.account}:secret:/${stack.stage}/${stack.stack}/${stack.app ?? "podcasts-rss"}/*`]
            })
        ]
    });