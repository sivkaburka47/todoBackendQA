//
//  SortDirection.swift
//  todomob
//
//  Created by Станислав Дейнекин on 05.05.2025.
//

import Foundation

enum SortDirection: String, Codable, CaseIterable {
    case asc = "ASC"
    case desc = "DESC"
}

extension SortDirection {
    var localized: String {
        switch self {
        case .asc: return "По возрастанию"
        case .desc: return "По убыванию"
        }
    }
}
