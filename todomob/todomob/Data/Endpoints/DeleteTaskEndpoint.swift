//
//  DeleteTaskEndpoint.swift
//  todomob
//
//  Created by Станислав Дейнекин on 05.05.2025.
//

import Foundation
import Alamofire

struct DeleteTaskEndpoint: APIEndpoint {
    let id: UUID

    init(id: UUID) {
        self.id = id
    }

    var path: String {
        return "/tasks/\(id)/delete"
    }

    var method: Alamofire.HTTPMethod {
        return .delete
    }

    var parameters: Alamofire.Parameters? {
        return nil
    }

    var headers: Alamofire.HTTPHeaders? {
        return nil
    }
}
