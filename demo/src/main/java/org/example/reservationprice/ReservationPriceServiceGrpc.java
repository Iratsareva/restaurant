package org.example.reservationprice;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.66.0)",
    comments = "Source: reservation-price.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class ReservationPriceServiceGrpc {

  private ReservationPriceServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "ReservationPriceService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<org.example.reservationprice.PriceRequest,
      org.example.reservationprice.PriceResponse> getCalculatePriceMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CalculatePrice",
      requestType = org.example.reservationprice.PriceRequest.class,
      responseType = org.example.reservationprice.PriceResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.example.reservationprice.PriceRequest,
      org.example.reservationprice.PriceResponse> getCalculatePriceMethod() {
    io.grpc.MethodDescriptor<org.example.reservationprice.PriceRequest, org.example.reservationprice.PriceResponse> getCalculatePriceMethod;
    if ((getCalculatePriceMethod = ReservationPriceServiceGrpc.getCalculatePriceMethod) == null) {
      synchronized (ReservationPriceServiceGrpc.class) {
        if ((getCalculatePriceMethod = ReservationPriceServiceGrpc.getCalculatePriceMethod) == null) {
          ReservationPriceServiceGrpc.getCalculatePriceMethod = getCalculatePriceMethod =
              io.grpc.MethodDescriptor.<org.example.reservationprice.PriceRequest, org.example.reservationprice.PriceResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CalculatePrice"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.reservationprice.PriceRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.reservationprice.PriceResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ReservationPriceServiceMethodDescriptorSupplier("CalculatePrice"))
              .build();
        }
      }
    }
    return getCalculatePriceMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.example.reservationprice.UpdatePriceRequest,
      org.example.reservationprice.PriceResponse> getUpdatePriceMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "UpdatePrice",
      requestType = org.example.reservationprice.UpdatePriceRequest.class,
      responseType = org.example.reservationprice.PriceResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.example.reservationprice.UpdatePriceRequest,
      org.example.reservationprice.PriceResponse> getUpdatePriceMethod() {
    io.grpc.MethodDescriptor<org.example.reservationprice.UpdatePriceRequest, org.example.reservationprice.PriceResponse> getUpdatePriceMethod;
    if ((getUpdatePriceMethod = ReservationPriceServiceGrpc.getUpdatePriceMethod) == null) {
      synchronized (ReservationPriceServiceGrpc.class) {
        if ((getUpdatePriceMethod = ReservationPriceServiceGrpc.getUpdatePriceMethod) == null) {
          ReservationPriceServiceGrpc.getUpdatePriceMethod = getUpdatePriceMethod =
              io.grpc.MethodDescriptor.<org.example.reservationprice.UpdatePriceRequest, org.example.reservationprice.PriceResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "UpdatePrice"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.reservationprice.UpdatePriceRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.reservationprice.PriceResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ReservationPriceServiceMethodDescriptorSupplier("UpdatePrice"))
              .build();
        }
      }
    }
    return getUpdatePriceMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ReservationPriceServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ReservationPriceServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ReservationPriceServiceStub>() {
        @java.lang.Override
        public ReservationPriceServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ReservationPriceServiceStub(channel, callOptions);
        }
      };
    return ReservationPriceServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ReservationPriceServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ReservationPriceServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ReservationPriceServiceBlockingStub>() {
        @java.lang.Override
        public ReservationPriceServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ReservationPriceServiceBlockingStub(channel, callOptions);
        }
      };
    return ReservationPriceServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ReservationPriceServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ReservationPriceServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ReservationPriceServiceFutureStub>() {
        @java.lang.Override
        public ReservationPriceServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ReservationPriceServiceFutureStub(channel, callOptions);
        }
      };
    return ReservationPriceServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     * <pre>
     * Метод расчета цены (основной)
     * </pre>
     */
    default void calculatePrice(org.example.reservationprice.PriceRequest request,
        io.grpc.stub.StreamObserver<org.example.reservationprice.PriceResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCalculatePriceMethod(), responseObserver);
    }

    /**
     * <pre>
     * Метод обновления цены (из самостоятельного задания №8)
     * </pre>
     */
    default void updatePrice(org.example.reservationprice.UpdatePriceRequest request,
        io.grpc.stub.StreamObserver<org.example.reservationprice.PriceResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getUpdatePriceMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service ReservationPriceService.
   */
  public static abstract class ReservationPriceServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return ReservationPriceServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service ReservationPriceService.
   */
  public static final class ReservationPriceServiceStub
      extends io.grpc.stub.AbstractAsyncStub<ReservationPriceServiceStub> {
    private ReservationPriceServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ReservationPriceServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ReservationPriceServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Метод расчета цены (основной)
     * </pre>
     */
    public void calculatePrice(org.example.reservationprice.PriceRequest request,
        io.grpc.stub.StreamObserver<org.example.reservationprice.PriceResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCalculatePriceMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Метод обновления цены (из самостоятельного задания №8)
     * </pre>
     */
    public void updatePrice(org.example.reservationprice.UpdatePriceRequest request,
        io.grpc.stub.StreamObserver<org.example.reservationprice.PriceResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getUpdatePriceMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service ReservationPriceService.
   */
  public static final class ReservationPriceServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<ReservationPriceServiceBlockingStub> {
    private ReservationPriceServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ReservationPriceServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ReservationPriceServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Метод расчета цены (основной)
     * </pre>
     */
    public org.example.reservationprice.PriceResponse calculatePrice(org.example.reservationprice.PriceRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCalculatePriceMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Метод обновления цены (из самостоятельного задания №8)
     * </pre>
     */
    public org.example.reservationprice.PriceResponse updatePrice(org.example.reservationprice.UpdatePriceRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getUpdatePriceMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service ReservationPriceService.
   */
  public static final class ReservationPriceServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<ReservationPriceServiceFutureStub> {
    private ReservationPriceServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ReservationPriceServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ReservationPriceServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Метод расчета цены (основной)
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.example.reservationprice.PriceResponse> calculatePrice(
        org.example.reservationprice.PriceRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCalculatePriceMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Метод обновления цены (из самостоятельного задания №8)
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.example.reservationprice.PriceResponse> updatePrice(
        org.example.reservationprice.UpdatePriceRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getUpdatePriceMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_CALCULATE_PRICE = 0;
  private static final int METHODID_UPDATE_PRICE = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_CALCULATE_PRICE:
          serviceImpl.calculatePrice((org.example.reservationprice.PriceRequest) request,
              (io.grpc.stub.StreamObserver<org.example.reservationprice.PriceResponse>) responseObserver);
          break;
        case METHODID_UPDATE_PRICE:
          serviceImpl.updatePrice((org.example.reservationprice.UpdatePriceRequest) request,
              (io.grpc.stub.StreamObserver<org.example.reservationprice.PriceResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getCalculatePriceMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              org.example.reservationprice.PriceRequest,
              org.example.reservationprice.PriceResponse>(
                service, METHODID_CALCULATE_PRICE)))
        .addMethod(
          getUpdatePriceMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              org.example.reservationprice.UpdatePriceRequest,
              org.example.reservationprice.PriceResponse>(
                service, METHODID_UPDATE_PRICE)))
        .build();
  }

  private static abstract class ReservationPriceServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ReservationPriceServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.example.reservationprice.ReservationPriceProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("ReservationPriceService");
    }
  }

  private static final class ReservationPriceServiceFileDescriptorSupplier
      extends ReservationPriceServiceBaseDescriptorSupplier {
    ReservationPriceServiceFileDescriptorSupplier() {}
  }

  private static final class ReservationPriceServiceMethodDescriptorSupplier
      extends ReservationPriceServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    ReservationPriceServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (ReservationPriceServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ReservationPriceServiceFileDescriptorSupplier())
              .addMethod(getCalculatePriceMethod())
              .addMethod(getUpdatePriceMethod())
              .build();
        }
      }
    }
    return result;
  }
}
