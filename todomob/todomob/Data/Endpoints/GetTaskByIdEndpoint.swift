//
//  GetTaskByIdEndpoint.swift
//  todomob
//
//  Created by Станислав Дейнекин on 05.05.2025.
//

import Alamofire
import Foundation

struct GetTaskByIdEndpoint: APIEndpoint {
    let id: UUID

    init(id: UUID) {
        self.id = id
    }

    var path: String {
        return "/tasks/\(id)"
    }

    var method: Alamofire.HTTPMethod {
        return .get
    }

    var parameters: Alamofire.Parameters? {
        return nil
    }

    var headers: Alamofire.HTTPHeaders? {
        return nil
    }
}

