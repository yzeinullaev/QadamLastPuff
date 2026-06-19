import Foundation

@MainActor
final class DataStore: ObservableObject {
    @Published private(set) var data = AppData()
    private let fileURL: URL
    private let encoder = JSONEncoder()
    private let decoder = JSONDecoder()

    init() {
        let dir = FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask).first!
        try? FileManager.default.createDirectory(at: dir, withIntermediateDirectories: true)
        fileURL = dir.appendingPathComponent("qadam_data.json")
        decoder.dateDecodingStrategy = .iso8601
        encoder.dateEncodingStrategy = .iso8601
        load()
    }

    func load() {
        guard let raw = try? Data(contentsOf: fileURL),
              let decoded = try? decoder.decode(AppData.self, from: raw) else { return }
        data = decoded
    }

    func save() {
        guard let raw = try? encoder.encode(data) else { return }
        try? raw.write(to: fileURL, options: .atomic)
    }

    func update(_ block: (inout AppData) -> Void) {
        block(&data)
        save()
    }
}
