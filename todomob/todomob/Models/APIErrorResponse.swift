//
//  APIErrorResponse.swift
//  todomob
//
//  Created by Станислав Дейнекин on 05.05.2025.
//

import Foundation

struct APIErrorResponse: Decodable {
    let status: String
    let message: String
}

enum APIError: LocalizedError {
    case server(message: String)

    var errorDescription: String? {
        switch self {
        case .server(let message):
            return message
        }
    }
}

