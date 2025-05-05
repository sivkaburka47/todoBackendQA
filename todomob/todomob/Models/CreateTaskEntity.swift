//
//  CreateTaskEntity.swift
//  todomob
//
//  Created by Станислав Дейнекин on 05.05.2025.
//

import Foundation

struct CreateTaskEntity: Codable, Equatable {
    var title: String
    var description: String?
    var priority: Priority?
    var deadline: Date?

    enum CodingKeys: String, CodingKey {
        case title, description, priority, deadline
    }
}
