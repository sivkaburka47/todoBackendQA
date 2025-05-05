//
//  AlamofireHTTPClient.swift
//  todomob
//
//  Created by Станислав Дейнекин on 05.05.2025.
//

import Alamofire
import Foundation

// MARK: - AlamofireHTTPClient
final class AlamofireHTTPClient: HTTPClient {
    private let baseURL: BaseURL

    init(baseURL: BaseURL) {
        self.baseURL = baseURL
    }


    func sendRequest<T: Decodable, U: Encodable>(
        endpoint: APIEndpoint,
        requestBody: U? = nil
    ) async throws -> T {
        let url = baseURL.baseURL + endpoint.path
        let method = endpoint.method
        let headers = endpoint.headers

        return try await withCheckedThrowingContinuation { continuation in
            var request: DataRequest

            if method == .get {
                request = AF.request(url, method: method, parameters: endpoint.parameters, headers: headers)
            } else {
                request = AF.request(url, method: method, parameters: requestBody,     encoder: JSONParameterEncoder(encoder: iso8601Encoder), headers: headers)
            }

            request
                .validate()
                .response { response in
                    self.log(response)
                }
                .responseDecodable(of: T.self, decoder: iso8601Decoder) { response in
                    switch response.result {
                    case .success(let decodedData):
                        continuation.resume(returning: decodedData)
                    case .failure(let error):
                        if let data = response.data,
                           let apiError = try? JSONDecoder().decode(APIErrorResponse.self, from: data) {
                            continuation.resume(throwing: APIError.server(message: apiError.message))
                        } else {
                            continuation.resume(throwing: error)
                        }
                    }
                }
        }
    }



    func sendRequestWithoutResponse<U: Encodable>(endpoint: APIEndpoint, requestBody: U? = nil) async throws {
        let url = baseURL.baseURL + endpoint.path
        let method = endpoint.method
        let headers = endpoint.headers

        return try await withCheckedThrowingContinuation { continuation in
            AF.request(url, method: method, parameters: requestBody, encoder: JSONParameterEncoder.default, headers: headers)
                .validate()
                .response { response in
                    switch response.result {
                    case .success:
                        continuation.resume()
                    case .failure(let error):
                        if let data = response.data,
                           let apiError = try? JSONDecoder().decode(APIErrorResponse.self, from: data) {
                            continuation.resume(throwing: APIError.server(message: apiError.message))
                        } else {
                            continuation.resume(throwing: error)
                        }
                    }
                }
        }
    }

    
}

private let iso8601Encoder: JSONEncoder = {
    let encoder = JSONEncoder()
    encoder.dateEncodingStrategy = .iso8601
    return encoder
}()

private let iso8601Decoder: JSONDecoder = {
    let decoder = JSONDecoder()
    decoder.dateDecodingStrategy = .custom { decoder in
        let container = try decoder.singleValueContainer()
        let dateString = try container.decode(String.self)

        let formatters: [ISO8601DateFormatter] = [
            { let f = ISO8601DateFormatter(); f.formatOptions = [.withInternetDateTime, .withFractionalSeconds]; return f }(),
            { let f = ISO8601DateFormatter(); f.formatOptions = [.withInternetDateTime]; return f }()
        ]

        for formatter in formatters {
            if let date = formatter.date(from: dateString) {
                return date
            }
        }

        throw DecodingError.dataCorruptedError(
            in: container,
            debugDescription: "Invalid date format: \(dateString)"
        )
    }
    return decoder
}()

// MARK: - Requests Logging
private extension AlamofireHTTPClient {
    func log(_ response: AFDataResponse<Data?>) {
        print("---------------------------------------------------------------------------------------")

        let method = response.request?.method?.rawValue ?? "UNKNOWN"
        let url = response.request?.url?.absoluteString ?? "UNKNOWN URL"
        let statusCode = response.response?.statusCode ?? 0
        let dateString = ISO8601DateFormatter().string(from: Date())

        print("[\(dateString)] \(method) \(url)")
        print("Status Code: \(statusCode)")

        if let requestHeaders = response.request?.headers {
            print("Request Headers: \(requestHeaders)")
        }
        if let responseHeaders = response.response?.headers {
            print("Response Headers: \(responseHeaders)")
        }

        print("---------------------------------------------------------------------------------------")

        switch response.result {
        case let .success(responseData):
            if let data = responseData, !data.isEmpty {
                if let object = try? JSONSerialization.jsonObject(with: data),
                   let formattedData = try? JSONSerialization.data(withJSONObject: object, options: [.prettyPrinted]),
                   let jsonString = String(data: formattedData, encoding: .utf8) {
                    print("Response JSON:\n\(jsonString)")
                }
            } else {
                print("Response JSON: {} (empty)")
            }
        case let .failure(error):
            print("Request Failed: \(error.localizedDescription)")
        }

        print("---------------------------------------------------------------------------------------")
    }

}

