//
//  UpdateTaskEndpoint.swift
//  todomob
//
//  Created by Станислав Дейнекин on 05.05.2025.
//

import Alamofire
import Foundation

struct UpdateTaskEndpoint: APIEndpoint {
    let id: UUID

    init(id: UUID) {
        self.id = id
    }

    var path: String {
        return "/tasks/\(id)/update"
    }

    var method: Alamofire.HTTPMethod {
        return .put
    }

    var parameters: Alamofire.Parameters? {
        return nil
    }

    var headers: Alamofire.HTTPHeaders? {
        return nil
    }
}
