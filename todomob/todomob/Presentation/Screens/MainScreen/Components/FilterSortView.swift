//
//  FilterSortView.swift
//  todomob
//
//  Created by Станислав Дейнекин on 05.05.2025.
//

import SwiftUI

struct FilterSortView: View {
    @ObservedObject var viewModel: MainViewModel
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Статус")) {
                    Picker("Статус", selection: $viewModel.selectedStatus) {
                        Text("Все").tag(Status?.none)
                        ForEach(Status.allCases, id: \.self) { status in
                            Text(status.localized).tag(Status?.some(status))
                        }
                    }
                    .pickerStyle(.menu)
                }

                Section(header: Text("Приоритет")) {
                    Picker("Приоритет", selection: $viewModel.selectedPriority) {
                        Text("Все").tag(Priority?.none)
                        ForEach(Priority.allCases, id: \.self) { priority in
                            Text(priority.localized).tag(Priority?.some(priority))
                        }
                    }
                    .pickerStyle(.menu)
                }

                Section(header: Text("Дедлайн")) {
                    DatePicker(
                        "С",
                        selection: Binding(
                            get: { viewModel.deadlineFrom ?? Date() },
                            set: { viewModel.deadlineFrom = $0 }
                        ),
                        displayedComponents: [.date]
                    )
                    .disabled(viewModel.deadlineFrom == nil)
                    Toggle("Активировать", isOn: Binding(
                        get: { viewModel.deadlineFrom != nil },
                        set: { viewModel.deadlineFrom = $0 ? Date() : nil }
                    ))

                    DatePicker(
                        "По",
                        selection: Binding(
                            get: { viewModel.deadlineTo ?? Date() },
                            set: { viewModel.deadlineTo = $0 }
                        ),
                        displayedComponents: [.date]
                    )
                    .disabled(viewModel.deadlineTo == nil)
                    Toggle("Активировать", isOn: Binding(
                        get: { viewModel.deadlineTo != nil },
                        set: { viewModel.deadlineTo = $0 ? Date() : nil }
                    ))
                }

                Section(header: Text("Сортировка")) {
                    Picker("Поле", selection: $viewModel.selectedSortField) {
                        ForEach(SortField.allCases, id: \.self) { field in
                            Text(field.localized).tag(field)
                        }
                    }
                    .pickerStyle(.menu)

                    Picker("Направление", selection: $viewModel.selectedSortDirection) {
                        Text("По возрастанию").tag(SortDirection.asc)
                        Text("По убыванию").tag(SortDirection.desc)
                    }
                    .pickerStyle(.menu)
                }
            }
            .navigationTitle("Фильтры")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Отменить") {
                        resetFilters()
                        dismiss()
                    }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Применить") {
                        Task {
                            await viewModel.fetchTasks()
                            dismiss()
                        }
                    }
                }
            }
        }
    }
}

// MARK: - Help methods
extension FilterSortView {
    private func resetFilters() {
        viewModel.selectedStatus = nil
        viewModel.selectedPriority = nil
        viewModel.deadlineFrom = nil
        viewModel.deadlineTo = nil
        viewModel.selectedSortField = .createdAt
        viewModel.selectedSortDirection = .asc
    }
}
