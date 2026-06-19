import Foundation

final class MessageSession {
    private var random: SeededRandom
    private var used: [MessageCategory: Set<Int>] = [:]

    init(seed: Int = Int.random(in: 0...Int.max)) {
        random = SeededRandom(seed: UInt64(seed))
    }

    func pick(_ category: MessageCategory) -> String {
        let pool = SupportMessageBank.get(category)
        guard !pool.isEmpty else { return "" }
        var usedSet = used[category, default: []]
        let available = pool.indices.filter { !usedSet.contains($0) }
        let index: Int
        if available.isEmpty {
            usedSet.removeAll()
            index = random.nextInt(upperBound: pool.count)
        } else {
            index = available[random.nextInt(upperBound: available.count)]
        }
        usedSet.insert(index)
        used[category] = usedSet
        return pool[index]
    }

    func pickHumorRarely() -> String? {
        random.nextFloat() < 0.22 ? pick(.humor) : nil
    }

    func pickAction(mode: SosMode) -> String {
        switch mode {
        case .breathing: return pick(.breath)
        case .family: return pick(.family)
        case .challenge: return pick(.challenge)
        }
    }
}

private struct SeededRandom {
    private var state: UInt64

    init(seed: UInt64) { state = seed }

    mutating func nextInt(upperBound: Int) -> Int {
        guard upperBound > 0 else { return 0 }
        return Int(next() % UInt64(upperBound))
    }

    mutating func nextFloat() -> Float {
        Float(next() % 10_000) / 10_000
    }

    private mutating func next() -> UInt64 {
        state = state &* 6364136223846793005 &+ 1
        return state
    }
}
