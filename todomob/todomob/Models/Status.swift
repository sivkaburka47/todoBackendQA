//
//  Status.swift
//  todomob
//
//  Created by Станислав Дейнекин on 05.05.2025.
//

import Foundation

enum Status: String, Codable, CaseIterable {
    case active = "ACTIVE"
    case completed = "COMPLETED"
    case overdue = "OVERDUE"
    case late = "LATE"
}

extension Status {
    var localized: String {
        switch self {
        case .active: return "Активная"
        case .completed: return "Завершённая"
        case .overdue: return "Просроченная"
        case .late: return "Завершена с опозданием"
        }
    }

    
}
