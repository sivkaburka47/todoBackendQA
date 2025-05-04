//
//  BaseURL.swift
//  todomob
//
//  Created by Станислав Дейнекин on 05.05.2025.
//

enum BaseURL {
    case local

    var baseURL: String {
        switch self {
        case .local:
            return "http://localhost:8080/api"
        }
    }
}
