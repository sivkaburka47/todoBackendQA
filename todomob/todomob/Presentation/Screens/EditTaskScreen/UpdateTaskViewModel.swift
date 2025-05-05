//
//  UpdateTaskViewModel.swift
//  todomob
//
//  Created by Станислав Дейнекин on 05.05.2025.
//

import Foundation
import Combine

class UpdateTaskViewModel: ObservableObject {
    @Published var title: String? = nil
    @Published var description: String? = nil
    @Published var priority: Priority? = nil
    @Published var deadline: Date? = nil
    @Published var errorMessage: String? = nil
    @Published var isLoading: Bool = false

    var taskId: UUID
    var status: Status
    var createdAt: Date
    var updatedAt: Date

    var isDeadlineSet: Bool {
        return deadline != nil
    }

    private let taskService: TaskService

    init(taskService: TaskService = APITaskService(), taskId: UUID) {
        self.taskService = taskService
        self.taskId = taskId
        self.status = .active
        self.createdAt = Date()
        self.updatedAt = Date()
    }

    func toggleDeadline(isSet: Bool) {
        if isSet {
            deadline = Date().addingTimeInterval(300)
        } else {
            deadline = nil
        }
    }

    @MainActor
    func fetchTask() async {
        isLoading = true
        do {
            let task = try await taskService.getTaskById(id: taskId)
            title = task.title
            description = task.description
            priority = task.priority
            deadline = task.deadline
            status = task.status
            createdAt = task.createdAt
            updatedAt = task.updatedAt
        } catch {
            errorMessage = "Не удалось загрузить данные задачи"
        }
        isLoading = false
    }

    @MainActor
    func updateTask() async -> Bool {
        isLoading = true
        do {
            _ = try await taskService.updateTask(id: taskId, title: title, description: description, priority: priority, deadline: deadline)
            return true
        } catch let error as APIError {
            errorMessage = error.localizedDescription
            return false
        } catch {
            errorMessage = "Произошла ошибка: \(error.localizedDescription)"
            return false
        }
        isLoading = false
    }
}
