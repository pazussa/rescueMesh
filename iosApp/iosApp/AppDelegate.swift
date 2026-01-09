import UIKit
import ComposeApp

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    var window: UIWindow?

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        // Setup the Kotlin to Swift bridge
        setupMultipeerBridge()
        
        window = UIWindow(frame: UIScreen.main.bounds)
        let mainViewController = MainViewControllerKt.MainViewController()
        window?.rootViewController = mainViewController
        window?.makeKeyAndVisible()
        return true
    }
    
    /// Connect the IOSMultipeerBridge to the native MultipeerService
    private func setupMultipeerBridge() {
        // Set the start callback
        IOSMultipeerBridge.shared.setOnStartCallback { displayName in
            MultipeerService.shared.startSession(displayName: displayName)
        }
        
        // Set the stop callback
        IOSMultipeerBridge.shared.setOnStopCallback {
            MultipeerService.shared.stopSession()
        }
        
        // Set the send callback
        IOSMultipeerBridge.shared.setOnSendCallback { data in
            MultipeerService.shared.sendToAllPeers(data: data)
        }
    }
}
