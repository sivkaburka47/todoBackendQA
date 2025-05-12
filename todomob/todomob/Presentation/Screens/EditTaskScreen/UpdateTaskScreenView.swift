//
//  UpdateTaskScreenView.swift
//  todomob
//
//  Created by Станислав Дейнекин on 05.05.2025.
//

import SwiftUI

struct UpdateTaskScreenView: View {
    @StateObject private var viewModel: UpdateTaskViewModel
    @Environment(\.dismiss) private var dismiss

    init(taskId: UUID) {
        _viewModel = StateObject(wrappedValue: UpdateTaskViewModel(taskId: taskId))
    }

    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("ID задачи")) {
                    Text(viewModel.taskId.uuidString)
                        .font(.system(.body, design: .rounded))
                        .foregroundColor(.gray)
                        .accessibilityLabel("ID задачи")
                }

                Section(header: Text("Название")) {
                    TextField("Название задачи", text: Binding(
                        get: { viewModel.title ?? "" },
                        set: { viewModel.title = $0.isEmpty ? nil : $0 }
                    ))
                    .font(.system(.body, design: .rounded))
                    .accessibilityLabel("Название задачи")
                }

                Section(header: Text("Описание")) {
                    TextEditor(text: Binding(
                        get: { viewModel.description ?? "" },
                        set: { viewModel.description = $0.isEmpty ? nil : $0 }
                    ))
                    .font(.system(.body, design: .rounded))
                    .frame(minHeight: 100)
                    .overlay(
                        RoundedRectangle(cornerRadius: 8)
                            .stroke(Color.gray.opacity(0.2), lineWidth: 1)
                    )
                    .accessibilityLabel("Описание задачи")
                }

                Section(header: Text("Приоритет")) {
                    Picker("Приоритет", selection: $viewModel.priority) {
                        Text("Без приоритета").tag(Priority?.none)
                        ForEach(Priority.allCases, id: \.self) { priority in
                            Text(priority.localized).tag(Priority?.some(priority))
                        }
                    }
                    .pickerStyle(.menu)
                    .font(.system(.body, design: .rounded))
                    .accessibilityLabel("Выбор приоритета")
                }

                Section(header: Text("Дедлайн")) {
                    deadlineToggleView

                    if viewModel.deadline != nil {
                        deadlineDatePicker
                    }
                }

                Section(header: Text("Статус")) {
                    Text(viewModel.status.localized)
                        .font(.system(.body, design: .rounded))
                        .foregroundColor(.gray)
                        .accessibilityLabel("Статус задачи")
                }

                Section(header: Text("Дата создания")) {
                    Text(viewModel.createdAt.formatted(date: .numeric, time: .shortened))
                        .font(.system(.body, design: .rounded))
                        .foregroundColor(.gray)
                        .accessibilityLabel("Дата создания задачи")
                }

                Section(header: Text("Дата обновления")) {
                    Text(viewModel.updatedAt.formatted(date: .numeric, time: .shortened))
                        .font(.system(.body, design: .rounded))
                        .foregroundColor(.gray)
                        .accessibilityLabel("Дата обновления задачи")
                }
            }
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Отмена") {
                        dismiss()
                    }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Сохранить") {
                        Task {
                            if await viewModel.updateTask() {
                                dismiss()
                            }
                        }
                    }
                    .disabled(viewModel.title?.isEmpty ?? true)
                }
            }
            .alert(isPresented: Binding(
                get: { viewModel.errorMessage != nil },
                set: { if !$0 { viewModel.errorMessage = nil } }
            )) {
                Alert(
                    title: Text("Ошибка"),
                    message: Text(viewModel.errorMessage ?? "Неизвестная ошибка"),
                    dismissButton: .default(Text("OK"))
                )
            }
            .onAppear {
                Task {
                    await viewModel.fetchTask()
                }
            }
        }
    }

    private var deadlineToggleView: some View {
        Toggle("Установить дедлайн", isOn: Binding(
            get: { viewModel.isDeadlineSet },
            set: { isSet in
                viewModel.toggleDeadline(isSet: isSet)
            }
        ))
        .font(.system(.body, design: .rounded))
        .accessibilityLabel("Установить дедлайн")
        .accessibilityIdentifier("DeadlineToggle")
    }

    private var deadlineDatePicker: some View {
        DatePicker(
            "Выберите дату и время",
            selection: Binding(
                get: { viewModel.deadline ?? Date() },
                set: { viewModel.deadline = $0 }
            ),
            displayedComponents: [.date, .hourAndMinute]
        )
        .datePickerStyle(.graphical)
        .font(.system(.body, design: .rounded))
        .accessibilityLabel("Дата и время дедлайна")
    }
}
