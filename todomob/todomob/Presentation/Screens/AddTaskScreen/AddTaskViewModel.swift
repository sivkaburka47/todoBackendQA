//
//  AddTaskViewModel.swift
//  todomob
//
//  Created by Станислав Дейнекин on 05.05.2025.
//

import Foundation
import Combine

class AddTaskViewModel: ObservableObject {
    @Published var title: String = ""
    @Published var description: String = ""
    @Published var priority: Priority? = nil
    @Published var deadline: Date? = nil
    @Published var errorMessage: String?
    @Published var isShowingDeadline: Bool = false

    private let taskService: TaskService

    init(taskService: TaskService = APITaskService()) {
        self.taskService = taskService
    }

    @MainActor
    func createTask() async -> Bool {
        guard !title.isEmpty else {
            errorMessage = "Название задачи не может быть пустым"
            return false
        }

        do {
            _ = try await taskService.createTask(
                title: title,
                description: description.isEmpty ? nil : description,
                priority: priority,
                deadline: deadline
            )
            return true
        } catch let error as APIError {
            errorMessage = error.localizedDescription
            return false
        } catch {
            errorMessage = "Произошла ошибка: \(error.localizedDescription)"
            return false
        }
    }

    func resetForm() {
        title = ""
        description = ""
        priority = nil
        deadline = nil
        isShowingDeadline = false
        errorMessage = nil
    }
}
