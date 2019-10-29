from grpc_mate.product_search_engine_pb2 import UploadProductResponse


def test_UploadProductResponse_enum():
    """
    see https://developers.google.com/protocol-buffers/docs/reference/python-generated#enum on how to use enum
    see https://github.com/protocolbuffers/protobuf/blob/master/python/google/protobuf/internal/enum_type_wrapper.py
        for method in all enums
    :return:
    """
    upload_product_response = UploadProductResponse(result_status=UploadProductResponse.SUCCESS)
    assert upload_product_response.result_status == UploadProductResponse.ResultStatus.Value('SUCCESS')
