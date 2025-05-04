//
//  SortField.swift
//  todomob
//
//  Created by Станислав Дейнекин on 05.05.2025.
//

import Foundation

enum SortField: String, Codable, CaseIterable {
    case title = "TITLE"
    case status = "STATUS"
    case priority = "PRIORITY"
    case deadline = "DEADLINE"
    case createdAt = "CREATED_AT"
    case updatedAt = "UPDATED_AT"
}

extension SortField {
    var localized: String {
        switch self {
        case .title: return "Название"
        case .status: return "Статус"
        case .priority: return "Приоритет"
        case .deadline: return "Дедлайн"
        case .createdAt: return "Дата создания"
        case .updatedAt: return "Дата обновления"
        }
    }
}
