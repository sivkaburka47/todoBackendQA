//
//  Task.swift
//  todomob
//
//  Created by Станислав Дейнекин on 05.05.2025.
//

import Foundation

struct TaskEntity: Identifiable, Codable, Equatable {
    let id: UUID
    var title: String
    var description: String?
    let status: Status
    var priority: Priority
    var deadline: Date?
    let createdAt: Date
    let updatedAt: Date

    enum CodingKeys: String, CodingKey {
        case id, title, description, status, priority, deadline, createdAt, updatedAt
    }
}
