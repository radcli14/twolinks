import Observation
import RealityKit

@Observable class TwoLinksSceneState {

    // Entity refs — set once during SceneView buildScene, used by controller each frame
    weak var link1AnchorEntity: Entity?
    weak var link2AnchorEntity: Entity?
    weak var pivot2AnchorEntity: Entity?
    weak var link1Entity: ModelEntity?
    weak var link2Entity: ModelEntity?

    // Color change detection
    var lastLink0Color = SIMD3<Float>(repeating: -1)
    var lastLink1Color = SIMD3<Float>(repeating: -1)
}
