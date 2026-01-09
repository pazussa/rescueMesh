import Foundation
import MultipeerConnectivity
import ComposeApp

/// Native MultipeerConnectivity implementation for RescueMesh
/// This class handles the actual peer-to-peer networking on iOS
class MultipeerService: NSObject {
    
    static let shared = MultipeerService()
    
    private let serviceType = "rescuemesh"
    
    private var peerID: MCPeerID?
    private var session: MCSession?
    private var advertiser: MCNearbyServiceAdvertiser?
    private var browser: MCNearbyServiceBrowser?
    
    private var currentRoomId: String?
    
    private override init() {
        super.init()
    }
    
    /// Start the mesh session with the given display name
    func startSession(displayName: String) {
        // Parse room ID from display name (format: "roomId|userName")
        let parts = displayName.split(separator: "|")
        currentRoomId = parts.first.map(String.init)
        
        // Create peer ID
        peerID = MCPeerID(displayName: displayName)
        
        // Create session
        session = MCSession(peer: peerID!, securityIdentity: nil, encryptionPreference: .required)
        session?.delegate = self
        
        // Start advertising
        advertiser = MCNearbyServiceAdvertiser(peer: peerID!, discoveryInfo: ["roomId": currentRoomId ?? ""], serviceType: serviceType)
        advertiser?.delegate = self
        advertiser?.startAdvertisingPeer()
        
        // Start browsing
        browser = MCNearbyServiceBrowser(peer: peerID!, serviceType: serviceType)
        browser?.delegate = self
        browser?.startBrowsingForPeers()
        
        print("MultipeerService: Started session as \(displayName)")
    }
    
    /// Stop the mesh session
    func stopSession() {
        advertiser?.stopAdvertisingPeer()
        browser?.stopBrowsingForPeers()
        session?.disconnect()
        
        advertiser = nil
        browser = nil
        session = nil
        peerID = nil
        currentRoomId = nil
        
        print("MultipeerService: Session stopped")
    }
    
    /// Send data to all connected peers
    func sendToAllPeers(data: String) {
        guard let session = session, !session.connectedPeers.isEmpty else {
            print("MultipeerService: No peers to send to")
            return
        }
        
        guard let messageData = data.data(using: .utf8) else {
            print("MultipeerService: Failed to encode message")
            return
        }
        
        do {
            try session.send(messageData, toPeers: session.connectedPeers, with: .reliable)
            print("MultipeerService: Sent data to \(session.connectedPeers.count) peers")
        } catch {
            print("MultipeerService: Failed to send data: \(error)")
        }
    }
}

// MARK: - MCSessionDelegate
extension MultipeerService: MCSessionDelegate {
    
    func session(_ session: MCSession, peer peerID: MCPeerID, didChange state: MCSessionState) {
        DispatchQueue.main.async {
            switch state {
            case .connected:
                let parts = peerID.displayName.split(separator: "|")
                let peerName = parts.count > 1 ? String(parts[1]) : peerID.displayName
                // Notify Kotlin bridge
                IOSMultipeerBridge.shared.notifyPeerConnected(peerId: peerID.displayName, peerName: peerName)
                print("MultipeerService: Peer connected: \(peerName)")
                
            case .notConnected:
                IOSMultipeerBridge.shared.notifyPeerDisconnected(peerId: peerID.displayName)
                print("MultipeerService: Peer disconnected: \(peerID.displayName)")
                
            case .connecting:
                print("MultipeerService: Connecting to peer: \(peerID.displayName)")
                
            @unknown default:
                break
            }
        }
    }
    
    func session(_ session: MCSession, didReceive data: Data, fromPeer peerID: MCPeerID) {
        guard let message = String(data: data, encoding: .utf8) else {
            print("MultipeerService: Failed to decode received data")
            return
        }
        
        DispatchQueue.main.async {
            IOSMultipeerBridge.shared.notifyMessageReceived(jsonString: message)
            print("MultipeerService: Received message from \(peerID.displayName)")
        }
    }
    
    func session(_ session: MCSession, didReceive stream: InputStream, withName streamName: String, fromPeer peerID: MCPeerID) {
        // Not used
    }
    
    func session(_ session: MCSession, didStartReceivingResourceWithName resourceName: String, fromPeer peerID: MCPeerID, with progress: Progress) {
        // Not used
    }
    
    func session(_ session: MCSession, didFinishReceivingResourceWithName resourceName: String, fromPeer peerID: MCPeerID, at localURL: URL?, withError error: Error?) {
        // Not used
    }
}

// MARK: - MCNearbyServiceAdvertiserDelegate
extension MultipeerService: MCNearbyServiceAdvertiserDelegate {
    
    func advertiser(_ advertiser: MCNearbyServiceAdvertiser, didReceiveInvitationFromPeer peerID: MCPeerID, withContext context: Data?, invitationHandler: @escaping (Bool, MCSession?) -> Void) {
        // Auto-accept invitations from same room
        let peerRoomId = peerID.displayName.split(separator: "|").first.map(String.init)
        
        if peerRoomId == currentRoomId {
            invitationHandler(true, session)
            print("MultipeerService: Accepted invitation from \(peerID.displayName)")
        } else {
            invitationHandler(false, nil)
            print("MultipeerService: Rejected invitation from different room: \(peerID.displayName)")
        }
    }
    
    func advertiser(_ advertiser: MCNearbyServiceAdvertiser, didNotStartAdvertisingPeer error: Error) {
        print("MultipeerService: Failed to start advertising: \(error)")
    }
}

// MARK: - MCNearbyServiceBrowserDelegate
extension MultipeerService: MCNearbyServiceBrowserDelegate {
    
    func browser(_ browser: MCNearbyServiceBrowser, foundPeer peerID: MCPeerID, withDiscoveryInfo info: [String : String]?) {
        // Only invite peers from the same room
        let peerRoomId = peerID.displayName.split(separator: "|").first.map(String.init)
        
        if peerRoomId == currentRoomId, let session = session {
            browser.invitePeer(peerID, to: session, withContext: nil, timeout: 30)
            print("MultipeerService: Found and invited peer: \(peerID.displayName)")
        }
    }
    
    func browser(_ browser: MCNearbyServiceBrowser, lostPeer peerID: MCPeerID) {
        print("MultipeerService: Lost peer: \(peerID.displayName)")
    }
    
    func browser(_ browser: MCNearbyServiceBrowser, didNotStartBrowsingForPeers error: Error) {
        print("MultipeerService: Failed to start browsing: \(error)")
    }
}
