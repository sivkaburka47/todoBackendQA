//
//  TaskService.swift
//  todomob
//
//  Created by Станислав Дейнекин on 05.05.2025.
//

import Foundation

protocol TaskService {
    func fetchTasks(
            status: Status?,
            priority: Priority?,
            deadlineFrom: Date?,
            deadlineTo: Date?,
            sortBy: SortField,
            direction: SortDirection
        ) async throws -> [TaskEntity]
}

class APITaskService: TaskService {
    private let httpClient: HTTPClient

    init(httpClient: HTTPClient = AlamofireHTTPClient(baseURL: .local)) {
        self.httpClient = httpClient
    }

    func fetchTasks(
            status: Status? = nil,
            priority: Priority? = nil,
            deadlineFrom: Date? = nil,
            deadlineTo: Date? = nil,
            sortBy: SortField = .createdAt,
            direction: SortDirection = .desc
        ) async throws -> [TaskEntity] {
            let endpoint = GetAllTaskEndpoint(
                status: status,
                priority: priority,
                deadlineFrom: deadlineFrom,
                deadlineTo: deadlineTo,
                sortBy: sortBy,
                direction: direction
            )
            return try await httpClient.sendRequest(endpoint: endpoint, requestBody: nil as EmptyRequestModel?)
        }
}
