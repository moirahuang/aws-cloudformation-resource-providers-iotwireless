package software.amazon.iotwireless.destination;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.iotwireless.IotWirelessClient;
import software.amazon.awssdk.services.iotwireless.model.ListDestinationsRequest;
import software.amazon.awssdk.services.iotwireless.model.ListDestinationsResponse;
import software.amazon.awssdk.services.iotwireless.model.AccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.List;
import java.util.stream.Collectors;

public class ListHandler extends BaseHandlerStd {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<IotWirelessClient> proxyClient,
            final Logger logger) {

        ResourceModel model = request.getDesiredResourceState();

        final ListDestinationsRequest listDestinationsRequest = Translator.translateToListRequest(request.getNextToken());

        try {
            ListDestinationsResponse listDestinationsResponse = proxy.injectCredentialsAndInvokeV2(listDestinationsRequest, proxyClient.client()::listDestinations);

            final List<ResourceModel> models = listDestinationsResponse.destinationList().stream()
                    .map(destination -> ResourceModel.builder()
                            .name(destination.name())
                            .arn(destination.arn())
                            .expressionType(destination.expressionTypeAsString())
                            .description(destination.description())
                            .roleArn(destination.roleArn())
                            .build())
                    .collect(Collectors.toList());

            String nextToken = listDestinationsResponse.nextToken();
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModels(models)
                    .nextToken(nextToken)
                    .status(OperationStatus.SUCCESS)
                    .build();
        } catch (final Exception e) {
            throw handleException(e, listDestinationsRequest);
        }
    }
}
