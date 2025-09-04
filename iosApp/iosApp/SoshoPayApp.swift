import SwiftUI
import Shared

@main
struct SoshoPayApp: App {
    
    init() {
        // Initialize Koin
        KoinHelperKt.doInitKoin()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
