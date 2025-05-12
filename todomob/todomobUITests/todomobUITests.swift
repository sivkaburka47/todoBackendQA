//
//  todomobUITests.swift
//  todomobUITests
//
//  Created by Станислав Дейнекин on 12.05.2025.
//

import XCTest

final class todomobUITests: XCTestCase {
    var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }

    func testCannotCreateTaskWithThreeCharacterTitle() throws {
        let addButton = app.navigationBars.buttons["plus"]
        XCTAssertTrue(addButton.exists, "Кнопка добавления задачи должна существовать")
        addButton.tap()

        let navigationBar = app.navigationBars["Новая задача"]
        XCTAssertTrue(navigationBar.exists, "Экран добавления задачи должен открыться")

        let titleTextField = app.textFields["Название задачи"]
        XCTAssertTrue(titleTextField.exists, "Поле для ввода названия задачи должно существовать")
        titleTextField.tap()
        titleTextField.typeText("abc")

        let saveButton = app.navigationBars.buttons["Сохранить"]
        XCTAssertTrue(saveButton.exists, "Кнопка 'Сохранить' должна существовать")
        XCTAssertTrue(saveButton.isEnabled, "Кнопка 'Сохранить' должна быть активна, так как поле названия не пустое")

        saveButton.tap()

        let alert = app.alerts["Ошибка"]
        XCTAssertTrue(alert.waitForExistence(timeout: 5), "Должен появиться алерт с ошибкой")

        let expectedErrorMessage = "Title must be at least 4 characters long (excluding macros)"
        XCTAssertTrue(alert.staticTexts[expectedErrorMessage].exists, "Текст ошибки должен быть: '\(expectedErrorMessage)'")

        alert.buttons["OK"].tap()
        XCTAssertFalse(alert.exists, "Алерт должен закрыться после нажатия 'OK'")

        XCTAssertTrue(navigationBar.exists, "Экран добавления задачи не должен закрыться после ошибки")

        let cancelButton = app.navigationBars.buttons["Отмена"]
        cancelButton.tap()
        let tasksTable = app.tables["Задачи"]
        XCTAssertFalse(tasksTable.cells.staticTexts["abc"].exists, "Задача с названием 'abc' не должна быть создана")
        }

    func testCreateTaskWithFourCharacterTitleSucceedsAndAppearsFirst() throws {
        let addButton = app.navigationBars.buttons["plus"]
        XCTAssertTrue(addButton.exists, "Кнопка добавления задачи должна существовать")
        addButton.tap()

        let navigationBar = app.navigationBars["Новая задача"]
        XCTAssertTrue(navigationBar.waitForExistence(timeout: 5), "Экран добавления задачи должен открыться")

        let titleTextField = app.textFields["Название задачи"]
        XCTAssertTrue(titleTextField.exists, "Поле для ввода названия задачи должно существовать")
        titleTextField.tap()
        titleTextField.typeText("test")

        let formArea = app.otherElements["AddTaskForm"].firstMatch
        if formArea.exists {
            formArea.tap()
        } else {
            app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5)).tap()
        }

        let keyboard = app.keyboards.element
        XCTAssertFalse(keyboard.waitForExistence(timeout: 2), "Клавиатура должна скрыться после тапа по пустой области")

        let descriptionTextEditor = app.textViews["Описание задачи"]
        XCTAssertTrue(descriptionTextEditor.exists, "Поле описания должно существовать")
        XCTAssertEqual(descriptionTextEditor.value as? String, "", "Описание должно быть пустым по умолчанию")

        let priorityElement = app.buttons["PriorityPicker"].firstMatch
        XCTAssertTrue(priorityElement.waitForExistence(timeout: 5), "Элемент выбора приоритета должен существовать")
        let noPriorityText = app.staticTexts["Без приоритета"]
        XCTAssertTrue(noPriorityText.exists, "Приоритет должен быть 'Без приоритета' по умолчанию")

        let deadlineToggle = app.switches["Установить дедлайн"].firstMatch
        XCTAssertTrue(deadlineToggle.waitForExistence(timeout: 5), "Тоггл дедлайна должен существовать")
        XCTAssertFalse(deadlineToggle.isSelected, "Дедлайн должен быть отключен по умолчанию")

        let saveButton = app.navigationBars.buttons["Сохранить"]
        XCTAssertTrue(saveButton.exists, "Кнопка 'Сохранить' должна существовать")
        XCTAssertTrue(saveButton.isEnabled, "Кнопка 'Сохранить' должна быть активна для названия из 4 символов")

        saveButton.tap()
        XCTAssertFalse(navigationBar.waitForExistence(timeout: 5), "Экран добавления задачи должен закрыться после успешного сохранения")

        let tasksTable = app.collectionViews["TasksList"].firstMatch
        XCTAssertTrue(tasksTable.waitForExistence(timeout: 5), "Список задач должен существовать")

        let firstTaskCell = tasksTable.cells.element(boundBy: 0)
        XCTAssertTrue(firstTaskCell.waitForExistence(timeout: 5), "Первая ячейка в списке задач должна существовать")
        print(firstTaskCell.debugDescription)
        let titleLabel = firstTaskCell.staticTexts["titleLabel"].firstMatch
        XCTAssertEqual(titleLabel.label, "test", "Задача с названием 'test' должна быть первой в списке")
    }

    func testFullEditTask() throws {
        let tasksTable = app.collectionViews["TasksList"].firstMatch
        XCTAssertTrue(tasksTable.waitForExistence(timeout: 5), "Список задач должен существовать")

        let firstCell = tasksTable.cells.element(boundBy: 0)
        XCTAssertTrue(firstCell.waitForExistence(timeout: 5), "Первая ячейка в списке задач должна существовать")
        firstCell.tap()

        let titleTextField = app.textFields["Название задачи"]
        XCTAssertTrue(titleTextField.exists, "Поле для ввода названия задачи должно существовать")
        titleTextField.tap()
        titleTextField.clearAndEnterText(text: "Изменённая задача")

        print(app.debugDescription)
        let descriptionField = app.textViews["Описание задачи"]
        XCTAssertTrue(descriptionField.exists)
        descriptionField.tap()
        descriptionField.clearAndEnterText(text: "Новое описание задачи")

        let priorityPicker = app.buttons["Приоритет, Выбор приоритета"]
        XCTAssertTrue(priorityPicker.exists, "Пикер приоритета должен существовать")
        priorityPicker.tap()

        let highPriorityOption = app.buttons["Высокий"]
        XCTAssertTrue(highPriorityOption.waitForExistence(timeout: 2), "Опция 'Высокий' должна появиться")
        highPriorityOption.tap()


        let deadlineToggle = app.switches["DeadlineToggle"]
        let normalized = deadlineToggle.coordinate(withNormalizedOffset: CGVector(dx: 0.9, dy: 0.5))
        normalized.tap()
        let newValue = deadlineToggle.value as? String
        XCTAssertEqual(newValue, "1", "Переключатель должен быть включен после тапа")

        app.scrollToElement(element: deadlineToggle)

        let tomorrow = Calendar.current.date(byAdding: .day, value: 1, to: Date())!
        let day = Calendar.current.component(.day, from: tomorrow)
        let dayString = "\(day)"

        let dateElement = app.staticTexts[dayString]
        XCTAssertTrue(dateElement.waitForExistence(timeout: 5), "Элемент даты \(dayString) должен существовать")
        dateElement.tap()

        let saveButton = app.navigationBars.buttons["Сохранить"]
            XCTAssertTrue(saveButton.waitForExistence(timeout: 5), "Кнопка 'Сохранить' должна существовать")
            saveButton.tap()
    }

    func testStatusActive() throws {
        let tasksTable = app.collectionViews["TasksList"].firstMatch
        XCTAssertTrue(tasksTable.waitForExistence(timeout: 5), "Список задач должен существовать")

        let updatedFirstCell = tasksTable.cells.element(boundBy: 0)
        XCTAssertTrue(updatedFirstCell.waitForExistence(timeout: 5), "Первая ячейка в списке задач должна существовать")

        let statusLabel = updatedFirstCell.staticTexts["status"]
        XCTAssertTrue(statusLabel.waitForExistence(timeout: 5), "Статус задачи должен быть доступен")

        XCTAssertEqual(statusLabel.label, "Активная", "Статус задачи должен быть 'Активная'")
    }

    func testDeadlineTomorrowEditTask() throws {
        let tasksTable = app.collectionViews["TasksList"].firstMatch
        XCTAssertTrue(tasksTable.waitForExistence(timeout: 5), "Список задач должен существовать")

        let firstCell = tasksTable.cells.element(boundBy: 0)
        XCTAssertTrue(firstCell.waitForExistence(timeout: 5), "Первая ячейка в списке задач должна существовать")
        firstCell.tap()

        tasksTable.swipeUp()

        let tomorrow = Calendar.current.date(byAdding: .day, value: -1, to: Date())!
        let day = Calendar.current.component(.day, from: tomorrow)
        let dayString = "\(day)"

        let dateElement = app.staticTexts[dayString]
        XCTAssertTrue(dateElement.waitForExistence(timeout: 5), "Элемент даты \(dayString) должен существовать")
        dateElement.tap()

        let saveButton = app.navigationBars.buttons["Сохранить"]
            XCTAssertTrue(saveButton.waitForExistence(timeout: 5), "Кнопка 'Сохранить' должна существовать")
            saveButton.tap()
    }

    func testStatusOverdue() throws {
        let tasksTable = app.collectionViews["TasksList"].firstMatch
        XCTAssertTrue(tasksTable.waitForExistence(timeout: 5), "Список задач должен существовать")

        let updatedFirstCell = tasksTable.cells.element(boundBy: 0)
        XCTAssertTrue(updatedFirstCell.waitForExistence(timeout: 5), "Первая ячейка в списке задач должна существовать")

        let statusLabel = updatedFirstCell.staticTexts["status"]
        XCTAssertTrue(statusLabel.waitForExistence(timeout: 5), "Статус задачи должен быть доступен")

        XCTAssertEqual(statusLabel.label, "Просроченная", "Статус задачи должен быть 'Просроченная'")
    }

    func testCompleteOverdueTask() throws {
        let tasksTable = app.collectionViews["TasksList"].firstMatch
        XCTAssertTrue(tasksTable.waitForExistence(timeout: 5), "Список задач должен существовать")

        let updatedFirstCell = tasksTable.cells.element(boundBy: 0)
        XCTAssertTrue(updatedFirstCell.waitForExistence(timeout: 5), "Первая ячейка в списке задач должна существовать")

        print(updatedFirstCell.debugDescription)
        let checkmarkButton = updatedFirstCell.buttons["checkmarkButton"].firstMatch
        XCTAssertTrue(checkmarkButton.isHittable, "Кнопка должна быть доступна для взаимодействия")

        checkmarkButton.tap()
        let statusLabel = updatedFirstCell.staticTexts["status"]
        XCTAssertTrue(statusLabel.waitForExistence(timeout: 5), "Статус задачи должен быть доступен")

        XCTAssertEqual(statusLabel.label, "Завершена с опозданием", "Статус задачи должен быть 'Завершена с опозданием'")
    }

    func testUncompleteLateTask() throws {
        let tasksTable = app.collectionViews["TasksList"].firstMatch
        XCTAssertTrue(tasksTable.waitForExistence(timeout: 5), "Список задач должен существовать")

        let updatedFirstCell = tasksTable.cells.element(boundBy: 0)
        XCTAssertTrue(updatedFirstCell.waitForExistence(timeout: 5), "Первая ячейка в списке задач должна существовать")

        print(updatedFirstCell.debugDescription)
        let checkmarkButton = updatedFirstCell.buttons["checkmarkButton"].firstMatch
        XCTAssertTrue(checkmarkButton.isHittable, "Кнопка должна быть доступна для взаимодействия")

        checkmarkButton.tap()
        let statusLabel = updatedFirstCell.staticTexts["status"]
        XCTAssertTrue(statusLabel.waitForExistence(timeout: 5), "Статус задачи должен быть доступен")

        XCTAssertEqual(statusLabel.label, "Просроченная", "Статус задачи должен быть 'Просроченная'")
    }

    func testSetCompleteToOverdueSec() throws {
        let tasksTable = app.collectionViews["TasksList"].firstMatch
        XCTAssertTrue(tasksTable.waitForExistence(timeout: 5), "Список задач должен существовать")

        let updatedFirstCell = tasksTable.cells.element(boundBy: 0)
        XCTAssertTrue(updatedFirstCell.waitForExistence(timeout: 5), "Первая ячейка в списке задач должна существовать")

        let checkmarkButton = updatedFirstCell.buttons["checkmarkButton"].firstMatch
        XCTAssertTrue(checkmarkButton.isHittable, "Кнопка должна быть доступна для взаимодействия")

        checkmarkButton.tap()

        let statusLabel = updatedFirstCell.staticTexts["status"]
        XCTAssertTrue(statusLabel.waitForExistence(timeout: 5), "Статус задачи должен быть доступен")

        XCTAssertEqual(statusLabel.label, "Завершена с опозданием", "Статус задачи должен быть 'Завершена с опозданием'")
    }

    func testEditDeadlineToEndMonthTask() throws {
        let tasksTable = app.collectionViews["TasksList"].firstMatch
        XCTAssertTrue(tasksTable.waitForExistence(timeout: 5), "Список задач должен существовать")

        let firstCell = tasksTable.cells.element(boundBy: 0)
        XCTAssertTrue(firstCell.waitForExistence(timeout: 5), "Первая ячейка в списке задач должна существовать")
        firstCell.tap()

        let deadlineToggle = app.switches["DeadlineToggle"]
        app.scrollToElement(element: deadlineToggle)

        let calendar = Calendar.current
        let currentDate = Date()
        let range = calendar.range(of: .day, in: .month, for: currentDate)!
        let lastDayOfMonth = range.upperBound - 1
        let lastDayString = "\(lastDayOfMonth)"

        let dateElement = app.staticTexts[lastDayString]
        XCTAssertTrue(dateElement.waitForExistence(timeout: 5), "Элемент даты \(lastDayString) должен существовать")
        dateElement.tap()

        let saveButton = app.navigationBars.buttons["Сохранить"]
        XCTAssertTrue(saveButton.waitForExistence(timeout: 5), "Кнопка 'Сохранить' должна существовать")
        saveButton.tap()

        let statusLabel = firstCell.staticTexts["status"]
        XCTAssertTrue(statusLabel.waitForExistence(timeout: 5), "Статус задачи должен быть доступен")

        XCTAssertEqual(statusLabel.label, "Завершённая", "Статус задачи должен быть 'Завершённая'")
    }

    func testCreateTaskWithMacrosPriorityOneTitleSucceeds() throws {
        let addButton = app.navigationBars.buttons["plus"]
        XCTAssertTrue(addButton.exists, "Кнопка добавления задачи должна существовать")
        addButton.tap()

        let navigationBar = app.navigationBars["Новая задача"]
        XCTAssertTrue(navigationBar.waitForExistence(timeout: 5), "Экран добавления задачи должен открыться")

        let titleTextField = app.textFields["Название задачи"]
        XCTAssertTrue(titleTextField.exists, "Поле для ввода названия задачи должно существовать")
        titleTextField.tap()
        titleTextField.typeText("!1 test priority")

        let formArea = app.otherElements["AddTaskForm"].firstMatch
        if formArea.exists {
            formArea.tap()
        } else {
            app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5)).tap()
        }

        let keyboard = app.keyboards.element
        XCTAssertFalse(keyboard.waitForExistence(timeout: 2), "Клавиатура должна скрыться после тапа по пустой области")

        let saveButton = app.navigationBars.buttons["Сохранить"]
        saveButton.tap()

        XCTAssertFalse(navigationBar.waitForExistence(timeout: 5), "Экран добавления задачи должен закрыться после успешного сохранения")

        let tasksTable = app.collectionViews["TasksList"].firstMatch
        XCTAssertTrue(tasksTable.waitForExistence(timeout: 5), "Список задач должен существовать")

        let firstTaskCell = tasksTable.cells.element(boundBy: 0)
        XCTAssertTrue(firstTaskCell.waitForExistence(timeout: 5), "Первая ячейка в списке задач должна существовать")
        print(firstTaskCell.debugDescription)
        let titleLabel = firstTaskCell.staticTexts["titleLabel"].firstMatch
        XCTAssertEqual(titleLabel.label, "test priority", "Задача с названием 'test priority' должна быть первой в списке")

        let priorityLabel = firstTaskCell.staticTexts["priorityLabel"]
        XCTAssertTrue(priorityLabel.waitForExistence(timeout: 5), "Приоритет задачи должен быть доступен")

        XCTAssertEqual(priorityLabel.label, "Критический", "Приоритет задачи должен быть 'Критический'")
    }

    func testCreateTaskWithMacrosBeforeOneTitleSucceeds() throws {
        let addButton = app.navigationBars.buttons["plus"]
        XCTAssertTrue(addButton.exists, "Кнопка добавления задачи должна существовать")
        addButton.tap()

        let navigationBar = app.navigationBars["Новая задача"]
        XCTAssertTrue(navigationBar.waitForExistence(timeout: 5), "Экран добавления задачи должен открыться")

        let titleTextField = app.textFields["Название задачи"]
        XCTAssertTrue(titleTextField.exists, "Поле для ввода названия задачи должно существовать")
        titleTextField.tap()
        titleTextField.typeText("!before 20.05.2025 test deadline before")

        let formArea = app.otherElements["AddTaskForm"].firstMatch
        if formArea.exists {
            formArea.tap()
        } else {
            app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5)).tap()
        }

        let keyboard = app.keyboards.element
        XCTAssertFalse(keyboard.waitForExistence(timeout: 2), "Клавиатура должна скрыться после тапа по пустой области")

        let saveButton = app.navigationBars.buttons["Сохранить"]
        saveButton.tap()

        XCTAssertFalse(navigationBar.waitForExistence(timeout: 5), "Экран добавления задачи должен закрыться после успешного сохранения")

        let tasksTable = app.collectionViews["TasksList"].firstMatch
        XCTAssertTrue(tasksTable.waitForExistence(timeout: 5), "Список задач должен существовать")

        let firstTaskCell = tasksTable.cells.element(boundBy: 0)
        XCTAssertTrue(firstTaskCell.waitForExistence(timeout: 5), "Первая ячейка в списке задач должна существовать")
        print(firstTaskCell.debugDescription)
        let titleLabel = firstTaskCell.staticTexts["titleLabel"].firstMatch
        XCTAssertEqual(titleLabel.label, "test deadline before", "Задача с названием 'test deadline before' должна быть первой в списке")

        let expectedDate = DateComponents(calendar: .current, year: 2025, month: 5, day: 20).date!
         let formatter = DateFormatter()
         formatter.locale = Locale(identifier: "ru_RU")
         formatter.dateFormat = "dd.MM.yyyy"
         let expectedDateString = formatter.string(from: expectedDate)

         let deadlineLabel = app.staticTexts.matching(NSPredicate(format: "label CONTAINS[c] %@", expectedDateString)).firstMatch
    }

    func testCreateTaskWithMacrosPriorityAndBeforeOneTitleSucceeds() throws {
        let addButton = app.navigationBars.buttons["plus"]
        XCTAssertTrue(addButton.exists, "Кнопка добавления задачи должна существовать")
        addButton.tap()

        let navigationBar = app.navigationBars["Новая задача"]
        XCTAssertTrue(navigationBar.waitForExistence(timeout: 5), "Экран добавления задачи должен открыться")

        let titleTextField = app.textFields["Название задачи"]
        XCTAssertTrue(titleTextField.exists, "Поле для ввода названия задачи должно существовать")
        titleTextField.tap()
        titleTextField.typeText("!before 20.05.2025 !1 test deadline before and priority")

        let formArea = app.otherElements["AddTaskForm"].firstMatch
        if formArea.exists {
            formArea.tap()
        } else {
            app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5)).tap()
        }

        let keyboard = app.keyboards.element
        XCTAssertFalse(keyboard.waitForExistence(timeout: 2), "Клавиатура должна скрыться после тапа по пустой области")

        let saveButton = app.navigationBars.buttons["Сохранить"]
        saveButton.tap()

        XCTAssertFalse(navigationBar.waitForExistence(timeout: 5), "Экран добавления задачи должен закрыться после успешного сохранения")

        let tasksTable = app.collectionViews["TasksList"].firstMatch
        XCTAssertTrue(tasksTable.waitForExistence(timeout: 5), "Список задач должен существовать")

        let firstTaskCell = tasksTable.cells.element(boundBy: 0)
        XCTAssertTrue(firstTaskCell.waitForExistence(timeout: 5), "Первая ячейка в списке задач должна существовать")
        print(firstTaskCell.debugDescription)
        let titleLabel = firstTaskCell.staticTexts["titleLabel"].firstMatch
        XCTAssertEqual(titleLabel.label, "test deadline before and priority", "Задача с названием 'test deadline before and priority' должна быть первой в списке")

        let priorityLabel = firstTaskCell.staticTexts["priorityLabel"]
        XCTAssertTrue(priorityLabel.waitForExistence(timeout: 5), "Приоритет задачи должен быть доступен")

        XCTAssertEqual(priorityLabel.label, "Критический", "Приоритет задачи должен быть 'Критический'")

        let expectedDate = DateComponents(calendar: .current, year: 2025, month: 5, day: 20).date!
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "ru_RU")
        formatter.dateFormat = "dd.MM.yyyy"
        let expectedDateString = formatter.string(from: expectedDate)

        let deadlineLabel = app.staticTexts.matching(NSPredicate(format: "label CONTAINS[c] %@", expectedDateString)).firstMatch
    }

    func testCreateTaskWithMacrosPriorityAndBeforeWithForm() throws {
        let addButton = app.navigationBars.buttons["plus"]
        XCTAssertTrue(addButton.exists, "Кнопка добавления задачи должна существовать")
        addButton.tap()

        let navigationBar = app.navigationBars["Новая задача"]
        XCTAssertTrue(navigationBar.waitForExistence(timeout: 5), "Экран добавления задачи должен открыться")

        let titleTextField = app.textFields["Название задачи"]
        XCTAssertTrue(titleTextField.exists, "Поле для ввода названия задачи должно существовать")
        titleTextField.tap()
        titleTextField.typeText("!4 !before 25.05.2025 test deadline before and priority")

        let formArea = app.otherElements["AddTaskForm"].firstMatch
        if formArea.exists {
            formArea.tap()
        } else {
            app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5)).tap()
        }

        let keyboard = app.keyboards.element
        XCTAssertFalse(keyboard.waitForExistence(timeout: 2), "Клавиатура должна скрыться после тапа по пустой области")

        let priorityPicker = app.buttons["Приоритет, Выбор приоритета"]
        XCTAssertTrue(priorityPicker.exists, "Пикер приоритета должен существовать")
        priorityPicker.tap()

        let highPriorityOption = app.buttons["Высокий"]
        XCTAssertTrue(highPriorityOption.waitForExistence(timeout: 2), "Опция 'Высокий' должна появиться")
        highPriorityOption.tap()


        let deadlineToggle = app.switches["DeadlineToggle"]
        let normalized = deadlineToggle.coordinate(withNormalizedOffset: CGVector(dx: 0.9, dy: 0.5))
        normalized.tap()
        let newValue = deadlineToggle.value as? String
        XCTAssertEqual(newValue, "1", "Переключатель должен быть включен после тапа")

        app.swipeUp()

        let tomorrow = Calendar.current.date(byAdding: .day, value: 1, to: Date())!
        let day = Calendar.current.component(.day, from: tomorrow)
        let dayString = "\(day)"

        let dateElement = app.staticTexts[dayString]
        XCTAssertTrue(dateElement.waitForExistence(timeout: 5), "Элемент даты \(dayString) должен существовать")
        dateElement.tap()

        let saveButton = app.navigationBars.buttons["Сохранить"]
        saveButton.tap()

        XCTAssertFalse(navigationBar.waitForExistence(timeout: 5), "Экран добавления задачи должен закрыться после успешного сохранения")

        let tasksTable = app.collectionViews["TasksList"].firstMatch
        XCTAssertTrue(tasksTable.waitForExistence(timeout: 5), "Список задач должен существовать")

        let firstTaskCell = tasksTable.cells.element(boundBy: 0)
        XCTAssertTrue(firstTaskCell.waitForExistence(timeout: 5), "Первая ячейка в списке задач должна существовать")
        print(firstTaskCell.debugDescription)
        let titleLabel = firstTaskCell.staticTexts["titleLabel"].firstMatch
        XCTAssertEqual(titleLabel.label, "test deadline before and priority", "Задача с названием 'test deadline before and priority' должна быть первой в списке")

        let priorityLabel = firstTaskCell.staticTexts["priorityLabel"]
        XCTAssertTrue(priorityLabel.waitForExistence(timeout: 5), "Приоритет задачи должен быть доступен")

        XCTAssertEqual(priorityLabel.label, "Высокий", "Приоритет задачи должен быть 'Высокий'")

        let expectedDate = tomorrow
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "ru_RU")
        formatter.dateFormat = "dd.MM.yyyy"
        let expectedDateString = formatter.string(from: expectedDate)

        let deadlineLabel = app.staticTexts.matching(NSPredicate(format: "label CONTAINS[c] %@", expectedDateString)).firstMatch
    }


    func testTestFiltersDesc() throws {
        let filterButton = app.navigationBars.buttons["slider.horizontal.3"]
        XCTAssertTrue(filterButton.exists, "Кнопка фильтров должна существовать")
        filterButton.tap()

        print(app.debugDescription)
        let priorityPicker = app.buttons["SortFieldPicker"]
        XCTAssertTrue(priorityPicker.exists, "Пикер приоритета должен существовать")
        priorityPicker.tap()

        let priorityOption = app.buttons["Статус"]
        XCTAssertTrue(priorityOption.waitForExistence(timeout: 2), "Опция 'Статус' должна появиться")
        priorityOption.tap()

        print(app.debugDescription)
        let directionPicker = app.buttons["SortDirectionPicker"]
        XCTAssertTrue(directionPicker.exists, "Пикер направления должен существовать")
        directionPicker.tap()

        let directionButton = app.buttons["По возрастанию"]
        XCTAssertTrue(directionButton.waitForExistence(timeout: 2), "Опция 'По возрастанию' должна появиться")
        directionButton.tap()

        let saveButton = app.navigationBars.buttons["Применить"]
        saveButton.tap()
    }



}
