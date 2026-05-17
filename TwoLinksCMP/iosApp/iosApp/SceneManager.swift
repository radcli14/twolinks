import ComposeApp
import Observation
import RealityKit
import SceneViewSwift
import UIKit

@Observable class SceneManager {

    // Entity refs — set once during SceneView buildScene, used by controller each frame
    weak var link1AnchorEntity: Entity?
    weak var link2AnchorEntity: Entity?
    weak var pivot2AnchorEntity: Entity?
    weak var link1Entity: ModelEntity?
    weak var link2Entity: ModelEntity?

    // Color change detection
    var lastLink0Color = SIMD3<Float>(repeating: -1)
    var lastLink1Color = SIMD3<Float>(repeating: -1)
    
    // MARK: - Update
    
    func applyTransforms(from twoLinks: TwoLinks) {
        // The 180° Y wrapper negates world X and Z; negate angles and positions to compensate
        link1AnchorEntity?.orientation = twoLinks.linkOneOrientation
        link1Entity?.position = twoLinks.linkOnePosition
        link1Entity?.scale = twoLinks.linkOneScale

        pivot2AnchorEntity?.position = -twoLinks.pivotPosition.asSIMD3

        link2AnchorEntity?.orientation = twoLinks.linkTwoOrientation
        link2Entity?.position = twoLinks.linkTwoPosition
        link2Entity?.scale = twoLinks.linkTwoScale
    }

    func applyColors(from twoLinks: TwoLinks) {
        guard let l0 = twoLinks.links.first, let l1 = twoLinks.links.last else { return }

        let c0 = SIMD3<Float>(l0.color.x, l0.color.y, l0.color.z)
        let c1 = SIMD3<Float>(l1.color.x, l1.color.y, l1.color.z)

        if c0 != lastLink0Color, let link1Entity {
            lastLink0Color = c0
            applyColor(l0.color.asUIColor, to: link1Entity)
        }

        if c1 != lastLink1Color, let link2Entity {
            lastLink1Color = c1
            applyColor(l1.color.asUIColor, to: link2Entity)
        }
    }
    
    private func applyColor(_ color: UIColor, to link: ModelEntity) {
        var mat = SimpleMaterial()
        mat.color    = .init(tint: color)
        mat.metallic  = .init(floatLiteral: 0.5)
        mat.roughness = .init(floatLiteral: 0.4)
        link.model?.materials = [mat]
    }
}
