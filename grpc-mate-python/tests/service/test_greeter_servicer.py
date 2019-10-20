import pytest
from grpc_mate.helloworld_pb2 import HelloRequest


@pytest.fixture(scope='module')
def grpc_add_to_server():
    from grpc_mate.helloworld_pb2_grpc import add_GreeterServicer_to_server

    return add_GreeterServicer_to_server


@pytest.fixture(scope='module')
def grpc_servicer():
    from service.greeter_servicer import GreeterServicer

    return GreeterServicer()


@pytest.fixture(scope='module')
def grpc_stub_cls(grpc_channel):
    from grpc_mate.helloworld_pb2_grpc import GreeterStub

    return GreeterStub


def test_SayHello(grpc_stub):
    hello_request = HelloRequest(name='ivan')
    response = grpc_stub.SayHello(hello_request)

    assert response.message == f'hello {hello_request.name}'

