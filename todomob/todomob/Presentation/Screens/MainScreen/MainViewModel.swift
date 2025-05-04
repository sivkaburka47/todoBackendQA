//
//  MainViewModel.swift
//  todomob
//
//  Created by Станислав Дейнекин on 05.05.2025.
//

import Foundation
import Combine

class MainViewModel: ObservableObject {
    @Published var tasks: [TaskEntity] = []
    @Published var isLoading: Bool = false
    @Published var errorMessage: String?

    @Published var selectedStatus: Status?
    @Published var selectedPriority: Priority?
    @Published var deadlineFrom: Date?
    @Published var deadlineTo: Date?
    @Published var selectedSortField: SortField = .createdAt
    @Published var selectedSortDirection: SortDirection = .desc

    private let taskService: TaskService

    init(taskService: TaskService = APITaskService()) {
        self.taskService = taskService
    }

    @MainActor
    func fetchTasks() async {
        isLoading = true
        errorMessage = nil

        print("Fetching tasks with filters: status=\(selectedStatus?.rawValue ?? "nil"), priority=\(selectedPriority?.rawValue ?? "nil"), deadlineFrom=\(deadlineFrom?.description ?? "nil"), deadlineTo=\(deadlineTo?.description ?? "nil"), sortBy=\(selectedSortField.rawValue), direction=\(selectedSortDirection.rawValue)")

        do {
            let fetchedTasks = try await taskService.fetchTasks(
                status: selectedStatus,
                priority: selectedPriority,
                deadlineFrom: deadlineFrom,
                deadlineTo: deadlineTo,
                sortBy: selectedSortField,
                direction: selectedSortDirection
            )
            print("Received tasks: \(fetchedTasks.count)")
            tasks = fetchedTasks
        } catch {
            print("Fetch error: \(error)")
            errorMessage = "Ошибка загрузки: \(error.localizedDescription)"
        }
        isLoading = false
    }
}
