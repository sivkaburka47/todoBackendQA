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

    func deleteTask(id: UUID) async throws

    func toggleTask(id: UUID) async throws

    func createTask(title: String, description: String?, priority: Priority?, deadline: Date?) async throws -> TaskEntity

    func updateTask(id: UUID, title: String?, description: String?, priority: Priority?, deadline: Date?) async throws

    func getTaskById(id: UUID) async throws -> TaskEntity
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

    func deleteTask(id: UUID) async throws {
        let endpoint = DeleteTaskEndpoint(id: id)
        try await httpClient.sendRequestWithoutResponse(endpoint: endpoint, requestBody: nil as EmptyRequestModel?)
    }

    func toggleTask(id: UUID) async throws {
        let endpoint = ToggleTaskEndpoint(id: id)
        try await httpClient.sendRequestWithoutResponse(endpoint: endpoint, requestBody: nil as EmptyRequestModel?)
    }

    func createTask(title: String, description: String?, priority: Priority?, deadline: Date?) async throws -> TaskEntity {
        let endpoint = CreateTaskEndpoint()
        let entity = CreateTaskEntity(
                title: title,
                description: description,
                priority: priority,
                deadline: deadline
            )
        return try await httpClient.sendRequest(endpoint: endpoint, requestBody: entity)
    }

    func updateTask(id: UUID, title: String?, description: String?, priority: Priority?, deadline: Date?) async throws {
        let endpoint = UpdateTaskEndpoint(id: id)
        let entity = UpdateTaskEntity(
                title: title,
                description: description,
                priority: priority,
                deadline: deadline
            )
        try await httpClient.sendRequestWithoutResponse(endpoint: endpoint, requestBody: entity)
    }

    func getTaskById(id: UUID) async throws -> TaskEntity {
        let endpoint = GetTaskByIdEndpoint(id: id)
        return try await httpClient.sendRequest(endpoint: endpoint, requestBody: nil as EmptyRequestModel?)
    }
}
