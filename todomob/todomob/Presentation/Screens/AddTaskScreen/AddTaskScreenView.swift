//
//  AddTaskScreenView.swift
//  todomob
//
//  Created by Станислав Дейнекин on 05.05.2025.
//

import SwiftUI

struct AddTaskScreenView: View {
    @StateObject private var viewModel = AddTaskViewModel()
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Название")) {
                    TextField("Введите название задачи", text: $viewModel.title)
                        .font(.system(.body, design: .rounded))
                        .accessibilityLabel("Название задачи")
                }

                Section(header: Text("Описание")) {
                    TextEditor(text: $viewModel.description)
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
                    Toggle("Установить дедлайн", isOn: $viewModel.isShowingDeadline)
                        .font(.system(.body, design: .rounded))
                        .accessibilityLabel("Установить дедлайн")

                    if viewModel.isShowingDeadline {
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
            }
            .navigationTitle("Новая задача")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Отмена") {
                        viewModel.resetForm()
                        dismiss()
                    }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Сохранить") {
                        Task {
                            if await viewModel.createTask() {
                                viewModel.resetForm()
                                dismiss()
                            }
                        }
                    }
                    .disabled(viewModel.title.isEmpty)
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
        }
    }
}
