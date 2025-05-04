//
//  MainScreenView.swift
//  todomob
//
//  Created by Станислав Дейнекин on 05.05.2025.
//

import SwiftUI

struct MainScreenView: View {
    @StateObject private var viewModel = MainViewModel()
    @State private var showFilters = false
    @State private var showAddTask = false

    var body: some View {
        NavigationView {
            Group {
                if viewModel.isLoading {
                    ProgressView()
                        .scaleEffect(1.5)
                } else if viewModel.tasks.isEmpty {
                    emptyStateView
                } else {
                    listContent
                }
            }
            .navigationTitle("Задачи")
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button {
                        showAddTask = true
                    } label: {
                        Image(systemName: "plus")
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        showFilters = true
                    } label: {
                        Image(systemName: "slider.horizontal.3")
                    }
                }
            }
            .sheet(isPresented: $showFilters, onDismiss: {
                Task { await viewModel.fetchTasks() }
            }, content: {
                FilterSortView(viewModel: viewModel)
            })
//            .sheet(isPresented: $showAddTask, onDismiss: {
//                Task { await viewModel.fetchTasks() }
//            }, content: {
//                AddTaskView(viewModel: viewModel)
//            })
            .onAppear {
                Task {
                    await viewModel.fetchTasks()
                }
            }
        }
    }
}

extension MainScreenView {

    private struct TaskRow: View {
        let task: TaskEntity
        @Environment(\.colorScheme) private var colorScheme

        private var deadlineColor: Color? {
            guard let deadline = task.deadline, task.status != .completed else { return nil }

            let now = Date()
            if deadline < now {
                return .red
            } else if Calendar.current.dateComponents([.day], from: now, to: deadline).day ?? 0 < 3 {
                return .orange
            } else {
                return nil
            }
        }

        var body: some View {
             VStack(alignment: .leading, spacing: 12) {
                 HStack(alignment: .top, spacing: 12) {
                     Button(action: {
                         withAnimation(.spring()) {
                             // await viewModel.toggleTask(task)
                         }
                     }) {
                         ZStack {
                             RoundedRectangle(cornerRadius: 6)
                                 .strokeBorder(task.status == .late || task.status == .completed ? Color.green : Color.gray, lineWidth: 2)
                                 .frame(width: 24, height: 24)
                                 .background(
                                     RoundedRectangle(cornerRadius: 6)
                                         .fill((task.status == .late || task.status == .completed) ? Color.green.opacity(0.2) : Color.clear)
                                 )

                             if task.status == .late || task.status == .completed {
                                 Image(systemName: "checkmark")
                                     .foregroundColor(.green)
                                     .font(.system(size: 14, weight: .bold))
                             }
                         }
                     }
                     .buttonStyle(.plain)

                     VStack(alignment: .leading, spacing: 6) {
                         Text(task.title)
                             .font(.system(.title3, design: .rounded, weight: .semibold))
                             .foregroundColor(.primary)
                             .lineLimit(1)

                         if let description = task.description, !description.isEmpty {
                             Text(description)
                                 .font(.system(.subheadline, design: .default))
                                 .foregroundColor(.secondary)
                                 .lineLimit(2)
                         } else {
                             Text("Описание отсутствует")
                                 .font(.system(.subheadline, design: .default))
                                 .foregroundColor(.secondary.opacity(0.5))
                                 .italic()
                         }
                     }
                 }

                 Divider()
                     .background(Color.gray.opacity(0.3))
                     .padding(.vertical, 4)

                 HStack(spacing: 12) {
                     Text(task.status.localized)
                         .font(.system(.caption, design: .rounded, weight: .medium))
                         .foregroundColor(.white)
                         .padding(.horizontal, 8)
                         .padding(.vertical, 4)
                         .background(Color.gray.opacity(0.1))
                         .clipShape(RoundedRectangle(cornerRadius: 8))
                         .overlay(
                             RoundedRectangle(cornerRadius: 8)
                                 .stroke(Color.gray.opacity(0.2), lineWidth: 1)
                         )
                     Spacer()
                     Text(task.priority.localized)
                         .font(.system(.caption, design: .rounded, weight: .medium))
                         .foregroundColor(.white)
                         .padding(.horizontal, 8)
                         .padding(.vertical, 4)
                         .background(task.priority.color)
                         .clipShape(RoundedRectangle(cornerRadius: 8))
                         .overlay(
                             RoundedRectangle(cornerRadius: 8)
                                 .stroke(Color.gray.opacity(0.2), lineWidth: 1)
                         )
                 }

                 VStack(alignment: .leading, spacing: 12) {
                     if let deadline = task.deadline {
                         InfoBadgeDeadline(
                             text: "Дедлайн: \(deadline.formatted(date: .abbreviated, time: .shortened))"
                         )
                     } else {
                         InfoBadgeDeadline(
                             text: "Дедлайн: Отсутствует"
                         )
                     }

                     InfoBadge(
                         icon: "plus.circle",
                         text: "Создан: \(task.createdAt.formatted(date: .numeric, time: .shortened))"
                     )
                     InfoBadge(
                         icon: "clock.arrow.circlepath",
                         text: "Обновлён: \(task.updatedAt.formatted(date: .numeric, time: .shortened))"
                     )
                 }
             }
             .padding(.vertical, 12)
             .padding(.horizontal, 16)
             .background(
                 (deadlineColor ?? Color(.systemBackground))
                     .opacity(deadlineColor != nil ? 0.1 : 1.0)
             )
             .clipShape(RoundedRectangle(cornerRadius: 16))
             .shadow(color: .gray.opacity(colorScheme == .dark ? 0.2 : 0.1), radius: 4, x: 0, y: 2)
         }
    }

    private var listContent: some View {
        List {
            ForEach(viewModel.tasks) { task in
                TaskRow(task: task)
                    .listRowInsets(EdgeInsets(top: 8, leading: 16, bottom: 8, trailing: 16))
                    .listRowSeparatorTint(.gray.opacity(0.2))
                    .swipeActions(edge: .trailing) {
                        Button(role: .destructive) {
                            Task { await viewModel.deleteTask(task) }
                        } label: {
                            Label("Удалить", systemImage: "trash")
                        }
                    }
            }
        }
        .refreshable {
            await viewModel.fetchTasks()
        }
    }

    private struct InfoBadge: View {
        let icon: String
        let text: String

        var body: some View {
            HStack(spacing: 4) {
                Image(systemName: icon)
                    .font(.system(size: 12))
                Text(text)
                    .font(.system(size: 12, design: .monospaced))
            }
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(Color.gray.opacity(0.1))
            .cornerRadius(6)
        }
    }

    private struct InfoBadgeDeadline: View {
        let text: String

        var body: some View {
            HStack(spacing: 4) {
                Image(systemName: "timer")
                    .font(.system(size: 12))
                Text(text)
                    .font(.system(size: 12, design: .monospaced))
            }
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(Color.gray.opacity(0.1))
            .cornerRadius(6)
        }
    }

    private var emptyStateView: some View {
        VStack(spacing: 20) {
            Image(systemName: "checklist")
                .font(.system(size: 40))
                .foregroundColor(.gray)

            Text("Список задач пуст")
                .font(.title3)
                .foregroundColor(.gray)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
