//
//  GetAllTaskEndpoint.swift
//  todomob
//
//  Created by Станислав Дейнекин on 05.05.2025.
//

import Alamofire
import Foundation

struct GetAllTaskEndpoint: APIEndpoint {
    let status: Status?
    let priority: Priority?
    let deadlineFrom: Date?
    let deadlineTo: Date?
    let sortBy: SortField
    let direction: SortDirection

    init(status: Status? = nil, priority: Priority? = nil, deadlineFrom: Date? = nil, deadlineTo: Date? = nil, sortBy: SortField = .createdAt, direction: SortDirection = .desc) {
        self.status = status
        self.priority = priority
        self.deadlineFrom = deadlineFrom
        self.deadlineTo = deadlineTo
        self.sortBy = sortBy
        self.direction = direction
    }

    var path: String {
        return "/tasks"
    }

    var method: HTTPMethod {
        return .get
    }

    var parameters: Parameters? {
        var params: [String: Any] = [
            "sortBy": sortBy.rawValue,
            "direction": direction.rawValue
        ]
        if let status = status {
            params["status"] = status.rawValue
        }
        if let priority = priority {
            params["priority"] = priority.rawValue
        }
        if let deadlineFrom = deadlineFrom {
            params["deadlineFrom"] = ISO8601DateFormatter().string(from: deadlineFrom)
        }
        if let deadlineTo = deadlineTo {
            params["deadlineTo"] = ISO8601DateFormatter().string(from: deadlineTo)
        }
        return params
    }

    var headers: HTTPHeaders? {
        return ["Content-Type": "application/json"]
    }
}
