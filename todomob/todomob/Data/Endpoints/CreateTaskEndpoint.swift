//
//  CreateTaskEndpoint.swift
//  todomob
//
//  Created by Станислав Дейнекин on 05.05.2025.
//

import Alamofire
import Foundation

struct CreateTaskEndpoint: APIEndpoint {

    var path: String {
        return "/tasks"
    }

    var method: HTTPMethod {
        return .post
    }

    var parameters: Parameters? {
        return nil
    }

    var headers: HTTPHeaders? {
        return [
            "Content-Type": "application/json",
            "Accept": "application/json"
        ]
    }
}
