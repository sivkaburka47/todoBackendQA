//
//  CustomTestElements.swift
//  todomob
//
//  Created by Станислав Дейнекин on 12.05.2025.
//

import XCTest

extension XCUIElement {
    func clearAndEnterText(text: String) {
        guard self.exists, self.isHittable else { return }
        self.tap()

        if let stringValue = self.value as? String {
            let deleteString = String(repeating: XCUIKeyboardKey.delete.rawValue, count: stringValue.count)
            self.typeText(deleteString)
        }

        self.typeText(text)
    }

    func scrollToElement(element: XCUIElement) {
            while !element.isHittable {
                swipeUp()
                if element.exists && element.isHittable {
                    break
                }
            }
        }
}
