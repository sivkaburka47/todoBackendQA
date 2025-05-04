//
//  APIEndpoint.swift
//  todomob
//
//  Created by Станислав Дейнекин on 05.05.2025.
//

import Alamofire

protocol APIEndpoint {
    var path: String { get }
    var method: HTTPMethod { get }
    var parameters: Parameters? { get }
    var headers: HTTPHeaders? { get }
}
